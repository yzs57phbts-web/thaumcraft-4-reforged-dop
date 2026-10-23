import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parent.parent
JAVA_HOME = Path(os.environ["JAVA_HOME"])
JOML_ROOT = Path(os.environ.get("GRADLE_USER_HOME", Path.home() / ".gradle")) / "caches/modules-2/files-2.1/org.joml/joml"
JOML = sorted(JOML_ROOT.glob("*/*/joml-*.jar"))[-1]
with tempfile.TemporaryDirectory(prefix="thaumcraft-golem-geometry-") as folder:
    sources = [ROOT / "src/main/java/thaumcraft/client/geom" / f"{name}.java" for name in ["GolemItemTransforms", "WandGeometry", "QuadSink", "FishingLineGeometry"]]
    sources.extend(ROOT / "tools/tests" / f"{name}.java" for name in ["GolemItemTransformsTest", "WandGeometryTest", "FishingLineGeometryTest"])
    subprocess.run([str(JAVA_HOME / "bin/javac"), "-cp", str(JOML), "-d", folder, *map(str, sources)], check=True)
    subprocess.run([str(JAVA_HOME / "bin/java"), "-cp", os.pathsep.join([folder, str(JOML)]), "GolemItemTransformsTest"], check=True)
    subprocess.run([str(JAVA_HOME / "bin/java"), "-cp", os.pathsep.join([folder, str(JOML)]), "WandGeometryTest"], check=True)
    subprocess.run([str(JAVA_HOME / "bin/java"), "-cp", os.pathsep.join([folder, str(JOML)]), "FishingLineGeometryTest"], check=True)
