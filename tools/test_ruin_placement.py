import os
from pathlib import Path
import subprocess
import tempfile

root = Path(__file__).resolve().parent.parent
java = Path(os.environ["JAVA_HOME"]) / "bin"
output = root / "build" / "ruin-placement-tests"
output.mkdir(parents=True, exist_ok=True)
with tempfile.TemporaryDirectory(prefix="classes-", dir=output) as classes:
    sources = [root / "src/main/java/thaumcraft/world/gen/HilltopRuinPlacement.java", root / "tools/tests/HilltopRuinPlacementTest.java"]
    subprocess.run([str(java / "javac"), "--release", "21", "-d", classes, *map(str, sources)], check=True)
    subprocess.run([str(java / "java"), "-cp", classes, "HilltopRuinPlacementTest"], check=True)
