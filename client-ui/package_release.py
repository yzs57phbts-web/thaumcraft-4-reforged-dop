"""Wrap the unchanged published 1.0.1 core with the checked client module."""
from pathlib import Path
import argparse,hashlib,json,zipfile
root=Path(__file__).resolve().parent
parser=argparse.ArgumentParser()
parser.add_argument('--base',type=Path,required=True)
parser.add_argument('--output',type=Path,required=True)
args=parser.parse_args()
assert hashlib.sha256(args.base.read_bytes()).hexdigest()=='a40a9635d98e4fc5ece67ede6e9db2dfd7abf10fbc81d244db6522d9c16f2277'
ui=root/'dist/thaumonomicon-ui-1.21.1-1.2.1.jar'
assert hashlib.sha256(ui.read_bytes()).hexdigest()=='6e6fa965c3d26084a51647f25cd543adccb22a04c7ce566b2e8dcdb29c5e275a'
metadata=(root/'jarjar-metadata.json').read_bytes()
args.output.parent.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(args.base) as original,zipfile.ZipFile(args.output,'w',zipfile.ZIP_DEFLATED) as result:
    for name in original.namelist():result.writestr(name,original.read(name))
    result.writestr('META-INF/jarjar/'+ui.name,ui.read_bytes())
    result.writestr('META-INF/jarjar/metadata.json',metadata)
with zipfile.ZipFile(args.base) as original,zipfile.ZipFile(args.output) as result:
    assert all(original.read(n)==result.read(n) for n in original.namelist())
print(hashlib.sha256(args.output.read_bytes()).hexdigest(),args.output.name)
