"""Round-trip original-source converters without modifying tracked outputs."""
import json
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA_OUTPUTS = [
    "src/main/java/thaumcraft/crafting/ConfigRecipes.java",
    "src/main/java/thaumcraft/research/ConfigResearch.java",
    "src/main/java/thaumcraft/client/render/armor/ArmorLayers.java",
]
LANG = Path("src/main/resources/assets/thaumcraft/lang")


def main():
    (ROOT / "build").mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="check-converters-", dir=ROOT / "build") as temporary:
        output = Path(temporary)
        converters = [
            ("convert_assets.py", "--lang-only"),
            ("convert_recipes.py",),
            ("convert_research.py",),
            ("convert_armor_models.py",),
        ]
        for name, *arguments in converters:
            result = subprocess.run(
                [sys.executable, "-X", "utf8", str(ROOT / "tools" / name),
                 *arguments, "--output-root", str(output)],
                cwd=ROOT, capture_output=True, encoding="utf-8",
            )
            if result.returncode:
                print(result.stdout + result.stderr)
                return result.returncode
        paths = [Path(path) for path in JAVA_OUTPUTS] + [Path("tools/tc_recipes.json")]
        actual_languages = {path.name for path in (output / LANG).glob("*.json")}
        expected_languages = {path.name for path in (ROOT / LANG).glob("*.json")}
        failures = []
        if actual_languages != expected_languages:
            failures.append("language file set")
        paths += [LANG / name for name in sorted(expected_languages)]
        for relative in paths:
            original = (ROOT / relative).read_text(encoding="utf-8")
            target = output / relative
            if not target.exists():
                failures.append(str(relative))
                continue
            generated = target.read_text(encoding="utf-8")
            if relative.suffix == ".json":
                same = json.loads(original) == json.loads(generated)
            else:
                same = original == generated
            if not same:
                failures.append(str(relative))
        for path in failures:
            print(f"DRIFT: {path}")
        print(f"Converters: {len(paths)} files checked, {len(failures)} differences")
        return bool(failures)


if __name__ == "__main__":
    sys.exit(main())
