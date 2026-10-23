import argparse
import io
import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import remap_srg

ROOT = Path(__file__).resolve().parent.parent
CACHE = ROOT / "tools" / "cache"
MC = CACHE / "1.7.10"
MAPPINGS = MC / "mcp1710"
REFERENCE = ROOT / "reference"
TC4_JAR = ROOT / "Thaumcraft-1.7.10-4.2.3.5.jar"
JAVA = str(Path(os.environ["JAVA_HOME"]) / "bin" / "java") if os.environ.get("JAVA_HOME") else "java"
VINEFLOWER = CACHE / "vineflower.jar"
VINEFLOWER_OPTIONS = ["--folder", "--log-level=warn", "--indent-string=    ", "--threads=8"]


def libraries():
    return sorted((MC / "libraries").glob("*.jar"))


def vineflower(source, target, externals):
    if target.exists():
        shutil.rmtree(target)
    target.mkdir(parents=True)
    command = [JAVA, "-Xmx4G", "-jar", str(VINEFLOWER), *VINEFLOWER_OPTIONS]
    command += [f"-e={path}" for path in externals]
    command += [str(source), str(target)]
    subprocess.run(command, check=True)


def client_srg():
    target = MC / "client-srg.jar"
    if not target.exists():
        subprocess.run([
            JAVA, "-jar", str(MC / "specialsource.jar"),
            "--in-jar", str(MC / "client.jar"),
            "--out-jar", str(target),
            "--srg-in", str(MAPPINGS / "joined.srg"),
        ], check=True)
    return target


def extract(archive, prefix, target):
    for name in archive.namelist():
        if name.startswith(prefix) and not name.endswith("/"):
            path = target / name[len(prefix):]
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(archive.read(name))


def decompile_mc():
    output = REFERENCE / "mc1710"
    srg = client_srg()
    vineflower(srg, output / "minecraft", libraries())
    for directory in ("forge", "forge-patches"):
        shutil.rmtree(output / directory, ignore_errors=True)
    with zipfile.ZipFile(MC / "forge-userdev.jar") as userdev:
        extract(userdev, "src/main/java/", output / "forge")
        for name in ("forgepatches.zip", "fmlpatches.zip"):
            with zipfile.ZipFile(io.BytesIO(userdev.read(name))) as patches:
                extract(patches, "", output / "forge-patches" / name.removesuffix("patches.zip"))
    for patch in (output / "forge-patches").rglob("*.patch"):
        patch.rename(patch.with_suffix(".java"))
    missing = remap_srg.remap([output], MAPPINGS)
    (output / "unmapped.txt").write_text("".join(f"{srg}\n" for srg in sorted(missing)), encoding="utf-8")
    for patch in (output / "forge-patches").rglob("*.java"):
        patch.rename(patch.with_suffix(".patch"))


def decompile_tc4():
    output = REFERENCE / "decompiled"
    externals = [client_srg(), MC / "forge-universal.jar", MC / "baubles.jar", *libraries()]
    vineflower(TC4_JAR, output, externals)
    missing = remap_srg.remap([output], MAPPINGS)
    (REFERENCE / "decompiled-unmapped.txt").write_text("".join(f"{srg}\n" for srg in sorted(missing)), encoding="utf-8")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("targets", nargs="+", choices=["mc1710", "tc4"])
    args = parser.parse_args()
    if "mc1710" in args.targets:
        decompile_mc()
    if "tc4" in args.targets:
        decompile_tc4()


if __name__ == "__main__":
    main()
