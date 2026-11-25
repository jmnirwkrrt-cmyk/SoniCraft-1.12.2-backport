import sys,json,zipfile,pathlib,struct
r=pathlib.Path(__file__).resolve().parent;src,out=map(pathlib.Path,sys.argv[1:3]);base='org/sonicraft/backport/'
old='0.1.27-p32-structures';new='0.1.27-p33-lighting'
def utf(s):return struct.pack('>H',len(s))+s.encode()
with zipfile.ZipFile(src) as z,zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as w:
 assert len(set(z.namelist()))==len(z.namelist()) and z.testzip() is None
 for i in z.infolist():
  b=z.read(i)
  if i.filename==base+'BulkBlocks.class':b=(r/'classes'/i.filename).read_bytes()
  if i.filename==base+'SonicraftLegacy.class':
   assert b.count(utf(old))==1;b=b.replace(utf(old),utf(new))
  if i.filename=='mcmod.info':
   d=json.loads(b);assert d[0]['version']==old;d[0]['version']=new;b=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
  w.writestr(i,b)
 n=base+'P33Light.class';w.write(r/'classes'/n,n)
 for f in [r/n for n in ['generate.py','levels.json','cases.txt','Patch.java','P33Light.java','assemble.py','README_RU.md']]+list((r/'fixture').rglob('*.java')):w.write(f,'META-INF/sonicraft-p33/'+f.relative_to(r).as_posix())
with zipfile.ZipFile(src) as a,zipfile.ZipFile(out) as b:
 assert b.testzip() is None and set(a.namelist())<=set(b.namelist())
 assert {n for n in a.namelist() if a.read(n)!=b.read(n)}=={base+'BulkBlocks.class',base+'SonicraftLegacy.class','mcmod.info'}
 assert {n for n in set(b.namelist())-set(a.namelist()) if n.endswith('.class')}=={base+'P33Light.class'}
 print('PASS: P32 preserved, versions match, no test or Forge classes injected',out.stat().st_size)
