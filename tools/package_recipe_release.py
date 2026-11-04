"""Compile the addressed sources and preserve every other entry of the previous release.

Python 3.10+, JDK 21, and a NeoForge development compile classpath are required.
Full source builds remain available through the repository's ordinary Gradle tasks.
"""
from pathlib import Path
import argparse,hashlib,json,re,subprocess,zipfile
ROOT=Path(__file__).resolve().parent.parent
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--baseline',type=Path,required=True);ap.add_argument('--classpath',required=True);ap.add_argument('--javac',default='javac');ap.add_argument('--output',type=Path);args=ap.parse_args()
    spec=json.loads((ROOT/'tools/recipe-release.json').read_text(encoding='utf8'))
    assert sha(args.baseline)==spec['previous_sha256'],'Wrong baseline JAR'
    classes=ROOT/'build/recipe-release-classes';classes.mkdir(parents=True,exist_ok=True)
    cp=str(args.baseline.resolve())+__import__('os').pathsep+args.classpath
    argv=['-encoding','UTF-8','-proc:none','--release','21','-cp',cp,'-d',str(classes)]+[str(ROOT/p) for p in spec['sources']]
    response=ROOT/'build/recipe-release-javac.args';response.write_text('\n'.join('"'+s.replace('\\','/').replace('"','\\"')+'"' for s in argv),encoding='utf8')
    subprocess.run([args.javac,'@'+str(response)],check=True)
    changes={p.relative_to(classes).as_posix():p.read_bytes() for p in classes.rglob('*.class')}
    out=args.output or ROOT/'build/libs'/spec['artifact'];out.parent.mkdir(parents=True,exist_ok=True)
    with zipfile.ZipFile(args.baseline) as old:
        if spec['owner']!='thaumcraft':
            meta=old.read('META-INF/neoforge.mods.toml').decode('utf8')
            meta,n=re.subn(r'(?m)^(version\s*=\s*)"[^"]+"',lambda m:m[1]+'"'+spec['version']+'"',meta,count=1);assert n==1
            changes['META-INF/neoforge.mods.toml']=meta.encode('utf8')
        with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as new:
            for info in old.infolist():new.writestr(info,changes.pop(info.filename,old.read(info.filename)))
            for path,data in sorted(changes.items()):new.writestr(path,data)
    print(out);print('SHA256',sha(out))
if __name__=='__main__':main()
