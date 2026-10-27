"""Keep the former pack-level tainted-land exclusions with the complete bbtt.30."""
import json
from pathlib import Path


def generate(output, profile):
    if profile != "1.21.1":
        return
    path = Path(output) / "data/thaumcraft/neoforge/biome_modifier/tainted_land_native_spawns.json"
    data = {
        "neoforge:conditions": [
            {"type": "neoforge:mod_loaded", "modid": "quark"},
            {"type": "neoforge:mod_loaded", "modid": "thaumcraft"},
        ],
        "type": "neoforge:remove_spawns",
        "biomes": "thaumcraft:tainted_land",
        "entity_types": ["quark:stoneling", "quark:toretoise", "thaumcraft:pech"],
    }
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
