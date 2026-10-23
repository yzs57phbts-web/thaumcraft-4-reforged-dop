import argparse
import json
import re
import sys
import zipfile
from collections import Counter, OrderedDict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DECOMPILED = ROOT / "reference" / "decompiled"
JAVA = DECOMPILED / "thaumcraft"
CONFIG = JAVA / "common" / "config"
LANG_PATH = DECOMPILED / "assets" / "thaumcraft" / "lang" / "en_US.lang"
SOUNDS_PATH = DECOMPILED / "assets" / "thaumcraft" / "sounds.json"
ID_MAP_PATH = ROOT / "tools" / "id_map.json"
INVENTORY_PATH = ROOT / "INVENTORY.md"
JAR_NAME = "Thaumcraft-1.7.10-4.2.3.5.jar"
ID_PATTERN = re.compile(r"^[a-z0-9_]+$")
NAME_KEY = re.compile(r"^(tile|item)\..+\.name$")
PLAN_NUMBERS = OrderedDict([
    ("blocks", 45),
    ("items", 111),
    ("research", 201),
    ("arcane", 84),
    ("arcane_shapeless", 5),
    ("crucible", 49),
    ("infusion", 63),
    ("infusion_enchantment", 24),
    ("smelting_bonus", 18),
    ("classes", 942),
])
ITEM_STACK_ARGUMENT = re.compile(r"new ItemStack\(\s*(?:par1|this|itemIn|p_150895_1_)\s*,\s*1\s*,\s*(\w+)\s*\)")


def read_text(path):
    return path.read_text(encoding="utf-8", errors="replace")


def read_lang():
    entries = OrderedDict()
    for line in read_text(LANG_PATH).splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        entries[key.strip()] = value
    return entries


def java_index():
    index = {}
    for path in JAVA.rglob("*.java"):
        index.setdefault(path.stem, path)
    return index


def statements(text):
    return [statement.strip() for statement in re.split(r";\s*\n", text)]


def method_body(text, name):
    match = re.search(r"\b" + name + r"\s*\([^)]*\)\s*(?:throws [\w, ]+)?\{", text)
    if not match:
        return None
    depth = 1
    position = match.end()
    while position < len(text) and depth:
        if text[position] == "{":
            depth += 1
        elif text[position] == "}":
            depth -= 1
        position += 1
    return text[match.end():position]


def snake(value):
    value = re.sub(r"([a-z0-9])([A-Z])", r"\1_\2", value)
    value = re.sub(r"[^A-Za-z0-9]+", "_", value).strip("_").lower()
    return value


def sub_metas(path, method):
    if path is None or not path.exists():
        return []
    body = method_body(read_text(path), method)
    if not body:
        return []
    metas = set()
    loops = {}
    for match in re.finditer(r"for \(int (\w+) = (\d+); \1 (<=|<) (\d+);", body):
        end = int(match.group(4)) + (1 if match.group(3) == "<=" else 0)
        loops[match.group(1)] = range(int(match.group(2)), end)
    for match in ITEM_STACK_ARGUMENT.finditer(body):
        argument = match.group(1)
        if argument.isdigit():
            metas.add(int(argument))
        elif argument in loops:
            metas.update(loops[argument])
    return sorted(metas)


def parse_blocks(index):
    text = read_text(CONFIG / "ConfigBlocks.java")
    created = {}
    registered = []
    for statement in statements(text):
        match = re.search(r"\b(\w+) = new (\w+)\(", statement)
        unlocalized = re.search(r"setUnlocalizedName\(\"(\w+)\"\)", statement)
        if match and unlocalized:
            created[match.group(1)] = (match.group(2), unlocalized.group(1))
        register = re.search(r"GameRegistry\.registerBlock\((\w+),\s*(?:(\w+)\.class,\s*)?\"(\w+)\"\)", statement)
        if register:
            registered.append((register.group(1), register.group(2), register.group(3)))
    blocks = []
    for field, item_class, registration in registered:
        block_class, unlocalized = created.get(field, (None, None))
        blocks.append(OrderedDict([
            ("field", field),
            ("class", block_class),
            ("item_class", item_class),
            ("unlocalized", unlocalized),
            ("old", registration),
            ("sub", sub_metas(index.get(block_class), "getSubBlocks")),
        ]))
    return blocks


def parse_items(index):
    text = read_text(CONFIG / "ConfigItems.java")
    created = {}
    registered = []
    for statement in statements(text):
        match = re.search(r"\b(\w+) = new (\w+)\(", statement)
        unlocalized = re.search(r"setUnlocalizedName\(\"(\w+)\"\)", statement)
        if match and unlocalized:
            created[match.group(1)] = (match.group(2), unlocalized.group(1))
        register = re.search(r"GameRegistry\.registerItem\((\w+),\s*\"(\w+)\"", statement)
        if register:
            registered.append((register.group(1), register.group(2)))
    items = []
    for field, registration in registered:
        item_class, unlocalized = created.get(field, (None, None))
        items.append(OrderedDict([
            ("field", field),
            ("class", item_class),
            ("unlocalized", unlocalized),
            ("old", registration),
            ("sub", sub_metas(index.get(item_class), "getSubItems")),
        ]))
    declared = re.findall(r"public static Item (\w+);", text)
    return items, declared


def lang_metas(lang, prefix, unlocalized):
    pattern = re.compile(r"^" + re.escape(prefix) + r"\." + re.escape(unlocalized) + r"\.(\d+)\.name$")
    return sorted(int(match.group(1)) for key in lang for match in [pattern.match(key)] if match)


def draft_id(lang, key, old, meta):
    if key and key in lang:
        name = lang[key].strip()
        name = re.sub(r"%\w+", "", name)
        if snake(name):
            return snake(name)
    return snake(old) + ("" if meta is None else "_" + str(meta))


def draft_rows(kind, entries, lang):
    prefix = "tile" if kind == "blocks" else "item"
    rows = []
    claimed = set()
    known = {}
    for entry in entries:
        unlocalized = entry["unlocalized"]
        known.setdefault(unlocalized, entry["old"])
        metas = sorted(set(lang_metas(lang, prefix, unlocalized)) | set(entry["sub"]))
        if not metas:
            key = f"{prefix}.{unlocalized}.name"
            lang_key = key if key in lang else None
            rows.append(make_row(kind, entry["old"], None, lang_key, lang))
            if lang_key:
                claimed.add(lang_key)
            continue
        for meta in metas:
            key = f"{prefix}.{unlocalized}.{meta}.name"
            lang_key = key if key in lang else None
            rows.append(make_row(kind, entry["old"], meta, lang_key, lang))
            if lang_key:
                claimed.add(lang_key)
        generic = f"{prefix}.{unlocalized}.name"
        if generic in lang:
            rows.append(make_row(kind, entry["old"], None, generic, lang))
            claimed.add(generic)
    for key in lang:
        if not NAME_KEY.match(key) or not key.startswith(prefix + ".") or key in claimed:
            continue
        body = key[len(prefix) + 1:-len(".name")]
        owner = None
        meta = None
        for unlocalized in sorted(known, key=len, reverse=True):
            if body == unlocalized or body.startswith(unlocalized + "."):
                owner = known[unlocalized]
                tail = body[len(unlocalized) + 1:]
                meta = int(tail) if tail.isdigit() else None
                break
        rows.append(make_row(kind, owner or body.split(".")[0], meta, key, lang))
        claimed.add(key)
    return rows


def make_row(kind, old, meta, lang_key, lang):
    row = OrderedDict([("old", old), ("meta", meta), ("lang", lang_key), ("id", draft_id(lang, lang_key, old, meta))])
    if kind == "blocks":
        row["state"] = {}
    row["skip"] = None
    return row


def merge_id_map(draft, existing):
    result = OrderedDict()
    for kind in ("blocks", "items"):
        rows = list(existing.get(kind, []))
        known_old = {row["old"] for row in rows}
        known_lang = {row["lang"] for row in rows if row["lang"]}
        for row in draft[kind]:
            if (row["lang"] and row["lang"] not in known_lang) or (not row["lang"] and row["old"] not in known_old):
                rows.append(row)
        result[kind] = rows
    return result


def write_id_map(blocks, items, lang):
    draft = OrderedDict([
        ("blocks", draft_rows("blocks", blocks, lang)),
        ("items", draft_rows("items", items, lang)),
    ])
    existing = json.loads(ID_MAP_PATH.read_text(encoding="utf-8")) if ID_MAP_PATH.exists() else {}
    merged = merge_id_map(draft, existing)
    ID_MAP_PATH.write_text(dump_id_map(merged), encoding="utf-8")
    added = sum(len(merged[kind]) for kind in merged) - sum(len(existing.get(kind, [])) for kind in merged)
    print(f"id_map.json: rows {sum(len(merged[kind]) for kind in merged)}, added {added}")


def dump_id_map(data):
    lines = ["{"]
    kinds = list(data)
    for kind_index, kind in enumerate(kinds):
        lines.append(f'  "{kind}": [')
        rows = data[kind]
        for row_index, row in enumerate(rows):
            suffix = "," if row_index < len(rows) - 1 else ""
            lines.append("    " + json.dumps(row, ensure_ascii=False) + suffix)
        lines.append("  ]" + ("," if kind_index < len(kinds) - 1 else ""))
    lines.append("}")
    return "\n".join(lines) + "\n"


def state_key(row):
    return json.dumps(row.get("state") or {}, sort_keys=True)


def check(blocks, items, lang):
    if not ID_MAP_PATH.exists():
        print("tools/id_map.json отсутствует")
        return 1
    data = json.loads(ID_MAP_PATH.read_text(encoding="utf-8"))
    errors = []
    covered = set()
    registered = {"blocks": {entry["old"] for entry in blocks}, "items": {entry["old"] for entry in items}}
    for kind in ("blocks", "items"):
        seen = {}
        used_old = set()
        for row in data.get(kind, []):
            label = f"{kind}:{row.get('old')}:{row.get('meta')}"
            missing = {"old", "meta", "lang", "id", "skip"} - set(row)
            if kind == "blocks" and "state" not in row:
                missing.add("state")
            if missing:
                errors.append(f"{label}: нет полей {sorted(missing)}")
                continue
            if row["meta"] is not None and not isinstance(row["meta"], int):
                errors.append(f"{label}: meta должен быть int или null")
            if row["lang"] is not None and row["lang"] not in lang:
                errors.append(f"{label}: ключа {row['lang']} нет в en_US.lang")
            if row["lang"] is not None:
                covered.add(row["lang"])
            if row["skip"] is not None and (not isinstance(row["skip"], str) or not row["skip"].strip()):
                errors.append(f"{label}: skip должен быть непустой строкой или null")
            used_old.add(row["old"])
            if row["skip"] is None:
                if not isinstance(row["id"], str) or not ID_PATTERN.match(row["id"]):
                    errors.append(f"{label}: некорректный id {row['id']!r}")
                    continue
                if row["old"] not in registered[kind]:
                    errors.append(f"{label}: old не найден среди регистраций")
                key = (row["id"], state_key(row) if kind == "blocks" else "")
                if key in seen:
                    errors.append(f"{label}: дубль id {row['id']} (уже у {seen[key]})")
                seen[key] = label
            elif row["id"] is not None and not ID_PATTERN.match(str(row["id"])):
                errors.append(f"{label}: некорректный id у skip-записи {row['id']!r}")
        for old in sorted(registered[kind] - used_old):
            errors.append(f"{kind}: регистрация {old} не покрыта ни одной записью")
        ids = {row["id"] for row in data.get(kind, []) if row.get("skip") is None and isinstance(row.get("id"), str)}
        print(f"{kind}: записей {len(data.get(kind, []))}, с id {sum(1 for row in data.get(kind, []) if row.get('skip') is None)}, skip {sum(1 for row in data.get(kind, []) if row.get('skip') is not None)}, уникальных id {len(ids)}")
    name_keys = [key for key in lang if NAME_KEY.match(key)]
    uncovered = [key for key in name_keys if key not in covered]
    print(f"lang: ключей tile/item .name {len(name_keys)}, покрыто {len(name_keys) - len(uncovered)}, всего tile/item {sum(1 for key in lang if key.startswith(('tile.', 'item.')))}")
    for key in uncovered:
        errors.append(f"lang не покрыт: {key}")
    for error in errors:
        print("ОШИБКА:", error)
    print("check:", "ошибок нет" if not errors else f"ошибок {len(errors)}")
    return 1 if errors else 0


def parse_tile_entities(index):
    text = read_text(CONFIG / "ConfigBlocks.java")
    return [(match.group(1), match.group(2)) for match in re.finditer(r"registerTileEntity\((\w+)\.class, \"(\w+)\"\)", text)]


def parse_entities():
    text = read_text(CONFIG / "ConfigEntities.java")
    eggs = {match.group(1): (int(match.group(2)), int(match.group(3))) for match in re.finditer(r"addMapping\(\"(\w+)\", (\d+), (\d+)\)", text)}
    entities = []
    for match in re.finditer(r"registerModEntity\((\w+)\.class, \"(\w+)\", id\+\+, Thaumcraft\.instance, (\d+), (\d+), (true|false)\)", text):
        entities.append(OrderedDict([
            ("class", match.group(1)),
            ("name", match.group(2)),
            ("range", int(match.group(3))),
            ("frequency", int(match.group(4))),
            ("velocity", match.group(5)),
            ("egg", eggs.get(match.group(2))),
        ]))
    return entities


def parse_gui():
    server = read_text(JAVA / "common" / "CommonProxy.java")
    client = read_text(JAVA / "client" / "ClientProxy.java")

    def collect(text, method, pattern):
        body = method_body(text, method) or ""
        result = OrderedDict()
        pending = []
        for line in body.splitlines():
            line = line.strip()
            case = re.match(r"case (\d+):", line)
            if case:
                pending.append(int(case.group(1)))
                continue
            returned = re.match(r"return new (" + pattern + r")\(", line)
            if returned:
                for number in pending:
                    result[number] = returned.group(1)
                pending = []
            elif line.startswith("return null") or line.startswith("default:"):
                for number in pending:
                    result.setdefault(number, None)
                pending = []
        return result

    return collect(server, "getServerGuiElement", r"Container\w+"), collect(client, "getClientGuiElement", r"Gui\w+")


def parse_packets():
    text = read_text(JAVA / "common" / "lib" / "network" / "PacketHandler.java")
    return [(match.group(1), match.group(2)) for match in re.finditer(r"registerMessage\((\w+)\.class, \w+\.class, idx\+\+, Side\.(\w+)\)", text)]


def parse_sounds():
    data = json.loads(read_text(SOUNDS_PATH))
    files = set()
    for event in data.values():
        for sound in event.get("sounds", []):
            files.add(sound["name"] if isinstance(sound, dict) else sound)
    ogg = sorted((DECOMPILED / "assets" / "thaumcraft" / "sounds").rglob("*.ogg"))
    return data, files, ogg


def loop_multipliers(lines):
    multipliers = []
    stack = []
    depth = 0
    for line in lines:
        multipliers.append(eval_stack(stack))
        loop = re.search(r"for \(int \w+ = (\d+); \w+ (<=|<) (\d+);", line)
        opened = line.count("{")
        closed = line.count("}")
        if loop and opened:
            count = int(loop.group(3)) + (1 if loop.group(2) == "<=" else 0) - int(loop.group(1))
            stack.append((depth + opened - closed, count))
        elif re.search(r"for \(\w+ \w+ : ", line) and opened:
            stack.append((depth + opened - closed, None))
        depth += opened - closed
        while stack and depth < stack[-1][0]:
            stack.pop()
    return multipliers


def eval_stack(stack):
    total = 1
    for _, count in stack:
        if count is None:
            return None
        total *= count
    return total


def parse_recipes():
    path = CONFIG / "ConfigRecipes.java"
    lines = read_text(path).splitlines()
    multipliers = loop_multipliers(lines)
    kinds = OrderedDict([
        ("arcane", r"ThaumcraftApi\.addArcaneCraftingRecipe\("),
        ("arcane_shapeless", r"ThaumcraftApi\.addShapelessArcaneCraftingRecipe\("),
        ("crucible", r"ThaumcraftApi\.addCrucibleRecipe\("),
        ("infusion", r"ThaumcraftApi\.addInfusionCraftingRecipe\("),
        ("infusion_enchantment", r"ThaumcraftApi\.addInfusionEnchantmentRecipe\("),
        ("smelting_bonus", r"ThaumcraftApi\.addSmeltingBonus\("),
        ("vanilla_shaped", r"(?:GameRegistry\.addRecipe\(|GameRegistry\.addShapedRecipe\(|CraftingManager\.getInstance\(\)\.addRecipe\(|(?<!IRecipe )\boreDictRecipe\()"),
        ("vanilla_shapeless", r"(?:GameRegistry\.addShapelessRecipe\(|CraftingManager\.getInstance\(\)\.addShapelessRecipe\(|(?<!IRecipe )\bshapelessOreDictRecipe\(|(?<!IRecipe )\bshapelessNBTOreRecipe\()"),
        ("vanilla_smelting", r"(?:FurnaceRecipes\.instance\(\)\.addSmeltingRecipe\(|GameRegistry\.addSmelting\()"),
    ])
    result = OrderedDict()
    for kind, pattern in kinds.items():
        calls = 0
        expanded = 0
        unknown = 0
        for number, line in enumerate(lines):
            if re.search(pattern, line):
                calls += 1
                multiplier = multipliers[number]
                if multiplier is None:
                    unknown += 1
                else:
                    expanded += multiplier
        result[kind] = {"calls": calls, "expanded": expanded, "unknown": unknown}
    return result


def parse_research():
    text = read_text(CONFIG / "ConfigResearch.java")
    categories = [match.group(1) for match in re.finditer(r"registerCategory\(\s*\"(\w+)\"", text)]
    items = [(match.group(1), match.group(2)) for match in re.finditer(r"new ResearchItem\(\s*\"(\w+)\",\s*\"(\w+)\"", text)]
    return categories, items


def parse_potions():
    text = read_text(CONFIG / "Config.java")
    return re.findall(r"(\w+)\.instance = new (Potion\w+)\((\w+), (true|false), (\d+)\)", text)


def parse_biomes():
    biomes = []
    for path in sorted((JAVA / "common" / "lib" / "world" / "biomes").glob("BiomeGen*.java")):
        text = read_text(path)
        name = re.search(r"biomeName = \"([^\"]+)\"|setBiomeName\(\"([^\"]+)\"\)", text)
        biomes.append((path.stem, (name.group(1) or name.group(2)) if name else ""))
    ids = dict(re.findall(r"public static int (biome\w+ID) = (\d+);", read_text(CONFIG / "Config.java")))
    return biomes, ids


def parse_wand_parts():
    text = read_text(CONFIG / "ConfigItems.java")
    caps = re.findall(r"WAND_CAP_\w+ = new WandCap\(\s*\"(\w+)\"", text)
    rods = re.findall(r"WAND_ROD_\w+ = new WandRod\(\"(\w+)\"", text)
    staffs = re.findall(r"STAFF_ROD_\w+ = new StaffRod\(\"(\w+)\"", text)
    return caps, rods, staffs


def package_key(parts):
    directories = parts[:-1]
    if len(directories) >= 3 and directories[0] == "thaumcraft" and directories[1] in ("common", "client"):
        return "/".join(directories[:3])
    if len(directories) == 2 and directories[0] == "thaumcraft":
        return "/".join(directories) + " (корень)"
    return "/".join(directories[:2])


def count_classes():
    jar = None
    for candidate in (ROOT / JAR_NAME, (ROOT / "reference").resolve().parent / JAR_NAME):
        if candidate.exists():
            jar = candidate
            break
    counter = Counter()
    if jar is None:
        for path in JAVA.rglob("*.java"):
            counter[package_key(path.relative_to(DECOMPILED).parts)] += 1
        for path in (DECOMPILED / "truetyper").rglob("*.java"):
            counter[package_key(path.relative_to(DECOMPILED).parts)] += 1
        return counter, "java-файлы декомпилята (без вложенных классов)", sum(counter.values())
    with zipfile.ZipFile(jar) as archive:
        names = [name for name in archive.namelist() if name.endswith(".class")]
    for name in names:
        counter[package_key(name.split("/"))] += 1
    return counter, f"классы из {jar.name}", len(names)


def references(name, own):
    total = 0
    for path in DECOMPILED.rglob("*.java"):
        if path.stem == own:
            continue
        if re.search(r"\b" + name + r"\b", read_text(path)):
            total += 1
    return total


def assigned(field):
    total = 0
    for path in JAVA.rglob("*.java"):
        if re.search(r"\b" + field + r"\s*=", read_text(path).replace("public static Item " + field + ";", "")):
            total += 1
    return total


def table(headers, rows):
    lines = ["| " + " | ".join(headers) + " |", "|" + "|".join("---" for _ in headers) + "|"]
    for row in rows:
        lines.append("| " + " | ".join(str(cell).replace("|", "\\|") if cell is not None else "" for cell in row) + " |")
    return "\n".join(lines)


def build_inventory(blocks, items, declared_items, lang):
    index = java_index()
    id_map = json.loads(ID_MAP_PATH.read_text(encoding="utf-8")) if ID_MAP_PATH.exists() else {"blocks": [], "items": []}
    tiles = parse_tile_entities(index)
    entities = parse_entities()
    server_gui, client_gui = parse_gui()
    packets = parse_packets()
    sounds, sound_files, ogg_files = parse_sounds()
    recipes = parse_recipes()
    categories, research = parse_research()
    potions = parse_potions()
    biomes, biome_ids = parse_biomes()
    caps, rods, staffs = parse_wand_parts()
    class_counter, class_source, class_total = count_classes()

    block_fields = re.findall(r"public static Block (\w+);", read_text(CONFIG / "ConfigBlocks.java"))
    unassigned = [field for field in declared_items if field not in {item["field"] for item in items}]
    research_per_category = Counter(category for _, category in research)

    out = []
    out.append("# Инвентарь контента Thaumcraft 4.2.3.5")
    out.append("")
    out.append("Файл генерируется скриптом `tools/inventory.py` по декомпилату `reference/decompiled/` и `tools/id_map.json`. Руками правится только `tools/id_map.json` (новые id и skip), остальное пересобирается командой `python3 tools/inventory.py`.")
    out.append("")

    out.append("## Сводка")
    out.append("")
    summary = [
        ("Блоки (`registerBlock`)", len(blocks)),
        ("Поля `public static Block` в ConfigBlocks", len(block_fields)),
        ("Предметы (`registerItem`)", len(items)),
        ("Поля `public static Item` в ConfigItems", len(declared_items)),
        ("Block entities (`registerTileEntity`)", len(tiles)),
        ("Сущности (`registerModEntity`)", len(entities)),
        ("Яйца призыва (`addMapping`)", sum(1 for entity in entities if entity["egg"])),
        ("Меню/контейнеры (серверные GUI id с контейнером)", sum(1 for value in server_gui.values() if value)),
        ("Пакеты (`registerMessage`)", len(packets)),
        ("Исследования (`new ResearchItem`)", len(research)),
        ("Вкладки исследований", len(categories)),
        ("События звука (`sounds.json`)", len(sounds)),
        ("Файлы `.ogg`", len(ogg_files)),
        ("Зелья", len(potions)),
        ("Биомы", len(biomes)),
        ("Ключи lang `tile.*`/`item.*`", sum(1 for key in lang if key.startswith(("tile.", "item.")))),
        ("Ключи lang `tile.*`/`item.*` `.name`", sum(1 for key in lang if NAME_KEY.match(key))),
        ("Записи id_map: блоки", len(id_map["blocks"])),
        ("Записи id_map: предметы", len(id_map["items"])),
        ("Классов всего", class_total),
    ]
    out.append(table(["Показатель", "Значение"], summary))
    out.append("")

    out.append("## Классы по пакетам")
    out.append("")
    out.append(f"Источник: {class_source}.")
    out.append("")
    out.append(table(["Пакет", "Классов"], sorted(class_counter.items(), key=lambda pair: (-pair[1], pair[0]))))
    out.append("")

    out.append("## Блоки")
    out.append("")
    out.append("Регистрации в `ConfigBlocks`. Колонка «meta-подтипов» это число записей `id_map.json` для блока.")
    out.append("")
    per_block = Counter(row["old"] for row in id_map["blocks"])
    block_rows = []
    for block in blocks:
        block_rows.append((block["old"], block["class"], block["item_class"] or "", block["unlocalized"], per_block.get(block["old"], 0)))
    out.append(table(["Регистрация", "Класс блока", "Класс ItemBlock", "Unlocalized", "Записей id_map"], block_rows))
    out.append("")
    out.append("### Блоки: meta в новые id")
    out.append("")
    block_names = []
    for row in id_map["blocks"]:
        name = lang.get(row["lang"], "").strip() if row["lang"] else ""
        state = json.dumps(row.get("state"), ensure_ascii=False) if row.get("state") else ""
        block_names.append((row["old"], row["meta"], name, row["id"] or "", state, row["skip"] or ""))
    out.append(table(["Старое имя", "meta", "Название (en)", "Новый id", "Состояние", "Skip"], block_names))
    out.append("")

    out.append("## Предметы")
    out.append("")
    per_item = Counter(row["old"] for row in id_map["items"])
    item_rows = []
    for item in items:
        item_rows.append((item["old"], item["class"], item["unlocalized"], per_item.get(item["old"], 0)))
    out.append(table(["Регистрация", "Класс", "Unlocalized", "Записей id_map"], item_rows))
    out.append("")
    out.append("### Предметы: meta в новые id")
    out.append("")
    item_names = []
    for row in id_map["items"]:
        name = lang.get(row["lang"], "").strip() if row["lang"] else ""
        item_names.append((row["old"], row["meta"], name, row["id"] or "", row["skip"] or ""))
    out.append(table(["Старое имя", "meta", "Название (en)", "Новый id", "Skip"], item_names))
    out.append("")
    out.append("### Части жезлов (`ConfigItems`)")
    out.append("")
    out.append(f"- Колпачки (`WandCap`): {', '.join(caps)}. Медный и серебряный создаются после обнаружения слитков других модов (`postInit`).")
    out.append(f"- Стержни жезлов (`WandRod`): {', '.join(rods)}.")
    out.append(f"- Стержни посохов (`StaffRod`): {', '.join(staffs)}, primal.")
    out.append("")

    out.append("## Block entities")
    out.append("")
    out.append(table(["Id в 1.7.10", "Класс"], [(identifier, cls) for cls, identifier in tiles]))
    out.append("")

    out.append("## Сущности")
    out.append("")
    entity_rows = []
    for number, entity in enumerate(entities):
        egg = f"{entity['egg'][0]:06X} / {entity['egg'][1]:06X}" if entity["egg"] else ""
        entity_rows.append((number, entity["name"], entity["class"], entity["range"], entity["frequency"], entity["velocity"], egg))
    out.append(table(["Мод-id", "Имя", "Класс", "Дальность", "Частота", "Скорость", "Яйцо (цвета)"], entity_rows))
    out.append("")
    out.append("Жители деревень: `entWizardId = 190`, `entBankerId = 191` (`VillagerRegistry`, компоненты `ComponentWizardTower`, `ComponentBankerHome`).")
    out.append("")

    out.append("## Меню и контейнеры")
    out.append("")
    gui_rows = []
    for number in sorted(set(server_gui) | set(client_gui)):
        gui_rows.append((number, server_gui.get(number) or "нет", client_gui.get(number) or "нет"))
    out.append(table(["GUI id", "Container (сервер)", "Gui (клиент)"], gui_rows))
    out.append("")

    out.append("## Рецепты")
    out.append("")
    names = OrderedDict([
        ("arcane", "Arcane shaped (`addArcaneCraftingRecipe`)"),
        ("arcane_shapeless", "Arcane shapeless"),
        ("crucible", "Тигель (`addCrucibleRecipe`)"),
        ("infusion", "Инфузия (`addInfusionCraftingRecipe`)"),
        ("infusion_enchantment", "Инфузионные зачарования"),
        ("smelting_bonus", "Бонусы переплавки (`addSmeltingBonus`)"),
        ("vanilla_shaped", "Ванильные shaped (включая oredict)"),
        ("vanilla_shapeless", "Ванильные shapeless (включая oredict и NBT)"),
        ("vanilla_smelting", "Ванильные плавки"),
    ])
    recipe_rows = []
    for kind, label in names.items():
        data = recipes[kind]
        recipe_rows.append((label, data["calls"], data["expanded"], data["unknown"]))
    out.append(table(["Тип", "Вызовов в коде", "С разворачиванием числовых циклов", "Вызовов в циклах по коллекции"], recipe_rows))
    out.append("")
    out.append("Часть вызовов лежит внутри циклов (краски, первичные аспекты, `Aspect.aspects.values()`), поэтому число рецептов в игре больше числа вызовов. Для портирования источник истины это сам код `ConfigRecipes.java`.")
    out.append("")

    out.append("## Исследования")
    out.append("")
    out.append(f"Вкладок: {len(categories)}, исследований: {len(research)}.")
    out.append("")
    research_rows = []
    for category in categories:
        keys = [key for key, owner in research if owner == category]
        research_rows.append((category, research_per_category.get(category, 0), ", ".join(keys)))
    out.append(table(["Вкладка", "Исследований", "Ключи"], research_rows))
    out.append("")

    out.append("## Звуки")
    out.append("")
    out.append(f"`sounds.json`: событий {len(sounds)}, ссылок на файлы {len(sound_files)}, `.ogg` на диске {len(ogg_files)}.")
    out.append("")
    out.append("События: " + ", ".join(f"`{name}`" for name in sounds) + ".")
    out.append("")

    out.append("## Пакеты")
    out.append("")
    out.append(table(["№", "Пакет", "Сторона получателя"], [(number, name, side) for number, (name, side) in enumerate(packets)]))
    out.append("")

    out.append("## Зелья, зачарования, биомы, измерения")
    out.append("")
    out.append("### Зелья")
    out.append("")
    out.append(table(["Класс", "Цвет", "Вредное"], [(cls, "0x%06X" % int(color), "да" if bad == "true" else "нет") for _, cls, _, bad, color in potions]))
    out.append("")
    out.append("### Зачарования")
    out.append("")
    out.append("Регистрируются два: `EnchantmentHaste` (вес 3) и `EnchantmentRepair` (вес 2); id 150 и 151 в конфиге. Классы `EnchantmentPotency`, `EnchantmentFrugal`, `EnchantmentWandFortune` нигде не используются (см. «Не переносим»). Инфузионные зачарования жезлов и инструментов (`addInfusionEnchantmentRecipe`) это отдельная система рецептов.")
    out.append("")
    out.append("### Биомы")
    out.append("")
    out.append(table(["Класс", "Название"], biomes))
    out.append("")
    out.append("Id по умолчанию в 1.7.10: " + ", ".join(f"{key} = {value}" for key, value in biome_ids.items()) + ". Они динамические и в порте не нужны.")
    out.append("")
    out.append("### Измерение")
    out.append("")
    out.append("Outer Lands: `WorldProviderOuter`, `ChunkProviderOuter`, лабиринт (`MazeGenerator`, `Gen*`), id измерения `-42` (`Config.dimensionOuterId`).")
    out.append("")

    out.append("## Решения по id для проверки")
    out.append("")
    decisions = [
        "Первичные аспекты названы по TC4 (`order`, `entropy`), TC3 использует `vis` и `dull`: `order_infused_stone`, `entropy_infused_stone`, `order_shard`, `entropy_shard`, `order_crystal_cluster`. Остальные общие id взяты из TC3 (`crucible`, `alembic`, `warded_jar`, `brain_jar`, `arcane_worktable`, `golem_*`, `top_hat` и т.д.).",
        "Meta, которые только меняют состояние, схлопнуты в blockstate: `golem_fetter` (meta 10, `active=true`), `item_grate` (meta 6, `closed=true`), `arcane_pressure_plate` (meta 3, `powered=true`), `table` (оси x и z), `research_table` и `research_table_part` (направление), `eldritch_obelisk` (meta 2, `part=top`), `loot_urn` и `loot_crate` (`tier`), двойные плиты (`type=double`).",
        "Знамя культа: один блок `banner` с состоянием `color`, ключи `tile.blockWoodenDevice.8.N.name` привязаны к нему. Цвет в оригинале лежит в `TileBanner`, поэтому вместо 16 отдельных блоков достаточно свойства или данных блока.",
        "Блок узла аур `aura_node` (blockAiry 0) и невидимые служебные блоки blockAiry названы по английским названиям. Блок для нитора назван `nitor_light`, чтобы не пересекаться с предметом `nitor` (ItemResource 1).",
        "Жезлы: один предмет `wand` (колпачок, стержень, жезл/скипетр/посох хранятся в NBT). Колпачки `*_wand_cap` и стержни `*_rod`, `*_staff_core` отдельные предметы по meta. Фокусы отдельные предметы `focus_*`, их апгрейды в данных.",
        "`thaumonomicon_cheat` (meta 42 книги) доступен только при `Config.allowCheatSheet` (по умолчанию выключено), это отладочный предмет. В id_map он сохранён, оставлять ли его решает человек.",
        "Самородки, кластеры и колпачки меди, олова, серебра и свинца в оригинале появляются в творческой вкладке только при наличии слитков других модов. `iron_nugget` пропущен (есть в ваниле), `copper_nugget` оставлен, хотя в 26.3 есть ванильный медный самородок.",
        "Яйца призыва: один предмет `ItemSpawnerEgg` с 29 meta разложен на `<entity>_spawn_egg`. Названия берутся из `entity.Thaumcraft.*.name`, ключей `item.*` для них нет.",
        "Ядра големов: в lang значение `item.ItemGolemCore.N.name` содержит только слово режима (`Fill`, `Gather`) без полного названия. Конвертеру ассетов нужно собрать название из общего `item.ItemGolemCore.name` и режима.",
        "Пустые мета-блоки `blockStoneDevice` 6 и 7, `blockEldritch` 6 и `blockMagicBox` помечены skip как недостижимые. Если при портировании генерации найдётся использование, поменять skip на id.",
    ]
    for decision in decisions:
        out.append("- " + decision)
    out.append("")

    out.append("## Не переносим")
    out.append("")
    photo = assigned("itemPhotoPlate")
    carpet = assigned("itemFlyingCarpet")
    dead = [
        ("WorldGenSilverwoodTreesOld", references("WorldGenSilverwoodTreesOld", "WorldGenSilverwoodTreesOld")),
        ("EnchantmentPotency", references("EnchantmentPotency", "EnchantmentPotency")),
        ("EnchantmentFrugal", references("EnchantmentFrugal", "EnchantmentFrugal")),
        ("EnchantmentWandFortune", references("EnchantmentWandFortune", "EnchantmentWandFortune")),
    ]
    skipped = [("blocks", row) for row in id_map["blocks"] if row["skip"]] + [("items", row) for row in id_map["items"] if row["skip"]]
    reasons = [
        ("`itemPhotoPlate`", f"Поле объявлено в `ConfigItems`, присваиваний {photo}, регистрации и класса нет. Камеры (`ThaumicCamera`) в этой версии тоже нет."),
        ("`itemFlyingCarpet`", f"Поле объявлено в `ConfigItems`, присваиваний {carpet}, регистрации и класса нет."),
        ("`WorldGenSilverwoodTreesOld`", f"Старый генератор дерева, ссылок из остального кода: {dead[0][1]}. Версия 4.2.3.2 вернула его только ради Thaumic Tinkerer (см. `changelog.txt`)."),
        ("`EnchantmentPotency`, `EnchantmentFrugal`, `EnchantmentWandFortune`", f"Классы-призраки, ссылок снаружи: {dead[1][1]}, {dead[2][1]}, {dead[3][1]}. Не регистрируются, из той же правки для Thaumic Tinkerer."),
        ("`thaumcraft/codechicken/core/launch/DepLoader`", "Coremod-загрузчик зависимостей CodeChicken. Не нужен: зависимости в современных загрузчиках описываются в метаданных мода."),
        ("`truetyper`", "Рендер TTF-шрифта для книги. Заменяется ванильным шрифтом `minecraft:uniform` (решение в PLAN.md)."),
        ("Встроенный `thaumcraft/codechicken/lib`", "Копия CodeChicken Lib (CCModel, OBJ, освещение, `Cuboid6`). Заменяется загрузчиком OBJ и ванильным рендером."),
        ("Код для Thaumic Tinkerer", "Отдельных классов нет: единственный след это возвращённый мёртвый код выше (`changelog.txt`, 4.2.3.2) и API-вызовы в `thaumcraft/api`."),
        ("`blockMagicBox` / `TileMagicBox` / `ContainerMagicBox`", "Блок зарегистрирован, но ни рецептов, ни генерации, ни творческой вкладки (`getSubBlocks` пуст), ни места, где он ставится."),
        ("Конфиг `Config.java` (id блоков, зелий, биомов, зачарований)", "Числовые id не нужны в современных реестрах. Остаются только настройки, которые решаются в фазе конфигурации."),
    ]
    out.append(table(["Что", "Причина"], reasons))
    out.append("")
    out.append("### Записи id_map со skip")
    out.append("")
    out.append(table(["Реестр", "Старое имя", "meta", "Lang", "Причина"], [(kind, row["old"], row["meta"], row["lang"] or "", row["skip"]) for kind, row in skipped]))
    out.append("")

    out.append("## Сверка с PLAN.md")
    out.append("")
    real = OrderedDict([
        ("blocks", len(blocks)),
        ("items", len(items)),
        ("research", len(research)),
        ("arcane", recipes["arcane"]["calls"]),
        ("arcane_shapeless", recipes["arcane_shapeless"]["calls"]),
        ("crucible", recipes["crucible"]["calls"]),
        ("infusion", recipes["infusion"]["calls"]),
        ("infusion_enchantment", recipes["infusion_enchantment"]["calls"]),
        ("smelting_bonus", recipes["smelting_bonus"]["calls"]),
        ("classes", class_total),
    ])
    labels = OrderedDict([
        ("blocks", "Блоки"),
        ("items", "Предметы"),
        ("research", "Исследования"),
        ("arcane", "Arcane shaped"),
        ("arcane_shapeless", "Arcane shapeless"),
        ("crucible", "Тигель"),
        ("infusion", "Инфузия"),
        ("infusion_enchantment", "Инфузионные зачарования"),
        ("smelting_bonus", "Бонусы переплавки"),
        ("classes", "Классы"),
    ])
    compare = []
    for key, plan in PLAN_NUMBERS.items():
        compare.append((labels[key], plan, real[key], "совпадает" if plan == real[key] else f"расхождение {real[key] - plan:+d}"))
    out.append(table(["Показатель", "PLAN.md", "Фактически", "Итог"], compare))
    out.append("")
    out.append("Пояснения к расхождениям:")
    out.append("")
    if unassigned:
        out.append(f"- Предметов 111 в PLAN.md это число полей `public static Item` в `ConfigItems` ({len(declared_items)}). Зарегистрировано {len(items)}. Не зарегистрированы и нигде не создаются: {', '.join('`' + field + '`' for field in unassigned)}.")
    else:
        out.append(f"- Предметов: полей {len(declared_items)}, регистраций {len(items)}.")
    out.append(f"- Блоков {len(blocks)} совпадает с числом полей `public static Block` ({len(block_fields)}). Число строк, содержащих `registerBlock` в файле, больше на 2 из-за вызова и объявления метода `registerBlocks`.")
    out.append("- Числа рецептов совпадают с числом вызовов `ThaumcraftApi.add*` в `ConfigRecipes`. Часть вызовов выполняется в циклах, реальное число рецептов в игре больше (см. таблицу «Рецепты»).")
    out.append("- 942 класса это все `.class` в jar, включая вложенные и анонимные (`$`). Java-файлов в декомпилате 859, из них 855 в `thaumcraft/` и 4 в `truetyper/`.")
    out.append("- PLAN.md упоминает «12 классов на `baubles/api`». В jar таких классов нет (`baubles.*` только в `dependancies.info` как внешняя зависимость), есть 7 классов предметов в `thaumcraft/common/items/baubles`, которые реализуют внешний `IBauble`.")
    out.append(f"- Звуков в PLAN.md 112, в `sounds.json` событий {len(sounds)} и ссылок на файлы {len(sound_files)}, `.ogg` на диске {len(ogg_files)}.")
    out.append("- Условные предметы: медные, оловянные, серебряные и свинцовые самородки/кластеры и колпачки (медь, серебро) показываются в творческой вкладке только при обнаружении слитков через oredict (`Config.foundCopperIngot` и т.п.). В `id_map.json` они сохранены, решение по ним нужно принять отдельно.")
    out.append("")
    return "\n".join(out) + "\n"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--write-id-map", action="store_true")
    parser.add_argument("--check", action="store_true")
    arguments = parser.parse_args()
    if not CONFIG.exists():
        print("Нет reference/decompiled, сначала tools/decompile.py")
        return 1
    lang = read_lang()
    index = java_index()
    blocks = parse_blocks(index)
    items, declared_items = parse_items(index)
    if arguments.write_id_map:
        write_id_map(blocks, items, lang)
        return 0
    if arguments.check:
        return check(blocks, items, lang)
    INVENTORY_PATH.write_text(build_inventory(blocks, items, declared_items, lang), encoding="utf-8")
    print(f"INVENTORY.md: блоков {len(blocks)}, предметов {len(items)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
