import gzip
import json
import subprocess
import sys
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def content(path):
    data = path.read_bytes()
    if path.suffix == ".json":
        return json.loads(data)
    if path.suffix == ".nbt":
        return gzip.decompress(data)
    return data.replace(b"\r\n", b"\n")


def compare(expected, generated):
    first = {p.relative_to(expected): p for p in expected.rglob("*") if p.is_file()}
    second = {p.relative_to(generated): p for p in generated.rglob("*") if p.is_file()}
    problems = []
    for name in sorted(first.keys() | second.keys()):
        if name not in second:
            problems.append(f"missing from generator: {name.as_posix()}")
        elif name not in first:
            problems.append(f"uncommitted generated resource: {name.as_posix()}")
        elif content(first[name]) != content(second[name]):
            problems.append(f"content differs: {name.as_posix()}")
    return problems


def main():
    build = ROOT / "build"
    build.mkdir(exist_ok=True)
    with tempfile.TemporaryDirectory(prefix="resource-check-", dir=build) as folder:
        output = Path(folder).resolve()
        assert output.is_relative_to(build.resolve())
        subprocess.run([sys.executable, str(ROOT / "tools/gen_resources.py"), "--mc", "1.21.1", "--out", str(output)], cwd=ROOT, check=True)
        problems = compare(ROOT / "src/generated/resources", output)
    for problem in problems:
        print(problem)
    print(f"Generated resource drift: {len(problems)} problem(s)")
    return bool(problems)


if __name__ == "__main__":
    sys.exit(main())
