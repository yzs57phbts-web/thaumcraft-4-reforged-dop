"""Apply reviewed local changes after the original TC4 conversion.

Overrides are keyed by recipe/research ID and guard the original statement's
hash. A changed reference must be reviewed instead of silently dropping fixes.
"""
import hashlib
import json
import re
from pathlib import Path

import mcformat

ROOT = Path(__file__).resolve().parent.parent
REGISTRATIONS = re.compile(r'^        TcRecipes\.register\(new \w+\("([^"\n]+)"[^\n]*\);', re.M)
RESEARCH = re.compile(r'^        new ResearchItem\("([^"\n]+)".*?\.registerResearchItem\(\);', re.M | re.S)


def canonical(text):
    # Ignore layout without changing whitespace inside Java string literals.
    return ''.join(re.findall(r'"(?:\\.|[^"\\])*"|\S', text))


def load(name):
    path = ROOT / "tools/conversion_overrides" / mcformat.gradle_version() / name
    return json.loads(path.read_text(encoding="utf-8")) if path.exists() else {}


def apply_java(name, java):
    if mcformat.gradle_version() == "1.21.1":
        java = re.sub(r'\bIdentifier\b', 'ResourceLocation', java)
    overrides = load(name + ".json")
    for section, pattern in (("registrations", REGISTRATIONS), ("research", RESEARCH)):
        for entry in overrides.get(section, []):
            matches = {match[1]: match for match in pattern.finditer(java)}
            key = entry["key"]
            if "after" in entry:
                if key in matches or entry["after"] not in matches:
                    raise ValueError(f"{name}: invalid insertion {key}")
                anchor = matches[entry["after"]]
                java = java[:anchor.end()] + "\n" + entry["java"] + java[anchor.end():]
            else:
                if key not in matches:
                    raise ValueError(f"{name}: missing override target {key}")
                match = matches[key]
                digest = hashlib.sha256(canonical(match[0]).encode("utf-8")).hexdigest()
                if digest != entry["expected_sha256"]:
                    raise ValueError(f"{name}: reference changed for {key}; review override")
                end = match.end()
                if entry["java"] is None and java[end:end + 1] == "\n":
                    end += 1
                java = java[:match.start()] + (entry["java"] or "") + java[end:]
    hooks = overrides.get("init_before_aspects", [])
    if hooks:
        anchor = "        TcRecipeAspects.register();"
        if java.count(anchor) != 1:
            raise ValueError(f"{name}: missing init hook anchor")
        java = java.replace(anchor, "".join(f"        {hook}\n" for hook in hooks) + anchor)
    return java


def apply_vanilla(entries):
    removed = set(load("vanilla_recipes.json").get("remove_ids", []))
    missing = removed - {entry["id"] for entry in entries}
    if missing:
        raise ValueError(f"vanilla recipe override targets missing: {sorted(missing)}")
    return [entry for entry in entries if entry["id"] not in removed]
