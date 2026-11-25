from pathlib import Path
import zipfile,hashlib
root=Path(__file__).resolve().parent;release=root/'release';base=root.parent/'p47/release/sonicraft-legacy-1.12.2-0.1.27-p47-tornado.jar'
with zipfile.ZipFile(release/'SoniCraft_P48_SOURCE.zip','w',zipfile.ZIP_DEFLATED) as out:
 seen=set()
 def put(name,raw):
  assert name not in seen,name;seen.add(name);out.writestr(name,raw)
 for n in ['README.md','README_RU.md','LICENSE.MD']:put(n,(root/n).read_bytes())
 put('baseline/P47.jar',base.read_bytes());put('baseline/SHA256.txt',(hashlib.sha256(base.read_bytes()).hexdigest()+'  P47.jar\n').encode())
 for f in root.rglob('*'):
  rel=f.relative_to(root)
  if not f.is_file() or rel.parts[0] in ['release','inspect','__pycache__','libhelpers'] or f.suffix in ['.log','.class','.jar']:continue
  if rel.parts[0] in ['src','assets'] or len(rel.parts)==1 and f.suffix in ['.py','.java','.sh','.md','.MD','.json']:put('p48/'+rel.as_posix(),f.read_bytes())
 for folder in ['build027','p42','social42','p43','p44','p45','p46','p47']:
  parent=root.parent/folder
  for sub in ['src','assets']:
   for f in (parent/sub).rglob('*'):
    if f.is_file() and '__pycache__' not in f.parts and f.suffix not in ['.class','.jar']:put('historical/'+folder+'/'+f.relative_to(parent).as_posix(),f.read_bytes())
  for f in parent.iterdir():
   if f.is_file() and f.suffix in ['.java','.py','.sh','.cmd','.md','.gradle','.properties'] and f.name not in ['gradle.properties']:put('historical/'+folder+'/'+f.name,f.read_bytes())
 with zipfile.ZipFile(base) as z:
  for n in z.namelist():
   if n.startswith('META-INF/sonicraft-') and not n.endswith('/'):put('embedded-source/'+n[len('META-INF/'):],z.read(n))
 for n in ['ORIGINAL_MOD_METADATA.toml']:
  put('attribution/'+n,(root.parent/'build027'/n).read_bytes())
with zipfile.ZipFile(release/'SoniCraft_P48_Mobius_Migration.zip','w',zipfile.ZIP_DEFLATED) as z:
 for n in ['migrate_mobius.py','nbt43.py','README.md','README_RU.md','LICENSE.MD','tests.py']:z.write(root/n,n)
for file in ['SoniCraft_P48_SOURCE.zip','SoniCraft_P48_Mobius_Migration.zip']:
 with zipfile.ZipFile(release/file) as z:assert z.testzip() is None
 print(file,(release/file).stat().st_size)
