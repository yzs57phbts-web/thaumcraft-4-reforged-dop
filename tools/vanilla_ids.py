import argparse
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import mcformat

ROOT = Path(__file__).resolve().parent.parent
REFERENCE = ROOT / "reference"
OUTPUT = ROOT / "tools" / "vanilla_ids.json"
DECOMPILED = REFERENCE / "decompiled" / "thaumcraft"
FIXES = REFERENCE / "mc" / "net" / "minecraft" / "util" / "datafix" / "fixes"
MC1710 = REFERENCE / "mc1710" / "minecraft" / "net" / "minecraft"
REGISTRY_SOURCES = {
    "26.3": REFERENCE / "mc" / "net" / "minecraft" / "world",
    "1.21.1": REFERENCE / "1.21.1" / "mc" / "net" / "minecraft" / "world",
    "1.19.2": REFERENCE / "1.19.2" / "mc" / "net" / "minecraft" / "world",
}
WILDCARD_META = 32767
FILTER_ME = "%%FILTER_ME%%"
DYE_COLORS = [
    "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
    "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
]
REGISTRY_REPORT = Path.home() / "projects" / "thaumcraft-reforged" / "tools" / "cache" / "1.21.1" / "reports" / "reports" / "registries.json"
BASE_VERSION = 1802

PRE_FLATTEN_ITEM_RENAMES = {"minecraft:cooked_fished": "minecraft:cooked_fish"}

PAIR = re.compile(r'"(minecraft:[a-z0-9_]+)",\s*"(minecraft:[a-z0-9_]+)"')


def java_pairs(path, marker):
    text = path.read_text()
    start = text.index(marker)
    end = text.index(".build()", start)
    return dict(PAIR.findall(text[start:end]))


def renames():
    renamening = FIXES / "EntityTheRenameningFix.java"
    renamed_blocks = java_pairs(renamening, "RENAMED_BLOCKS")
    renamed_items = {**renamed_blocks, **java_pairs(renamening, "RENAMED_ITEMS")}
    dyes = java_pairs(FIXES / "DyeItemRenameFix.java", "RENAMED_IDS")
    return [
        (1475, {"minecraft:flowing_water": "minecraft:water", "minecraft:flowing_lava": "minecraft:lava"}, {}),
        (
            1490,
            {"minecraft:melon_block": "minecraft:melon"},
            {
                "minecraft:melon_block": "minecraft:melon",
                "minecraft:melon": "minecraft:melon_slice",
                "minecraft:speckled_melon": "minecraft:glistering_melon_slice",
            },
        ),
        (1510, renamed_blocks, renamed_items),
        (1800, {}, dyes),
        (
            1802,
            {
                "minecraft:stone_slab": "minecraft:smooth_stone_slab",
                "minecraft:sign": "minecraft:oak_sign",
                "minecraft:wall_sign": "minecraft:oak_wall_sign",
            },
            {"minecraft:stone_slab": "minecraft:smooth_stone_slab", "minecraft:sign": "minecraft:oak_sign"},
        ),
        (2680, {"minecraft:grass_path": "minecraft:dirt_path"}, {"minecraft:grass_path": "minecraft:dirt_path"}),
        (3692, {"minecraft:grass": "minecraft:short_grass"}, {"minecraft:grass": "minecraft:short_grass"}),
        (4541, {"minecraft:chain": "minecraft:iron_chain"}, {"minecraft:chain": "minecraft:iron_chain"}),
    ]


def apply_renames(value, steps, kind, limit, floor=0):
    for version, blocks, items in steps:
        if floor < version <= limit:
            value = (blocks if kind == "blocks" else items).get(value, value)
    return value


def legacy_registry(path):
    text = path.read_text()
    return {int(number): f"minecraft:{name}" for number, name in re.findall(r'addObject\(\s*(\d+),\s*"(\w+)"', text)}


def legacy_fields(path, owner):
    text = path.read_text()
    pattern = rf'public static final \w+ (\w+) = \(\w+\){owner}\.\w+\.getObject\("(\w+)"\)'
    return {f"{owner}s.{field}": f"minecraft:{name}" for field, name in re.findall(pattern, text)}


def block_state_data():
    text = (FIXES / "BlockStateData.java").read_text()
    constants = {}
    for name, body in re.findall(r"private static final Map<String, String> (\w+) = Map\.of\(([^)]*)\);", text):
        constants[name] = dict(zip(*[iter(re.findall(r'"([^"]*)"', body))] * 2))
    states = {}
    pattern = re.compile(r'register\(\s*(\d+),\s*create\(\s*"([^"]+)"(?:,\s*(Map\.of\([^)]*\)|\w+))?\s*\)', re.S)
    for number, name, properties in pattern.findall(text):
        if not properties:
            values = {}
        elif properties.startswith("Map.of"):
            values = dict(zip(*[iter(re.findall(r'"([^"]*)"', properties))] * 2))
        else:
            values = constants[properties]
        if name == FILTER_ME:
            name = "minecraft:skeleton_skull"
            values = {}
        states[int(number)] = (name, values)
    return states


def state_string(name, properties):
    if not properties:
        return name
    return f"{name}[{','.join(f'{key}={properties[key]}' for key in sorted(properties))}]"


def item_flattening():
    text = (FIXES / "ItemStackTheFlatteningFix.java").read_text()
    return dict(re.findall(r'map\.put\("(minecraft:[a-z0-9_]+\.\d+)", "(minecraft:[a-z0-9_]+)"\)', text))


def item_numeric_ids():
    text = (FIXES / "ItemIdFix.java").read_text()
    return {int(number): f"minecraft:{name}" for number, name in re.findall(r'map\.put\((\d+), "minecraft:(\w+)"\)', text)}


def build():
    steps = renames()
    block_numeric = legacy_registry(MC1710 / "block" / "Block.java")
    item_numeric = legacy_registry(MC1710 / "item" / "Item.java")
    block_item_ids = item_numeric_ids()
    for number, name in block_numeric.items():
        if number in block_item_ids:
            item_numeric[number] = name
    states = block_state_data()
    flattening = item_flattening()
    flattening_ids = {key.rsplit(".", 1)[0] for key in flattening}

    block_defaults = {}
    for number in sorted(states):
        block_defaults.setdefault(number >> 4, states[number])

    block_names = {}
    block_flatten = {}
    block_states = {}
    for number, legacy in sorted(block_numeric.items()):
        default = states.get(number << 4, block_defaults.get(number, (legacy, {})))
        base = apply_renames(default[0], steps, "blocks", BASE_VERSION)
        block_names[legacy] = base
        for meta in range(16):
            name, properties = states.get((number << 4) | meta, block_defaults.get(number, default))
            resolved = apply_renames(name, steps, "blocks", BASE_VERSION)
            if resolved != base:
                block_flatten[f"{legacy}:{meta}"] = resolved
                if properties:
                    block_states[f"{legacy}:{meta}"] = state_string(resolved, properties)

    def flatten_item(legacy, damage):
        legacy = PRE_FLATTEN_ITEM_RENAMES.get(legacy, legacy)
        if legacy in flattening_ids:
            name = flattening.get(f"{legacy}.{damage}", flattening.get(f"{legacy}.0"))
        else:
            name = legacy
        return apply_renames(name, steps, "items", BASE_VERSION)

    item_names = {}
    item_flatten = {}
    for number, legacy in sorted(item_numeric.items()):
        base = flatten_item(legacy, 0)
        item_names[legacy] = base
        for key in sorted(flattening):
            owner, damage = key.rsplit(".", 1)
            if owner != legacy:
                continue
            resolved = flatten_item(legacy, int(damage))
            if resolved != base:
                item_flatten[f"{legacy}:{damage}"] = resolved

    universe = set(block_names.values()) | set(block_flatten.values()) | set(item_names.values()) | set(item_flatten.values())
    profiles = {}
    for version, profile in mcformat.PROFILES.items():
        table = {}
        for value in sorted(universe):
            final = value
            for kind in ("blocks", "items"):
                final = apply_renames(final, steps, kind, profile.data_version, BASE_VERSION)
                if final != value:
                    break
            if final != value:
                table[value] = final
        profiles[version] = table

    mcp_fields = {
        **legacy_fields(MC1710 / "init" / "Blocks.java", "Block"),
        **legacy_fields(MC1710 / "init" / "Items.java", "Item"),
    }

    return {
        "numeric": {
            "blocks": {str(number): name for number, name in sorted(block_numeric.items())},
            "items": {str(number): name for number, name in sorted(item_numeric.items())},
        },
        "names": {"blocks": block_names, "items": item_names},
        "flatten": {"blocks": block_flatten, "items": item_flatten},
        "states": {"blocks": block_states},
        "mcp_fields": mcp_fields,
        "profiles": profiles,
    }


def load():
    return json.loads(OUTPUT.read_text())


def field_registry(version):
    world = REGISTRY_SOURCES[version]
    result = {}
    for kind, path in (("blocks", world / "level" / "block" / "Blocks.java"), ("items", world / "item" / "Items.java")):
        text = path.read_text()
        result[kind] = {f"minecraft:{field.lower()}" for field in re.findall(r"public static final \w+ ([A-Z0-9_]+) =", text)}
    if version == "26.3":
        references = world.parent / "references"
        for kind, name in (("blocks", "BlockItemIds"), ("items", "BlockItemIds"), ("items", "ItemIds")):
            text = (references / f"{name}.java").read_text()
            result[kind] |= {f"minecraft:{value}" for value in re.findall(r'create\("(\w+)"\)', text)}
            for suffix in re.findall(r'createSimpleColored\("(\w+)"\)', text):
                result[kind] |= {f"minecraft:{color}_{suffix}" for color in DYE_COLORS}
    if version == "1.21.1" and REGISTRY_REPORT.exists():
        report = json.loads(REGISTRY_REPORT.read_text())
        result["blocks"] = set(report["minecraft:block"]["entries"])
        result["items"] = set(report["minecraft:item"]["entries"])
    return result


def resolve(data, kind, legacy, meta, version):
    names = data["names"][kind]
    flatten = data["flatten"][kind]
    value = flatten.get(f"{legacy}:{meta}", names.get(legacy))
    if value is None:
        return None
    return data["profiles"][version].get(value, value)


def resolve_field(data, field, meta, version, as_item):
    legacy = data["mcp_fields"].get(field)
    if legacy is None:
        return None
    if field.startswith("Blocks."):
        if as_item and legacy in data["names"]["items"]:
            return resolve(data, "items", legacy, meta, version)
        return resolve(data, "blocks", legacy, meta, version)
    return resolve(data, "items", legacy, meta, version)


def check():
    data = load()
    registries = {version: field_registry(version) for version in mcformat.PROFILES}
    field_pattern = re.compile(r"\b(Blocks|Items)\.(\w+)")
    stack_pattern = re.compile(r"new ItemStack\(\s*(Blocks|Items)\.(\w+)\s*,\s*[^,()]+?\s*,\s*(\d+)\s*\)")
    set_block_pattern = re.compile(r"setBlock\([^;]*?\b(Blocks)\.(\w+)\s*,\s*(\d+)\s*,")
    fields = {}
    pairs = {}
    for path in sorted(DECOMPILED.rglob("*.java")):
        text = path.read_text()
        for owner, name in field_pattern.findall(text):
            fields.setdefault(f"{owner}.{name}", path)
        for owner, name, meta in stack_pattern.findall(text) + set_block_pattern.findall(text):
            if int(meta) != WILDCARD_META:
                pairs.setdefault((f"{owner}.{name}", int(meta)), path)
    problems = []
    for field, path in sorted(fields.items()):
        if field not in data["mcp_fields"]:
            problems.append(f"unknown field {field} ({path.relative_to(DECOMPILED)})")
    for (field, meta), path in sorted(pairs.items()):
        if field not in data["mcp_fields"]:
            continue
        as_item = data["mcp_fields"][field] in data["names"]["items"]
        for version in mcformat.PROFILES:
            value = resolve_field(data, field, meta, version, as_item)
            kind = "items" if as_item else "blocks"
            if value is None:
                problems.append(f"{version}: {field}:{meta} unresolved ({path.relative_to(DECOMPILED)})")
            elif value not in registries[version][kind]:
                problems.append(f"{version}: {field}:{meta} -> {value} missing in registry ({path.relative_to(DECOMPILED)})")
    for field in sorted(fields):
        if field not in data["mcp_fields"]:
            continue
        for version in mcformat.PROFILES:
            for as_item in (True, False):
                if not as_item and not field.startswith("Blocks."):
                    continue
                if as_item and field.startswith("Blocks."):
                    if data["mcp_fields"][field] not in data["names"]["items"]:
                        continue
                    if resolve_field(data, field, 0, version, True) not in registries[version]["items"]:
                        continue
                value = resolve_field(data, field, 0, version, as_item)
                kind = "items" if as_item else "blocks"
                if value is None:
                    problems.append(f"{version}: {field} unresolved")
                elif value not in registries[version][kind]:
                    problems.append(f"{version}: {field} -> {value} missing in {kind} registry")
    print(f"fields {len(fields)}, field+meta pairs {len(pairs)}")
    for problem in sorted(set(problems)):
        print(problem)
    print(f"uncovered {len(set(problems))}")
    return 1 if problems else 0


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check", action="store_true")
    arguments = parser.parse_args()
    if arguments.check:
        raise SystemExit(check())
    data = build()
    OUTPUT.write_text(json.dumps(data, indent=2, sort_keys=True, ensure_ascii=False) + "\n")
    for section, content in data.items():
        if all(isinstance(value, dict) for value in content.values()):
            print(section, {key: len(value) for key, value in content.items()})
        else:
            print(section, len(content))


if __name__ == "__main__":
    main()
