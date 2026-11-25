from pathlib import Path
import sys,zipfile,json,hashlib
p=Path(__file__).parent;src,orig,out=map(Path,sys.argv[1:])
assert hashlib.sha256(src.read_bytes()).hexdigest()=='bde0d22e521c37b5b469d6243b3d8f6931fdc21f4bbdd90ca31e198f1156dd6b'
with zipfile.ZipFile(src) as z,zipfile.ZipFile(orig) as o,zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as w:
 ch={f.relative_to(p/'bin').as_posix():f.read_bytes() for f in (p/'bin').rglob('*.class')};assert len(ch)==4
 s='assets/sonicraft/sounds.json';a=json.loads(z.read(s));b=json.loads(o.read(s))
 for k in ['dash_pad.classic','dash_ring.normal']:
  a[k]=b[k]
  for entry in a[k]['sounds']:
   ns,path=entry['name'].split(':');n=f'assets/{ns}/sounds/{path}.ogg';ch[n]=o.read(n);assert ch[n][:4]==b'OggS'
 ch[s]=json.dumps(a,indent=1).encode();old=b'0.1.27-p39-registry';new=b'0.1.27-p40-dashring'
 for n in ['mcmod.info','org/sonicraft/backport/SonicraftLegacy.class']:
  v=z.read(n);assert len(old)==len(new) and v.count(old)==1;ch[n]=v.replace(old,new)
 for n in z.namelist():
  if n not in ch:w.writestr(n,z.read(n))
 for n,v in ch.items():w.writestr(n,v)
 for n in ['P40.java','Patch.java','Check40.java','build.py']:w.write(p/n,'META-INF/sonicraft-p40/'+n)
with zipfile.ZipFile(out) as z:assert z.testzip() is None and len(set(z.namelist()))==len(z.namelist())
