import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CONFIG = ROOT / "reference" / "decompiled" / "thaumcraft" / "common" / "config"
ASPECTS_SOURCE = CONFIG / "ConfigAspects.java"
OUTPUT = ROOT / "src" / "main" / "java" / "thaumcraft" / "aspect" / "ConfigAspects.java"
ITEMS_SOURCE = ROOT / "reference" / "mc" / "net" / "minecraft" / "world" / "item" / "Items.java"
PROFILE = "26.3"
WILDCARD = 32767

ORE_DICT = {
    "stone": "minecraft:stone",
    "cobblestone": "minecraft:cobblestone",
    "logWood": "#minecraft:logs",
    "plankWood": "#minecraft:planks",
    "slabWood": "#minecraft:wooden_slabs",
    "stairWood": "#minecraft:wooden_stairs",
    "stickWood": "#c:rods/wooden",
    "treeSapling": "#minecraft:saplings",
    "treeLeaves": "#minecraft:leaves",
    "oreLapis": "#c:ores/lapis",
    "oreDiamond": "#c:ores/diamond",
    "gemDiamond": "#c:gems/diamond",
    "oreRedstone": "#c:ores/redstone",
    "oreEmerald": "#c:ores/emerald",
    "gemEmerald": "#c:gems/emerald",
    "oreQuartz": "#c:ores/quartz",
    "nuggetIron": "#c:nuggets/iron",
    "oreIron": "#c:ores/iron",
    "dustIron": "#c:dusts/iron",
    "oreGold": "#c:ores/gold",
    "dustGold": "#c:dusts/gold",
    "dustRedstone": "#c:dusts/redstone",
    "dustGlowstone": "#c:dusts/glowstone",
    "glowstone": "minecraft:glowstone",
}
for metal in ("copper", "tin", "silver", "lead"):
    ORE_DICT[f"nugget{metal.capitalize()}"] = f"#c:nuggets/{metal}"
    ORE_DICT[f"ingot{metal.capitalize()}"] = f"#c:ingots/{metal}"
    ORE_DICT[f"dust{metal.capitalize()}"] = f"#c:dusts/{metal}"
    ORE_DICT[f"ore{metal.capitalize()}"] = f"#c:ores/{metal}"

DYES = [
    ("dyeBlack", "black"), ("dyeRed", "red"), ("dyeGreen", "green"), ("dyeBrown", "brown"),
    ("dyeBlue", "blue"), ("dyePurple", "purple"), ("dyeCyan", "cyan"), ("dyeLightGray", "light_gray"),
    ("dyeGray", "gray"), ("dyePink", "pink"), ("dyeLime", "lime"), ("dyeYellow", "yellow"),
    ("dyeLightBlue", "light_blue"), ("dyeMagenta", "magenta"), ("dyeOrange", "orange"), ("dyeWhite", "white"),
]
for old, color in DYES:
    ORE_DICT[old] = f"#c:dyes/{color}"

NO_ITEM_BLOCKS = {
    "Blocks.fire", "Blocks.water", "Blocks.lava", "Blocks.portal", "Blocks.end_portal",
    "Blocks.lit_redstone_ore", "Blocks.lit_furnace",
}

REPORT = []


def load_json(name):
    return json.loads((ROOT / "tools" / name).read_text(encoding="utf-8"))


VANILLA = load_json("vanilla_ids.json")
ID_MAP = load_json("id_map.json")
ENTITY_IDS = load_json("entity_ids.json")


def item_ids():
    text = ITEMS_SOURCE.read_text(encoding="utf-8")
    ids = {name.lower() for name in re.findall(r"public static final Item (\w+) =", text)}
    colors = [
        "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black",
    ]
    for name in re.findall(r"public static final ColorCollection<Item> (\w+) =", text):
        suffix = name.lower().removeprefix("dyed_")
        ids.update(f"{color}_{suffix}" for color in colors)
    return ids


ITEM_IDS = item_ids()


def declarations(path, pattern):
    return dict(re.findall(pattern, path.read_text(encoding="utf-8")))


ITEM_FIELDS = declarations(CONFIG / "ConfigItems.java", r"GameRegistry\.registerItem\((\w+),\s*\"(\w+)\"")
ITEM_FIELDS = {field: registered for field, registered in ITEM_FIELDS.items()}
BLOCK_FIELDS = declarations(CONFIG / "ConfigBlocks.java", r"(\w+) = new \w+\([^;]*?\)\.setUnlocalizedName\(\"(\w+)\"\)")


def profile_id(identifier):
    return VANILLA["profiles"][PROFILE].get(identifier, identifier)


def vanilla_ids(reference, meta):
    kind = "blocks" if reference.startswith("Blocks.") else "items"
    base = VANILLA["mcp_fields"].get(reference)
    if base is None:
        return None
    flatten = VANILLA["flatten"][kind]
    names = VANILLA["names"][kind]
    if meta == WILDCARD:
        result = []
        if base in names:
            result.append(names[base])
        for key, value in flatten.items():
            if key.startswith(base + ":") and key.rsplit(":", 1)[0] == base:
                result.append(value)
    else:
        value = flatten.get(f"{base}:{meta}") or names.get(base)
        result = [value] if value else []
    unique = []
    for value in result:
        value = profile_id(value)
        if value not in unique:
            unique.append(value)
    return unique


def thaumcraft_ids(reference, meta):
    field = reference.split(".", 1)[1]
    if reference.startswith("ConfigItems."):
        old = ITEM_FIELDS.get(field)
        rows = ID_MAP["items"]
    else:
        old = BLOCK_FIELDS.get(field, field)
        rows = ID_MAP["blocks"]
    if old is None:
        return None
    matching = [row for row in rows if row["old"] == old]
    if not matching:
        return None
    if meta == WILDCARD:
        selected = matching
    else:
        selected = [row for row in matching if row["meta"] == meta]
        if not selected:
            selected = [row for row in matching if row["meta"] is None]
    result = []
    for row in selected:
        if row["id"] and f"thaumcraft:{row['id']}" not in result:
            result.append(f"thaumcraft:{row['id']}")
    return result


def split_arguments(text):
    arguments = []
    depth = 0
    current = []
    in_string = False
    for character in text:
        if character == '"':
            in_string = not in_string
        if not in_string:
            if character in "({":
                depth += 1
            elif character in ")}":
                depth -= 1
            elif character == "," and depth == 0:
                arguments.append("".join(current).strip())
                current = []
                continue
        current.append(character)
    if "".join(current).strip():
        arguments.append("".join(current).strip())
    return arguments


def statements(text, names):
    pattern = re.compile(r"ThaumcraftApi\.(" + "|".join(names) + r")\(")
    position = 0
    while True:
        match = pattern.search(text, position)
        if not match:
            return
        depth = 1
        index = match.end()
        in_string = False
        while depth:
            character = text[index]
            if character == '"':
                in_string = not in_string
            elif not in_string:
                if character == "(":
                    depth += 1
                elif character == ")":
                    depth -= 1
            index += 1
        yield match.group(1), split_arguments(text[match.end():index - 1]), text.count("\n", 0, match.start()) + 1
        position = index


STACK = re.compile(r"new ItemStack\(\s*([\w.]+)\s*(?:,\s*([\w]+))?\s*(?:,\s*(\d+))?\s*\)$")
OPERATION = re.compile(r"\.(add|remove|merge)\(Aspect\.(\w+)(?:,\s*(\d+))?\)")


def resolve_stack(expression, line):
    match = STACK.match(expression.strip())
    if not match:
        REPORT.append(f"L{line}: cannot parse target {expression}")
        return None
    reference, second, third = match.groups()
    meta = int(third) if third is not None else 0
    if reference in NO_ITEM_BLOCKS:
        REPORT.append(f"L{line}: {reference} has no item form, skipped")
        return None
    if reference.startswith("Blocks.") or reference.startswith("Items."):
        result = vanilla_ids(reference, meta)
    else:
        result = thaumcraft_ids(reference, meta)
    if not result:
        REPORT.append(f"L{line}: unresolved {reference} meta {meta}")
        return None
    checked = []
    for identifier in result:
        namespace, name = identifier.split(":", 1)
        if namespace == "minecraft" and name not in ITEM_IDS:
            if "wall_" in name:
                continue
            REPORT.append(f"L{line}: {identifier} is not an item in {PROFILE}, dropped")
            continue
        checked.append(identifier)
    return checked or None


def java_target(targets):
    if len(targets) == 1:
        return f'"{targets[0]}"'
    return "List.of(" + ", ".join(f'"{target}"' for target in targets) + ")"


def convert_aspects(expression, line):
    expression = expression.strip()
    chain = expression
    if expression.startswith("new AspectList()"):
        result = "tags()"
        chain = expression[len("new AspectList()"):]
    else:
        match = re.match(r"new AspectList\((new ItemStack\([^()]*\))\)", expression)
        if not match:
            REPORT.append(f"L{line}: cannot parse aspects {expression}")
            return None
        source = resolve_stack(match.group(1), line)
        if source is None:
            return None
        result = f'copy("{source[0]}")'
        chain = expression[match.end():]
    for method, aspect, amount in OPERATION.findall(chain):
        if amount:
            result += f".{method}(Aspect.{aspect}, {amount})"
        else:
            result += f".{method}(Aspect.{aspect})"
    return result


def convert_items(text):
    start = text.index("private static void registerItemAspects()")
    body = text[start:]
    cut_start = body.index("Map lhm = new LinkedHashMap();")
    cut_end = body.index("ThaumcraftApi.registerComplexObjectTag(new ItemStack(ConfigBlocks.blockTable)")
    body = body[:cut_start] + body[cut_end:]
    lines = []
    for method, arguments, line in statements(body, ["registerObjectTag", "registerComplexObjectTag"]):
        call = "complex" if method == "registerComplexObjectTag" else "register"
        target = arguments[0]
        metas = None
        if len(arguments) == 3:
            metas = [int(value) for value in re.findall(r"\d+", arguments[1])]
            aspects_expression = arguments[2]
        else:
            aspects_expression = arguments[1]
        if target.startswith('"'):
            name = target.strip('"')
            targets = [ORE_DICT[name]] if name in ORE_DICT else None
            if name == "stone":
                targets = vanilla_ids("Blocks.stone", WILDCARD)
            if targets is None:
                REPORT.append(f"L{line}: unmapped oredict {name}")
                continue
        elif target == "dyes[i]":
            converted = convert_aspects(aspects_expression, line)
            for old, color in DYES:
                lines.append(f'        register("#c:dyes/{color}", {converted});')
            continue
        elif metas:
            reference = STACK.match(target).group(1)
            targets = resolve_stack(f"new ItemStack({reference}, 1, {WILDCARD})", line)
            if not targets:
                continue
        else:
            targets = resolve_stack(target, line)
            if targets is None:
                continue
        converted = convert_aspects(aspects_expression, line)
        if converted is None:
            continue
        lines.append(f"        {call}({java_target(targets)}, {converted});")
    return lines


NBT_VALUE = {"powered": "1"}


def convert_entities(text):
    start = text.index("private static void registerEntityAspects()")
    end = text.index("private static void registerItemAspects()")
    body = text[start:end]
    lines = []
    for method, arguments, line in statements(body, ["registerEntityTag"]):
        name = arguments[0].strip('"')
        aspects_expression = arguments[1]
        nbt = arguments[2:]
        if name.startswith("Thaumcraft."):
            identifier = ENTITY_IDS["thaumcraft"].get(name)
        else:
            identifier = ENTITY_IDS["vanilla"].get(name)
        if identifier is None:
            REPORT.append(f"L{line}: unmapped entity {name}")
            continue
        if name == "Thaumcraft.Wisp":
            lines.append("        for (Aspect aspect : Aspect.values()) {")
            lines.append(
                '            entity("thaumcraft:wisp", tags().add(aspect, 2).add(Aspect.MAGIC, 1).add(Aspect.AIR, 1), "Type", aspect.getTag());'
            )
            lines.append("        }")
            continue
        converted = convert_aspects(aspects_expression, line)
        pairs = []
        for entry in nbt:
            match = re.search(r'EntityTagsNBT\("(\w+)",\s*(?:\(byte\))?(\w+)\)', entry)
            key, value = match.groups()
            if name == "Skeleton" and key == "SkeletonType":
                identifier = "minecraft:wither_skeleton"
                continue
            pairs.append(f'"{key}", {value}')
        suffix = (", " + ", ".join(pairs)) if pairs else ""
        lines.append(f'        entity("{identifier}", {converted}{suffix});')
    return lines


def main():
    text = ASPECTS_SOURCE.read_text(encoding="utf-8")
    entity_lines = convert_entities(text)
    item_lines = convert_items(text)
    java = f"""package thaumcraft.aspect;

import java.util.List;

public final class ConfigAspects extends AspectRegistrar {{
    public ConfigAspects(AspectRegistry registry) {{
        super(registry);
    }}

    public void registerAll() {{
        registerItemAspects();
        registerEntityAspects();
    }}

    private void register(List<String> targets, AspectList aspects) {{
        for (String target : targets) {{
            register(target, aspects);
        }}
    }}

    private void complex(List<String> targets, AspectList aspects) {{
        for (String target : targets) {{
            complex(target, aspects);
        }}
    }}

    private void registerItemAspects() {{
{chr(10).join(item_lines)}
    }}

    private void registerEntityAspects() {{
{chr(10).join(entity_lines)}
    }}
}}
"""
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(java)
    print(f"items: {len(item_lines)} lines, entities: {len(entity_lines)} lines")
    print(f"report entries: {len(REPORT)}")
    for entry in REPORT:
        print("  " + entry)


if __name__ == "__main__":
    sys.exit(main())
