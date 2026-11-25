from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,sys
p=Path(__file__).resolve().parent
src,out=map(Path,sys.argv[1:])
assert hashlib.sha256(src.read_bytes()).hexdigest()=='074dd240a932b482483c785c39f0e3f18100d6fdf9471652fdc7908e3ebe4b9e'
changes={f.relative_to(p/'bin').as_posix():f.read_bytes() for f in (p/'bin').rglob('*.class')}
with ZipFile(src) as z,ZipFile(out,'w',ZIP_DEFLATED) as w:
 meta=json.loads(z.read('mcmod.info'));meta[0]['version']='0.1.27-p36-mobius-planet';changes['mcmod.info']=json.dumps(meta,indent=2).encode()
 for n in z.namelist():
  if n not in changes:w.writestr(n,z.read(n))
 for n,b in changes.items():w.writestr(n,b)
 for f in [p/'Patch.java',p/'README_RU.txt',p/'test_bridge.py',p/'assemble.py',*(p/'src').rglob('*.java')]:w.write(f,'META-INF/sonicraft-p36/'+f.relative_to(p).as_posix())
with ZipFile(src) as z, ZipFile(out) as w:
 assert w.testzip() is None and len(w.namelist())==len(set(w.namelist()))
 assert [n for n in z.namelist() if z.read(n)!=w.read(n) and n not in changes]==[]
 assert {n for n in changes if n in z.namelist()}=={'org/sonicraft/backport/MobiusWorld.class','org/sonicraft/backport/SonicraftLegacy.class','mcmod.info'}
 print(out, out.stat().st_size,hashlib.sha256(out.read_bytes()).hexdigest())
