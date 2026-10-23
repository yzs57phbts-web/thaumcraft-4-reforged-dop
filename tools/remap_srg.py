import argparse
import csv
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DEFAULT_MAPPINGS = ROOT / "tools" / "cache" / "1.7.10" / "mcp1710"
SRG_PATTERN = re.compile(r"\b(func_\d+_[a-zA-Z_]+|field_\d+_[a-zA-Z_]+|p_i?\d+_\d+_)\b")


def load_mappings(directory):
    names = {}
    for file_name, key in (("methods.csv", "searge"), ("fields.csv", "searge"), ("params.csv", "param")):
        with open(directory / file_name, newline="", encoding="utf-8") as handle:
            for row in csv.DictReader(handle):
                names[row[key]] = row["name"]
    return names


def remap(source_dirs, mappings):
    names = load_mappings(mappings)
    missing = set()
    changed = 0
    files = [path for directory in source_dirs for path in Path(directory).rglob("*.java")]

    def replace(match):
        srg = match.group(1)
        if srg in names:
            return names[srg]
        missing.add(srg)
        return srg

    for path in files:
        text = path.read_text(encoding="utf-8")
        remapped = SRG_PATTERN.sub(replace, text)
        if remapped != text:
            path.write_text(remapped, encoding="utf-8")
            changed += 1

    kinds = {"func": 0, "field": 0, "p": 0}
    for srg in missing:
        kinds[srg.split("_", 1)[0]] += 1
    print(f"remapped {changed}/{len(files)} files, unmapped: {len(missing)} {kinds}")
    return missing


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("sources", nargs="+")
    parser.add_argument("--mappings", type=Path, default=DEFAULT_MAPPINGS)
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    missing = remap(args.sources, args.mappings)
    if args.report:
        args.report.write_text("".join(f"{srg}\n" for srg in sorted(missing)), encoding="utf-8")


if __name__ == "__main__":
    main()
