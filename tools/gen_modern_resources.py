"""Preserve the 1.21.1 recipe/content additions when regenerating legacy resources."""
import json
import shutil
from pathlib import Path


def generate(output: Path, profile: str):
    if profile != "1.21.1":
        return
    source = Path(__file__).parent / "modern_resources" / profile
    for path in source.rglob("*"):
        if not path.is_file():
            continue
        relative = path.relative_to(source)
        destination = output / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        if "/tags/" in relative.as_posix() and destination.exists():
            current = json.loads(destination.read_text(encoding="utf-8"))
            addition = json.loads(path.read_text(encoding="utf-8"))
            for value in addition["values"]:
                if value not in current["values"]:
                    current["values"].append(value)
            destination.write_text(json.dumps(current, indent=2) + "\n", encoding="utf-8")
        else:
            shutil.copyfile(path, destination)
