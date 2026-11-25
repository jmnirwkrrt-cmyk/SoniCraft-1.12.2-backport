import json,hashlib
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
def js(x):return (json.dumps(x,ensure_ascii=False,indent=2)+'\n').encode()
base=next(Path('baseline29').rglob('*p28-audio.jar'));orig=next(Path('input').rglob('*.jar'))
changes={}
with ZipFile(base) as z, ZipFile(orig) as o:
 ns='assets/sonicraft/'
 for id in ['spring','red_spring','yellow_spring','neon_spring','ice_spring']:
  key=ns+'blockstates/'+id+'.json';b=json.loads(z.read(key))['variants'];s=json.loads(o.read(key))['variants'];variants={}
  for face,v in b.items():
   variants['blockstate=0,'+face]=v
   used=s[face+',blockstate=1'].copy();model=used['model'].split('/')[-1];used['model']='sonicraft:'+model;variants['blockstate=1,'+face]=used
   k=ns+'models/block/'+model+'.json';m=json.loads(o.read(k));m.pop('render_type',None);m['textures']={a:b.replace(':block/',':blocks/') for a,b in m['textures'].items()};changes[k]=js(m)
   assert ns+'models/'+m['parent'].split(':')[1]+'.json' in z.namelist()
   for tex in m['textures'].values():
    t=ns+'textures/'+tex.split(':')[1]+'.png'
    if t not in z.namelist():changes[t]=o.read(t.replace('/blocks/','/block/'))
  assert len(variants)==12;changes[key]=js({'variants':variants})
 for c in ['MarbleCaveBlocks$Spring','ZoneMachines$Dash']:
  changes['org/sonicraft/backport/'+c+'.class']=Path('p29/work',c+'.class').read_bytes()
 info=json.loads(z.read('mcmod.info'));info[0]['version']='0.1.27-p29-springs';changes['mcmod.info']=js(info)
 changes['META-INF/sonicraft-p29/check.py']=Path('p29/check.py').read_bytes()
 changes['META-INF/sonicraft-p29/BUILD.txt']=b'''Incremental SRG class/resource patch for the P28 Audio JAR, Java classfile 52. No stubs shipped. Place P28 JAR under baseline29/ and original 1.10.1 JAR under input/. Copy Patch.java, assemble.py and check.py from this directory to p29/. Extract MarbleCaveBlocks$Spring.class, ExtraZoneMechanisms$ChemicalSpring.class and ZoneMachines$Dash.class from P28 into p29/classes/ with original package paths. mkdir -p p29/work. With JDK 17 set E="--add-exports java.base/jdk.internal.org.objectweb.asm=ALL-UNNAMED --add-exports java.base/jdk.internal.org.objectweb.asm.tree=ALL-UNNAMED --add-exports java.base/jdk.internal.org.objectweb.asm.tree.analysis=ALL-UNNAMED". Run javac $E -d p29/work p29/Patch.java; java $E -cp p29/work Patch; python3 p29/assemble.py; python3 p29/check.py. The test interprets selected disassembled instructions with mock state/world adapters; it does not launch Minecraft.'''
 changes['META-INF/sonicraft-p29/Patch.java']=Path('p29/Patch.java').read_bytes()
 changes['META-INF/sonicraft-p29/assemble.py']=Path(__file__).read_bytes()
 changes['META-INF/sonicraft-p29/provenance.json']=js({'base':base.name,'base_sha256':hashlib.sha256(base.read_bytes()).hexdigest(),'patched_classes':['MarbleCaveBlocks$Spring','ZoneMachines$Dash'],'spring_reset_ticks':10,'spring_meta':'direction in low 3 bits; active in bit 3','ice_spring':'first active frame only; melting not ported','minecraft_client_tested':False})
 out=Path('sonicraft-legacy-1.12.2-0.1.27-p29-springs.jar')
 with ZipFile(out,'w',ZIP_DEFLATED) as w:
  for n in z.namelist():w.writestr(n,changes.pop(n) if n in changes else z.read(n))
  for n,b in changes.items():w.writestr(n,b)
 with ZipFile(out) as w:
  assert w.testzip() is None and len(set(w.namelist()))==len(w.namelist())
  for n in z.namelist():
   if n.endswith(('.class','.ogg')) and n.split('/')[-1] not in ['MarbleCaveBlocks$Spring.class','ZoneMachines$Dash.class']:assert w.read(n)==z.read(n),n
print(out,out.stat().st_size)
