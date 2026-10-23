import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parent.parent
JAVA_HOME = Path(os.environ["JAVA_HOME"])
with tempfile.TemporaryDirectory(prefix="thaumcraft-eldritch-geometry-") as folder:
    sources = [ROOT / "src/main/java/thaumcraft/client/geom" / name for name in ["QuadSink.java", "EldritchSurfaceGeometry.java"]]
    sources.append(ROOT / "tools/tests/EldritchSurfaceGeometryTest.java")
    subprocess.run([str(JAVA_HOME / "bin/javac"), "-d", folder, *map(str, sources)], check=True)
    subprocess.run([str(JAVA_HOME / "bin/java"), "-cp", folder, "EldritchSurfaceGeometryTest"], check=True)
