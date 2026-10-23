import json
import os
import subprocess
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from validate_ids import Validator, java_executable, run_reports


class BlockStateValidatorTests(unittest.TestCase):
    def setUp(self):
        self.validator = Validator.__new__(Validator)
        self.validator.problems = []
        self.properties = {
            "facing": {"north", "east", "south", "west"},
            "half": {"top", "bottom"},
        }

    def test_multipart_string_value_is_single_value(self):
        self.validator.check_state_condition(
            "blockstates/oak_stairs.json",
            "minecraft:oak_stairs",
            {"facing": "north"},
            self.properties,
        )
        self.assertEqual([], self.validator.problems)

    def test_multipart_and_or_conditions_are_checked_recursively(self):
        self.validator.check_state_condition(
            "blockstates/oak_stairs.json",
            "minecraft:oak_stairs",
            {"OR": [{"facing": "north"}, {"AND": [{"half": "top|bottom"}, {"facing": "south"}]}]},
            self.properties,
        )
        self.assertEqual([], self.validator.problems)

        self.validator.check_state_condition(
            "blockstates/oak_stairs.json",
            "minecraft:oak_stairs",
            {"OR": [{"facing": "north"}, {"half": "middle"}]},
            self.properties,
        )
        self.assertEqual(
            [("blockstates/oak_stairs.json", "minecraft:oak_stairs[half=middle]", "block-state property value")],
            self.validator.problems,
        )

    def test_shader_references_are_checked_against_shader_stages(self):
        self.validator.shader_stages = {"minecraft:sobel"}
        self.validator.check("assets/thaumcraft/shaders/program/example.json", "minecraft:sobel", "shader")
        self.validator.check("assets/thaumcraft/shaders/program/example.json", "minecraft:missing", "shader")
        self.assertEqual(
            [("assets/thaumcraft/shaders/program/example.json", "minecraft:missing", "shader")],
            self.validator.problems,
        )

    def test_modern_post_effect_validates_shader_sources_and_main_target(self):
        self.assertEqual("shader", self.validator.classify("minecraft:core/screenquad", ["vertex_shader"], "post_effect"))
        self.assertEqual("skip", self.validator.classify("minecraft:main", ["output"], "post_effect"))
        self.assertEqual("flat", self.validator.classify("minecraft:missing", ["vertex_shader"], "other"))

    def test_main_framebuffer_is_a_valid_special_shader_target(self):
        self.assertEqual("skip", self.validator.classify("minecraft:main", ["intarget"], "shader_post"))
        self.assertEqual("flat", self.validator.classify("minecraft:missing", ["name"], "shader_post"))

    def test_recipe_serializer_ids_are_not_skipped(self):
        self.validator.registry = {"recipe_serializer": {"minecraft:crafting_shaped"}}
        self.validator.check("recipes/example.json", "minecraft:crafting_shaped", "recipe_serializer")
        self.validator.check("recipes/example.json", "minecraft:missing_recipe_for_test", "recipe_serializer")
        self.assertEqual(
            [("recipes/example.json", "minecraft:missing_recipe_for_test", "recipe_serializer")],
            self.validator.problems,
        )


class ReportJavaTests(unittest.TestCase):
    def java_version(self, major):
        return subprocess.CompletedProcess([], 0, "", f'openjdk version "{major}.0.1"\n')

    @patch("validate_ids.shutil.which", return_value=None)
    def test_actions_jdk_is_selected_before_gradle_jdk(self, which):
        with patch.dict(os.environ, {"JAVA_HOME_21_X64": "/opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/21/x64", "JAVA_HOME": "/opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/25/x64"}, clear=True):
            with patch("validate_ids.subprocess.run", return_value=self.java_version(21)) as run:
                java = java_executable("1.21.1")
        self.assertEqual(str(Path("/opt/hostedtoolcache/Java_Temurin-Hotspot_jdk/21/x64/bin") / ("java.exe" if os.name == "nt" else "java")), java)
        self.assertEqual([java, "-version"], run.call_args.args[0])

    @patch("validate_ids.shutil.which", return_value=None)
    def test_java_home_with_spaces(self, which):
        with patch.dict(os.environ, {"JAVA_HOME": "/test/JDK with spaces"}, clear=True):
            with patch("validate_ids.subprocess.run", return_value=self.java_version(25)):
                self.assertEqual(str(Path("/test/JDK with spaces/bin") / ("java.exe" if os.name == "nt" else "java")), java_executable("1.21.1"))

    @patch("validate_ids.shutil.which", return_value="/usr/bin/java")
    def test_missing_java_home_falls_back_to_path(self, which):
        with patch.dict(os.environ, {"JAVA_HOME": "/missing/jdk"}, clear=True):
            with patch("validate_ids.subprocess.run", side_effect=[FileNotFoundError(), self.java_version(21)]):
                self.assertEqual("/usr/bin/java", java_executable("1.21.1"))

    @patch("validate_ids.shutil.which", return_value="/usr/bin/java")
    def test_old_java_has_actionable_error(self, which):
        with patch.dict(os.environ, {}, clear=True):
            with patch("validate_ids.subprocess.run", return_value=self.java_version(17)):
                with self.assertRaisesRegex(SystemExit, "need Java 21 or newer.*JAVA_HOME"):
                    java_executable("1.21.1")

    @patch("validate_ids.shutil.which", return_value=None)
    def test_uncached_reports_launch_selected_java(self, which):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            java = str(directory / "jdk" / "bin" / ("java.exe" if os.name == "nt" else "java"))
            commands = []

            def run(command, **kwargs):
                commands.append(command)
                if command[1:] == ["-version"]:
                    return self.java_version(21)
                self.assertEqual(directory, kwargs["cwd"])
                reports = directory / "reports/reports"
                reports.mkdir()
                (reports / "registries.json").write_text(json.dumps({"minecraft:item": {"entries": {}}}))
                return subprocess.CompletedProcess(command, 0, "", "")

            with patch.dict(os.environ, {"JAVA_HOME": str(directory / "jdk")}, clear=True):
                with patch("validate_ids.subprocess.run", side_effect=run):
                    result = run_reports(directory, "1.21.1")
            self.assertIn("minecraft:item", result)
            self.assertEqual([java, "-version"], commands[0])
            self.assertEqual(java, commands[1][0])
            self.assertIn("-DbundlerMainClass=net.minecraft.data.Main", commands[1])

    def test_cached_reports_do_not_require_java(self):
        with tempfile.TemporaryDirectory() as temporary:
            directory = Path(temporary)
            reports = directory / "reports/reports"
            reports.mkdir(parents=True)
            (reports / "registries.json").write_text("{}")
            with patch("validate_ids.java_executable", side_effect=AssertionError("cached reports should not launch Java")):
                self.assertEqual({}, run_reports(directory, "1.21.1"))


if __name__ == "__main__":
    unittest.main()
