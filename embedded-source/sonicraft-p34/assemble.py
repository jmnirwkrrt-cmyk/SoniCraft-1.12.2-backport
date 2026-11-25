from pathlib import Path
import zipfile,json,hashlib,struct
r=Path('work34');base=r/'base';patch=r/'patch';out=Path('sonicraft-legacy-1.12.2-0.1.27-p34-zone-mechanics.jar');ver='0.1.27-p34-zone-mechanics';orig=next(Path('in34').rglob('*p33*.jar'));bp='org/sonicraft/backport/';meta='META-INF/sonicraft-p34/'
def h(b):return hashlib.sha256(b).hexdigest()
with zipfile.ZipFile(orig) as z:files={n:z.read(n) for n in z.namelist() if not n.endswith('/')}
origfiles=dict(files)
for p in patch.rglob('*'):
 if p.is_file():files[str(p.relative_to(patch))]=p.read_bytes()
for p in (r/'compiled'/bp).glob('P34*.class'):files[bp+p.name]=p.read_bytes()
for p in [r/'Patch.java',r/'stubs.py',r/'assets.py',r/'assemble.py',r/'NOTES_RU.md',*(r/'src').rglob('*.java')]:files[meta+str(p.relative_to(r))]=p.read_bytes()
f=bp+'SonicraftLegacy.class';old=b'0.1.27-p33-lighting';token=struct.pack('>H',len(old))+old;assert files[f].count(token)==1;files[f]=files[f].replace(token,struct.pack('>H',len(ver))+ver.encode())
data=json.loads(files['mcmod.info']);data[0]['version']=ver;files['mcmod.info']=json.dumps(data,ensure_ascii=False,indent=2).encode()
for n,b in files.items():
 if n.endswith('.class') and n.startswith(bp):assert b[:4]==b'\xca\xfe\xba\xbe' and int.from_bytes(b[6:8],'big')<=52
 if n.endswith('.json') and n.startswith('assets/'):json.loads(b)
def model(n):
 n=n.split(':',1)[-1];return 'assets/sonicraft/models/'+n+'.json'
def graph(n,seen):
 if n in seen:return
 seen.add(n);d=json.loads(files[n]);par=d.get('parent','')
 if par.startswith('sonicraft:'):graph(model(par),seen)
 for v in d.get('textures',{}).values():
  if v.startswith('sonicraft:'):assert 'assets/sonicraft/textures/'+v.split(':')[1]+'.png' in files,(n,v)
seen=set()
for n in files:
 if n.startswith('assets/sonicraft/blockstates/') and files[n]!=origfiles.get(n):
  for v in json.loads(files[n])['variants'].values():graph(model('block/'+v['model'].split(':')[1]),seen)
  graph(n.replace('blockstates','models/item'),seen)
assert len([n for n in files if n.endswith('.nbt') and 'structures_1201' in n])==361
for n in origfiles:
 if '/structures_1201/' in n:assert origfiles[n]==files[n]
assert not any(n.startswith('net/minecraft/') or 'Check34.class' in n for n in files if n not in origfiles)
diff=[n for n in files if files[n]!=origfiles.get(n)];files[meta+'changes.json']=json.dumps(dict(base=orig.name,sha256=h(orig.read_bytes()),changed=sorted(diff)),indent=2).encode()
with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
 for n,b in sorted(files.items()):z.writestr(n,b)
with zipfile.ZipFile(out) as z:assert z.testzip() is None and len(z.namelist())==len(set(z.namelist()))
print(out,out.stat().st_size,h(out.read_bytes()),'changed model graph nodes',len(seen))
