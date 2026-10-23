import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parent.parent
JAVA_HOME = Path(os.environ["JAVA_HOME"])
ARTIFACTS = ROOT / "build/moddev/artifacts"
CLASSPATH = [str(ROOT / "build/classes/java/main"), *map(str, ARTIFACTS.glob("*-merged.jar"))]
CLASSPATH.extend((ROOT / "build/moddev/clientLegacyClasspath.txt").read_text().splitlines())
with tempfile.TemporaryDirectory(prefix="thaumcraft-billboard-") as folder:
    classpath = os.pathsep.join(CLASSPATH)
    subprocess.run([str(JAVA_HOME / "bin/javac"), "-cp", classpath, "-d", folder, str(ROOT / "tools/tests/BillboardVertexTest.java")], check=True)
    subprocess.run([str(JAVA_HOME / "bin/java"), "--enable-native-access=ALL-UNNAMED", "-Dlog4j2.configurationFile=" + str(ROOT / "build/moddev/clientLog4j2.xml"), "-cp", os.pathsep.join([folder, classpath]), "BillboardVertexTest"], check=True, cwd=folder)
