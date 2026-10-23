import hashlib
import json
import shutil
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
CACHE = ROOT / "tools" / "cache"
TC3_CACHE = Path.home() / "projects" / "thaumcraft-reforged" / "tools" / "cache"
MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
FORGE_MAVEN = "https://maven.minecraftforge.net"
FORGE_VERSION = "1.7.10-10.13.4.1614-1.7.10"
DOWNLOADS = {
    "mcp_stable-12-1.7.10.zip": f"{FORGE_MAVEN}/de/oceanlabs/mcp/mcp_stable/12-1.7.10/mcp_stable-12-1.7.10.zip",
    "mcp-1.7.10-srg.zip": f"{FORGE_MAVEN}/de/oceanlabs/mcp/mcp/1.7.10/mcp-1.7.10-srg.zip",
    "forge-universal.jar": f"{FORGE_MAVEN}/net/minecraftforge/forge/{FORGE_VERSION}/forge-{FORGE_VERSION}-universal.jar",
    "forge-userdev.jar": f"{FORGE_MAVEN}/net/minecraftforge/forge/{FORGE_VERSION}/forge-{FORGE_VERSION}-userdev.jar",
    "baubles.jar": "https://www.cursemaven.com/curse/maven/baubles-227083/2224857/baubles-227083-2224857.jar",
    "specialsource.jar": "https://repo1.maven.org/maven2/net/md-5/SpecialSource/1.11.4/SpecialSource-1.11.4-shaded.jar",
}


def curl(url):
    return subprocess.run(["curl", "-fsSL", url], check=True, capture_output=True).stdout


def download(url, target, sha1=None):
    if target.exists():
        return
    data = curl(url)
    if sha1 and hashlib.sha1(data).hexdigest() != sha1:
        raise RuntimeError(f"sha1 mismatch for {url}")
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(data)
    print(f"downloaded {target.relative_to(ROOT)}")


def fetch_minecraft(directory):
    manifest = json.loads(curl(MANIFEST_URL))
    url = next(version["url"] for version in manifest["versions"] if version["id"] == "1.7.10")
    version = json.loads(curl(url))
    client = version["downloads"]["client"]
    download(client["url"], directory / "client.jar", client["sha1"])
    for library in version["libraries"]:
        artifact = library.get("downloads", {}).get("artifact")
        if artifact:
            download(artifact["url"], directory / "libraries" / Path(artifact["path"]).name, artifact["sha1"])


def extract_mappings(directory):
    mappings = directory / "mcp1710"
    mappings.mkdir(exist_ok=True)
    with zipfile.ZipFile(directory / "mcp_stable-12-1.7.10.zip") as archive:
        for name in ("methods.csv", "fields.csv", "params.csv"):
            (mappings / name).write_bytes(archive.read(name))
    with zipfile.ZipFile(directory / "mcp-1.7.10-srg.zip") as archive:
        for name in ("joined.srg", "joined.exc"):
            (mappings / name).write_bytes(archive.read(name))


def check_baubles(path):
    with zipfile.ZipFile(path) as archive:
        info = json.loads(archive.read("mcmod.info"))
    entries = info if isinstance(info, list) else info.get("modList", [])
    print("baubles:", ", ".join(f"{entry['version']} ({entry.get('mcversion')})" for entry in entries))


def main():
    directory = CACHE / "1.7.10"
    directory.mkdir(parents=True, exist_ok=True)
    vineflower = CACHE / "vineflower.jar"
    if not vineflower.exists():
        shutil.copy(TC3_CACHE / "vineflower.jar", vineflower)
    fetch_minecraft(directory)
    for name, url in DOWNLOADS.items():
        download(url, directory / name)
    extract_mappings(directory)
    check_baubles(directory / "baubles.jar")


if __name__ == "__main__":
    main()
