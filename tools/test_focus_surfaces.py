import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parent.parent
JAVA_HOME = Path(os.environ["JAVA_HOME"])
with tempfile.TemporaryDirectory(prefix="thaumcraft-focus-surfaces-") as folder:
    sources = [ROOT / "src/main/java/thaumcraft/client/geom" / f"{name}.java" for name in ["QuadSink", "EldritchSurfaceGeometry", "ConnectedTextureGeometry"]]
    sources.append(ROOT / "tools/tests/ConnectedTextureGeometryTest.java")
    subprocess.run([str(JAVA_HOME / "bin/javac"), "-d", folder, *map(str, sources)], check=True)
    subprocess.run([str(JAVA_HOME / "bin/java"), "-cp", folder, "ConnectedTextureGeometryTest"], check=True)
