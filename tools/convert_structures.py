import re
import sys
from collections import Counter
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import vanilla_ids

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "reference" / "decompiled" / "thaumcraft" / "common" / "lib" / "world" / "WorldGenMound.java"
if not SOURCE.exists():
    SOURCE = Path.home() / "projects" / "thaumcraft-4-reforged" / "reference" / "decompiled" / "thaumcraft" / "common" / "lib" / "world" / "WorldGenMound.java"
OUTPUT = ROOT / "src" / "main" / "resources" / "thaumcraft" / "structures" / "mound.txt"
TAIL_MARKER = "ChestGenHooks info ="
STAIR_FACING = {0: "east", 1: "west", 2: "south", 3: "north"}

LADDER_FACING = {2: "north", 3: "south", 4: "west", 5: "east"}

CALL =re.compile(r"world\.setBlock\(i \+ (-?\d+), j \+ (-?\d+), k \+ (-?\d+), Blocks\.(\w+)(?:, (\d+), \d+)?\);")


def state_string(data, name, meta):
    block = vanilla_ids.resolve_field(data, f"Blocks.{name}", meta, "26.3", False)
    if block is None:
        raise SystemExit(f"unresolved block Blocks.{name}:{meta}")
    if name == "stone_stairs":
        half = "top" if meta & 4 else "bottom"
        return f"{block}[facing={STAIR_FACING[meta & 3]},half={half}]"
    if name == "ladder":
        return f"{block}[facing={LADDER_FACING[meta]}]"
    return block


def main():
    data = vanilla_ids.load()
    text = SOURCE.read_text(encoding="utf-8")
    text = text[: text.index(TAIL_MARKER)]
    lines = []
    counts = Counter()
    for match in CALL.finditer(text):
        x, y, z, name, meta = match.groups()
        state = state_string(data, name, int(meta or 0))
        counts[state] += 1
        lines.append(f"{x} {y} {z} {state}")
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"mound: {len(lines)} blocks")
    for state, count in counts.most_common():
        print(f"  {count} {state}")


if __name__ == "__main__":
    main()
