import hashlib
import json
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import conversion_overrides as overrides
import convert_armor_models as armor
import convert_assets as assets


class ConversionOverridesTests(unittest.TestCase):
    def test_replacement_removal_and_ordered_insertion(self):
        first = '        TcRecipes.register(new CrucibleRecipe("Old", ""));'
        digest = hashlib.sha256(overrides.canonical(first).encode()).hexdigest()
        replacement = first.replace('"Old"', '"Adapted"')
        addition = first.replace('"Old"', '"Added"')
        data = {"registrations": [
            {"key": "Old", "expected_sha256": digest, "java": replacement},
            {"key": "Added", "after": "Adapted", "java": addition},
            {"key": "Adapted", "expected_sha256": hashlib.sha256(overrides.canonical(replacement).encode()).hexdigest(), "java": None},
        ]}
        with patch.object(overrides, "load", return_value=data):
            self.assertEqual(addition + "\n", overrides.apply_java("test", first + "\n"))

    def test_changed_reference_stops_conversion(self):
        data = {"registrations": [{"key": "Old", "expected_sha256": "outdated", "java": None}]}
        with patch.object(overrides, "load", return_value=data):
            with self.assertRaisesRegex(ValueError, "reference changed"):
                overrides.apply_java("test", '        TcRecipes.register(new CrucibleRecipe("Old", ""));')

    def test_strings_preserve_significant_whitespace(self):
        self.assertNotEqual(overrides.canonical('f("a b")'), overrides.canonical('f("ab")'))
        self.assertEqual(overrides.canonical('f ( "a b" )'), overrides.canonical('f("a b")'))

    def test_removed_vanilla_ids_are_validated(self):
        with patch.object(overrides, "load", return_value={"remove_ids": ["removed"]}):
            self.assertEqual([{"id": "kept"}], overrides.apply_vanilla([{"id": "removed"}, {"id": "kept"}]))
            with self.assertRaisesRegex(ValueError, "targets missing"):
                overrides.apply_vanilla([{"id": "kept"}])

    def test_hat_is_a_root_sibling_and_parts_use_the_same_path(self):
        part = {"tex": (0, 0), "boxes": [], "pivot": (0, 0, 0), "rot": (0, 0, 0)}
        with patch.object(armor, "parse", return_value=({"Hood": part}, [("bipedHeadwear", "Hood", None)])):
            java = "\n".join(armor.layer_method("robe", "fixture"))
        self.assertIn('hat = root.addOrReplaceChild("hat"', java)
        self.assertIn('root.getChild("hat").getChild("hood")', java)
        self.assertNotIn('head.addOrReplaceChild("hat"', java)

    def test_language_overrides_preserve_name_and_remove_obsolete_key(self):
        build = Path(__file__).resolve().parent.parent / "build"
        build.mkdir(exist_ok=True)
        with tempfile.TemporaryDirectory(dir=build) as temporary:
            root = Path(temporary)
            (root / "source/lang").mkdir(parents=True)
            (root / "extra").mkdir()
            (root / "source/lang/ru_RU.lang").write_text("entity.CultistCleric.name=Old\nobsolete=Old\n", encoding="utf-8")
            extra = {"entity.thaumcraft.cultist_cleric": "Багровый маг", "obsolete": None, "tc.golem.alchemy.source": "Источник голема: %s (%s)"}
            (root / "extra/ru_ru.json").write_text(json.dumps(extra), encoding="utf-8")
            with patch.object(assets, "SOURCE_THAUM", root / "source"), patch.object(assets, "LANG_EXTRA", root / "extra"), patch.object(assets, "ASSETS", root / "output"):
                results, *_ = assets.convert_lang(None)
            self.assertEqual(extra["entity.thaumcraft.cultist_cleric"], results["ru_ru"]["entity.thaumcraft.cultist_cleric"])
            self.assertNotIn("obsolete", results["ru_ru"])
            self.assertEqual(extra["tc.golem.alchemy.source"], results["ru_ru"]["tc.golem.alchemy.source"])


if __name__ == "__main__":
    unittest.main()
