import argparse
import json
import re
import shutil
from collections import Counter, defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SOURCE = ROOT / "reference" / "decompiled" / "assets"
SOURCE_THAUM = SOURCE / "thaumcraft"
LOGO = ROOT / "reference" / "decompiled" / "TCLogo.png"
RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / "thaumcraft"
LANG_EXTRA = ROOT / "tools" / "lang"
PENDING = ROOT / "tools" / "assets_pending"
ASSETS_EXTRA = ROOT / "tools" / "assets_extra"

TEXTURE_DIRS = {
    "blocks": "block",
    "items": "item",
    "models": "model",
    "misc": "misc",
    "gui": "gui",
    "aspects": "aspects",
    "foci": "foci",
}

SKIPPED_TEXTURES = {"blocks/es_i_1 - Copy.png"}

ENTITY_KEYS = {
    "SpecialItem": "special_item",
    "PermanentItem": "permanent_item",
    "FollowItem": "following_item",
    "AspectOrb": "aspect_orb",
    "FallingTaint": "falling_taint",
    "Alumentum": "alumentum",
    "PrimalOrb": "primal_orb",
    "FrostShard": "frost_shard",
    "Dart": "golem_dart",
    "PrimalArrow": "primal_arrow",
    "PechBlast": "pech_blast",
    "EldritchOrb": "eldritch_orb",
    "BottleTaint": "bottle_taint",
    "GolemOrb": "golem_orb",
    "ShockOrb": "shock_orb",
    "ExplosiveOrb": "explosive_orb",
    "Ember": "ember",
    "Golem": "golem",
    "GolemBobber": "golem_bobber",
    "TravelingTrunk": "traveling_trunk",
    "BrainyZombie": "brainy_zombie",
    "GiantBrainyZombie": "giant_brainy_zombie",
    "Wisp": "wisp",
    "Firebat": "firebat",
    "Pech": "pech",
    "Pech.1": "pech_mage",
    "Pech.2": "pech_stalker",
    "MindSpider": "mind_spider",
    "EldritchGuardian": "eldritch_guardian",
    "EldritchWarden": "eldritch_warden",
    "CultistKnight": "cultist_knight",
    "CultistCleric": "cultist_cleric",
    "CultistLeader": "cultist_leader",
    "CultistPortal": "cultist_portal",
    "EldritchGolem": "eldritch_golem",
    "EldritchCrab": "eldritch_crab",
    "InhabitedZombie": "inhabited_zombie",
    "ThaumSlime": "thaumic_slime",
    "TaintSpider": "taint_spider",
    "Taintacle": "taintacle",
    "TaintacleTiny": "taintacle_tiny",
    "TaintacleGiant": "taintacle_giant",
    "TaintSpore": "taint_spore",
    "TaintSwarmer": "taint_swarmer",
    "TaintSwarm": "taint_swarm",
    "TaintedVillager": "tainted_villager",
    "TaintedChicken": "tainted_chicken",
    "TaintedPig": "tainted_pig",
    "TaintedSheep": "tainted_sheep",
    "TaintedCow": "tainted_cow",
    "TaintedCreeper": "tainted_creeper",
}

ENTITY_PATTERN = re.compile(r"^entity\.Thaumcraft\.(.+)\.name$")
FORMAT_PATTERN = re.compile(r"%(?:(\d+)\$)?([A-Za-z%]|$)")
INVALID_PATH = re.compile(r"[^a-z0-9_.\-/]")

report = {
    "renamed": [],
    "warnings": [],
}


def warn(message):
    report["warnings"].append(message)
    print(f"warning: {message}")


def clean_name(name):
    cleaned = INVALID_PATH.sub("_", name.lower().replace(" - ", "_"))
    return re.sub(r"_+", "_", cleaned)


def snake_case(name):
    spaced = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", "_", name)
    return clean_name(spaced)


def remove(path):
    if path.is_dir():
        shutil.rmtree(path)
    elif path.exists():
        path.unlink()


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def clean_targets():
    for name in ("textures", "sounds", "lang", "sounds.json"):
        remove(ASSETS / name)
    remove(ASSETS / "models" / "obj")
    remove(PENDING)


def convert_mcmeta(source, destination):
    data = json.loads(source.read_text(encoding="utf-8"))
    animation = data.get("animation")
    if animation is not None:
        for frame in animation.get("frames", []):
            if isinstance(frame, dict) and set(frame) - {"index", "time"}:
                warn(f"mcmeta frame with unknown keys: {source}")
    write_json(destination, data)


def convert_obj(source, destination):
    result = []
    for line in source.read_text(encoding="utf-8", errors="replace").splitlines():
        if line.startswith("mtllib "):
            result.append("mtllib " + clean_name(line[len("mtllib "):].strip()))
        else:
            result.append(line)
    destination.write_text("\n".join(result) + "\n", encoding="utf-8")


def convert_mtl(source, destination):
    lines = source.read_text(encoding="utf-8", errors="replace").splitlines()
    result = [line for line in lines if not line.strip().startswith("map_")]
    destination.write_text("\n".join(result) + "\n", encoding="utf-8")


def convert_villager_textures():
    from PIL import Image
    meta = json.dumps({"villager": {"hat": "full"}}, indent=4) + "\n"
    human = ASSETS / "textures" / "entity" / "villager" / "profession"
    zombie = ASSETS / "textures" / "entity" / "zombie_villager" / "profession"
    human.mkdir(parents=True, exist_ok=True)
    zombie.mkdir(parents=True, exist_ok=True)
    for source_name, name in (("wizard", "wizard"), ("moneychanger", "banker")):
        image = Image.open(SOURCE_THAUM / "textures" / "models" / f"{source_name}.png").convert("RGBA")
        image.paste((0, 0, 0, 0), (32, 0, 64, 18))
        image.paste((0, 0, 0, 0), (30, 47, 64, 64))
        image.save(human / f"{name}.png")
        (human / f"{name}.png.mcmeta").write_text(meta)
        zombie_image = image.copy()
        zombie_image.paste((0, 0, 0, 0), (0, 0, 32, 18))
        zombie_image.save(zombie / f"{name}.png")
        (zombie / f"{name}.png.mcmeta").write_text(meta)


def convert_effect_icons():
    from PIL import Image

    atlas = Image.open(ASSETS / "textures" / "misc" / "potions.png").convert("RGBA")
    icons = {
        "taint_poison": (3, 1),
        "vis_exhaust": (5, 1),
        "warp_ward": (3, 2),
        "death_gaze": (4, 2),
        "sun_scorned": (6, 2),
        "thaumarhia": (7, 2),
        "unnatural_hunger": (7, 1),
        "blurred_vision": (5, 2),
        "infectious_vis_exhaust": (6, 1),
    }
    destination = ASSETS / "textures" / "mob_effect"
    destination.mkdir(parents=True, exist_ok=True)
    for name, (column, row) in icons.items():
        left = column * 18
        top = 198 + row * 18
        atlas.crop((left, top, left + 18, top + 18)).save(destination / f"{name}.png")


def convert_textures():
    counts = Counter()
    for old_dir, new_dir in TEXTURE_DIRS.items():
        source_root = SOURCE_THAUM / "textures" / old_dir
        for path in sorted(source_root.rglob("*")):
            if path.is_dir() or path.name == ".DS_Store":
                continue
            relative = path.relative_to(source_root).as_posix()
            if f"{old_dir}/{relative}" in SKIPPED_TEXTURES:
                continue
            name = clean_name(relative)
            if name != relative:
                report["renamed"].append(f"textures/{old_dir}/{relative} -> {name}")
            if old_dir == "models" and path.suffix in (".obj", ".mtl"):
                destination = ASSETS / "models" / "obj" / name
                destination.parent.mkdir(parents=True, exist_ok=True)
                if path.suffix == ".obj":
                    convert_obj(path, destination)
                else:
                    convert_mtl(path, destination)
                counts[f"{old_dir}/{path.suffix[1:]}"] += 1
                continue
            destination = ASSETS / "textures" / new_dir / name
            destination.parent.mkdir(parents=True, exist_ok=True)
            if path.suffix == ".mcmeta":
                convert_mcmeta(path, destination)
                counts[f"{old_dir}/mcmeta"] += 1
            elif path.suffix == ".png":
                shutil.copyfile(path, destination)
                counts[f"{old_dir}/png"] += 1
            else:
                warn(f"unexpected texture file skipped: {path}")
    return counts


def convert_sounds():
    source_dir = SOURCE_THAUM / "sounds"
    target = ASSETS / "sounds"
    target.mkdir(parents=True, exist_ok=True)
    copied = {}
    for path in sorted(source_dir.rglob("*.ogg")):
        relative = path.relative_to(source_dir).as_posix()
        name = snake_case(relative)
        if name != relative:
            report["renamed"].append(f"sounds/{relative} -> {name}")
        destination = target / name
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(path, destination)
        copied[relative[:-4]] = name[:-4]

    old = json.loads((SOURCE_THAUM / "sounds.json").read_text(encoding="utf-8"))
    result = {}
    referenced = set()
    for event, definition in old.items():
        new_event = snake_case(event)
        entries = []
        for sound in definition["sounds"]:
            if isinstance(sound, str):
                sound = {"name": sound}
            name = sound["name"]
            referenced.add(name)
            if name not in copied:
                warn(f"sound file missing: {name} (event {event})")
                continue
            entry = f"thaumcraft:{copied[name]}"
            options = {key: value for key, value in sound.items() if key != "name"}
            entries.append({"name": entry, **options} if options else entry)
        result[new_event] = {
            "sounds": entries,
            "subtitle": f"subtitles.thaumcraft.{new_event}",
        }
    for name in sorted(set(copied) - referenced):
        warn(f"sound file not referenced in sounds.json: {name}")
    write_json(ASSETS / "sounds.json", result)
    return len(copied), result


def load_id_map(path):
    if not path.exists():
        warn(f"id map not found: {path}, tile and item keys are not renamed")
        return None
    return json.loads(path.read_text(encoding="utf-8"))


def build_key_map(id_map):
    key_map = {}
    if id_map is None:
        return key_map
    block_ids = set()
    for entry in id_map["blocks"]:
        if entry.get("skip") or not entry.get("lang"):
            continue
        if "color" in entry.get("state", {}):
            key_map[entry["lang"]] = f"block.thaumcraft.{entry['id']}.{entry['state']['color']}"
            continue
        if entry["id"] in block_ids:
            key_map.setdefault(entry["lang"], None)
            continue
        key_map[entry["lang"]] = f"block.thaumcraft.{entry['id']}"
        block_ids.add(entry["id"])
    for entry in id_map["items"]:
        if entry.get("skip") or not entry.get("lang"):
            continue
        if entry["id"] in block_ids:
            key_map.setdefault(entry["lang"], None)
        else:
            key_map[entry["lang"]] = f"item.thaumcraft.{entry['id']}"
    key_map["item.ItemGolemCore.name"] = "item.thaumcraft.golem_core"
    return key_map


def skipped_keys(id_map):
    if id_map is None:
        return set()
    return {
        entry["lang"]
        for entry in id_map["blocks"] + id_map["items"]
        if entry.get("skip") and entry.get("lang")
    }


def unescape(value):
    return re.sub(r"\\(n|\\)", lambda match: "\n" if match.group(1) == "n" else "\\", value)


def escape_format(value, stats):
    def replace(match):
        index, kind = match.group(1), match.group(2)
        if kind in ("s", "%"):
            return match.group(0)
        stats["escaped"] += 1
        if kind == "d":
            return f"%{index}$s" if index else "%s"
        return "%%" + (f"{index}$" if index else "") + kind

    return FORMAT_PATTERN.sub(replace, value)


def read_lang(path):
    text = path.read_bytes().decode("utf-8-sig")
    entries = {}
    for number, line in enumerate(text.splitlines(), 1):
        stripped = line.strip()
        if not stripped or stripped.startswith("#"):
            continue
        if "=" not in line:
            warn(f"{path.name}:{number} line without '=': {stripped[:40]}")
            continue
        key, value = line.split("=", 1)
        key = key.strip()
        value = unescape(value)
        if key in entries and entries[key] != value:
            warn(f"{path.name}:{number} duplicate key {key}, last wins")
        entries[key] = value
    return entries


def convert_lang(id_map):
    if not (SOURCE_THAUM / "lang").is_dir():
        raise FileNotFoundError(f"Original TC4 languages not found: {SOURCE_THAUM / 'lang'}")
    key_map = build_key_map(id_map)
    dropped = skipped_keys(id_map)
    used_keys = set()
    prefixes = Counter()
    results = {}
    unmatched = defaultdict(list)
    escape_stats = Counter()
    entity_missing = set()
    for path in sorted((SOURCE_THAUM / "lang").glob("*.lang")):
        language = path.stem.lower()
        result = {}
        for key, value in read_lang(path).items():
            value = escape_format(value, escape_stats)
            if key in key_map:
                used_keys.add(key)
                if key_map[key] is not None:
                    result[key_map[key]] = value
                continue
            if key in dropped:
                continue
            entity = ENTITY_PATTERN.match(key)
            if entity and entity.group(1) in ENTITY_KEYS:
                result[f"entity.thaumcraft.{ENTITY_KEYS[entity.group(1)]}"] = value
                continue
            if entity:
                entity_missing.add(key)
            if id_map is not None and re.match(r"^(tile|item)\.", key) and key.endswith(".name"):
                unmatched[language].append(key)
            result[key] = value
            if language == "en_us":
                prefixes[".".join(key.split(".")[:2])] += 1
        extra_path = LANG_EXTRA / f"{language}.json"
        if extra_path.exists():
            for key, value in json.loads(extra_path.read_text(encoding="utf-8")).items():
                if value is None:
                    result.pop(key, None)
                else:
                    result[key] = value
        results[language] = result
        write_json(ASSETS / "lang" / f"{language}.json", dict(sorted(result.items())))
    for key in sorted(entity_missing):
        warn(f"entity key without mapping: {key}")
    uncovered = []
    if id_map is not None:
        uncovered = sorted(
            entry["lang"]
            for entry in id_map["blocks"] + id_map["items"]
            if entry.get("lang") and not entry.get("skip") and entry["lang"] not in used_keys
        )
    return results, prefixes, uncovered, unmatched, escape_stats


def convert_shaders():
    copied = []
    pairs = [
        (SOURCE_THAUM / "shader", PENDING / "thaumcraft" / "shader"),
        (SOURCE / "minecraft" / "shaders", PENDING / "minecraft" / "shaders"),
    ]
    for source_root, target_root in pairs:
        for path in sorted(source_root.rglob("*")):
            if path.is_dir():
                continue
            destination = target_root / path.relative_to(source_root)
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(path, destination)
            copied.append(destination.relative_to(PENDING).as_posix())
    return copied


def validate(results, sounds):
    for pattern in ("*.json", "*.mcmeta"):
        for path in ASSETS.rglob(pattern):
            json.loads(path.read_text(encoding="utf-8"))
    research = [key for key in results["ru_ru"] if key.startswith("tc.research_page.")]
    if len(research) != 309:
        warn(f"ru_ru tc.research_page.* count is {len(research)}, expected 309")
    for language in ("en_us", "ru_ru"):
        for event in sounds.values():
            if event["subtitle"] not in results[language]:
                warn(f"missing {language} subtitle {event['subtitle']}")
    for language, entries in results.items():
        for key, value in entries.items():
            if any(match.group(2) not in ("s", "%") for match in FORMAT_PATTERN.finditer(value)):
                warn(f"{language}: bad format in {key}")


def main():
    global RESOURCES, ASSETS, PENDING
    parser = argparse.ArgumentParser()
    parser.add_argument("--id-map", default=str(ROOT / "tools" / "id_map.json"))
    parser.add_argument("--output-root", type=Path, default=ROOT, help="project-layout output directory")
    parser.add_argument("--lang-only", action="store_true", help="regenerate languages without touching textures or sounds")
    arguments = parser.parse_args()
    RESOURCES = arguments.output_root / "src/main/resources"
    ASSETS = RESOURCES / "assets/thaumcraft"
    PENDING = arguments.output_root / "tools/assets_pending"
    if not (SOURCE_THAUM / "lang").is_dir():
        raise FileNotFoundError(f"Original TC4 assets not found: {SOURCE_THAUM}")
    if arguments.lang_only:
        results, *_ = convert_lang(load_id_map(Path(arguments.id_map)))
        print(f"languages: {len(results)}")
        return

    clean_targets()
    texture_counts = convert_textures()
    convert_effect_icons()
    sound_files, sounds = convert_sounds()
    id_map = load_id_map(Path(arguments.id_map))
    results, prefixes, uncovered, unmatched, escape_stats = convert_lang(id_map)
    shaders = convert_shaders()
    convert_villager_textures()
    shutil.copytree(ASSETS_EXTRA, ASSETS, dirs_exist_ok=True)
    RESOURCES.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(LOGO, RESOURCES / "TCLogo.png")
    validate(results, sounds)

    print("\n== textures ==")
    for category, count in sorted(texture_counts.items()):
        print(f"{category}: {count}")
    print("\n== sounds ==")
    print(f"ogg files: {sound_files}, events: {len(sounds)}")
    print("\n== lang ==")
    for language, entries in sorted(results.items()):
        print(f"{language}: {len(entries)} keys")
    print(f"format specifiers escaped: {escape_stats['escaped']}")
    print("\n== renamed ==")
    for line in report["renamed"]:
        print(line)
    print("\n== id_map lang keys without source entry ==")
    for key in uncovered:
        print(key)
    print(f"total: {len(uncovered)}")
    print("\n== en_us tile/item keys not covered by id_map ==")
    for key in unmatched.get("en_us", []):
        print(key)
    print(f"total: {len(unmatched.get('en_us', []))}")
    print("\n== en_us key prefixes left as is ==")
    for prefix, count in prefixes.most_common():
        print(f"{prefix}: {count}")
    print("\n== shaders copied to tools/assets_pending ==")
    for line in shaders:
        print(line)
    print(f"\nwarnings: {len(report['warnings'])}")


if __name__ == "__main__":
    main()
