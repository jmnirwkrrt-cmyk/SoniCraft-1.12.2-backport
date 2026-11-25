"""Build after compiling Patch32.java and P32Sites.java and running Patch32 on the P31 class."""
import json,zipfile,hashlib,pathlib,sys,struct
p=pathlib.Path(__file__).resolve().parent; src=pathlib.Path(sys.argv[1]);out=pathlib.Path(sys.argv[2]);prefix='org/sonicraft/backport/';prev='0.1.27-p31-zone-loot';new='0.1.27-p32-structures'
def utf(v):return struct.pack('>H',len(v))+v.encode()
with zipfile.ZipFile(src) as z,zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as w:
 assert len(z.namelist())==len(set(z.namelist()))
 for i in z.infolist():
  b=z.read(i)
  if i.filename==prefix+'CityComplexWorldgen.class':b=(p/'classes'/i.filename).read_bytes()
  if i.filename==prefix+'SonicraftLegacy.class':
   assert b.count(utf(prev))==1;b=b.replace(utf(prev),utf(new))
  if i.filename=='mcmod.info':
   j=json.loads(b);assert j[0]['version']==prev;j[0]['version']=new;b=(json.dumps(j,ensure_ascii=False,indent=2)+'\n').encode()
  w.writestr(i,b)
 n=prefix+'P32Sites.class';w.write(p/'classes'/n,n)
 for n in ['Patch32.java','assemble.py','Isolate.java','README_RU.md','src','testsrc']:
  f=p/n
  for a in ([f] if f.is_file() else sorted(f.rglob('*.java'))):w.write(a,'META-INF/sonicraft-p32/'+a.relative_to(p).as_posix())
with zipfile.ZipFile(src) as a,zipfile.ZipFile(out) as b:
 assert b.testzip() is None and set(a.namelist())<=set(b.namelist())
 modified={n for n in a.namelist() if a.read(n)!=b.read(n)}
 assert modified=={prefix+'SonicraftLegacy.class',prefix+'CityComplexWorldgen.class','mcmod.info'},modified
 assert not any(n.startswith('net/minecraft') for n in set(b.namelist())-set(a.namelist()))
 print('PASS: all P31 entries retained; changed classes, version and added files verified;',out.stat().st_size,'bytes')
