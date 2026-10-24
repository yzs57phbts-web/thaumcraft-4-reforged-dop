import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parent.parent
JAVA_HOME = Path(os.environ["JAVA_HOME"])
CLASSPATH_FILE = ROOT / "build/moddev/geometryTestClasspath.txt"
if not CLASSPATH_FILE.is_file():
    raise RuntimeError("Run ./gradlew writeGeometryTestClasspath before geometry tests")
CLASSPATH = CLASSPATH_FILE.read_text(encoding="utf-8").splitlines()
with tempfile.TemporaryDirectory(prefix="thaumcraft-client-damage-") as folder:
    classpath = os.pathsep.join(CLASSPATH)
    subprocess.run([str(JAVA_HOME / "bin/javac"), "-cp", classpath, "-d", folder, str(ROOT / "tools/tests/ClientDamageTest.java")], check=True)
    subprocess.run([str(JAVA_HOME / "bin/java"), "--enable-native-access=ALL-UNNAMED", "-Dlog4j2.configurationFile=" + str(ROOT / "build/moddev/clientLog4j2.xml"), "-cp", os.pathsep.join([folder, classpath]), "ClientDamageTest"], check=True, cwd=folder)
