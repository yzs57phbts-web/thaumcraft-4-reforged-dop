import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parent.parent
JAVA_HOME = Path(os.environ["JAVA_HOME"])
JOML_ROOT = Path(os.environ.get("GRADLE_USER_HOME", Path.home() / ".gradle")) / "caches/modules-2/files-2.1/org.joml/joml"
JOML = sorted(JOML_ROOT.glob("*/*/joml-*.jar"))[-1]
with tempfile.TemporaryDirectory(prefix="thaumcraft-node-geometry-") as folder:
    sources = [ROOT / "src/main/java/thaumcraft/client/geom" / f"{name}.java" for name in ["QuadSink", "FacingQuads", "NodeGeometry"]]
    sources.append(ROOT / "tools/tests/NodeGeometryTest.java")
    subprocess.run([str(JAVA_HOME / "bin/javac"), "-cp", str(JOML), "-d", folder, *map(str, sources)], check=True)
    subprocess.run([str(JAVA_HOME / "bin/java"), "-cp", os.pathsep.join([folder, str(JOML)]), "NodeGeometryTest"], check=True)
