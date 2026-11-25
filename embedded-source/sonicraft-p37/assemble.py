from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,sys
p=Path(__file__).resolve().parent
src,out,original=map(Path,sys.argv[1:])
assert hashlib.sha256(src.read_bytes()).hexdigest()=='e6b18616c1ab005dfea759735708fdd093d9a96f4491cd0331cd45f86594f39e'
changes={f.relative_to(p/'bin').as_posix():f.read_bytes() for f in (p/'bin').rglob('*.class')}
with ZipFile(src) as z,ZipFile(out,'w',ZIP_DEFLATED) as w:
 meta=json.loads(z.read('mcmod.info'));meta[0]['version']='0.1.27-p37-warp';changes['mcmod.info']=json.dumps(meta,indent=2).encode()
 with ZipFile(original) as oz:
  sn='assets/sonicraft/sounds.json';snd=json.loads(z.read(sn));snd['jump_panel.use']=json.loads(oz.read(sn))['jump_panel.use'];changes[sn]=json.dumps(snd,indent=1).encode()
  for entry in snd['jump_panel.use']['sounds']:
   ns,base=entry['name'].split(':');ogg=f'assets/{ns}/sounds/{base}.ogg';changes[ogg]=oz.read(ogg);assert changes[ogg][:4]==b'OggS'
 for n in z.namelist():
  if n not in changes:w.writestr(n,z.read(n))
 for n,b in changes.items():w.writestr(n,b)
 for f in [p/'Patch.java',p/'README_RU.txt',p/'test_bridge.py',p/'assemble.py',*(p/'src').rglob('*.java')]:w.write(f,'META-INF/sonicraft-p37/'+f.relative_to(p).as_posix())
with ZipFile(src) as z, ZipFile(out) as w:
 assert w.testzip() is None and len(w.namelist())==len(set(w.namelist()))
 assert [n for n in z.namelist() if z.read(n)!=w.read(n) and n not in changes]==[]
 assert len([n for n in changes if n.endswith('.class')])==6
 print(out, out.stat().st_size,hashlib.sha256(out.read_bytes()).hexdigest())
