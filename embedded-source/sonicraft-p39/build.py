from pathlib import Path
import hashlib,struct,json,zipfile,sys
src,out=map(Path,sys.argv[1:]);base=Path(__file__).parent
assert hashlib.sha256(src.read_bytes()).hexdigest()=='18f69bdcd9857af291779d5725f5199e53209adfc2867d92c5940a33e1237490'
with zipfile.ZipFile(src) as z,zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as w:
 for name in z.namelist():
  b=z.read(name)
  if name=='assets/sonicraft/backport/blocks.json':
   defs=json.loads(b); missing=[d for d in defs if not d.get('material')]
   assert len(missing)==92
   for d in missing:d['material']='stone'
   b=json.dumps(defs,ensure_ascii=False,separators=(',',':')).encode()
  elif name=='org/sonicraft/backport/SonicraftLegacy.class':
   prev=b'0.1.27-p38-chemical';new=b'0.1.27-p39-registry';assert len(prev)==len(new) and b.count(prev)==1;b=b.replace(prev,new)
  elif name=='mcmod.info':
   info=json.loads(b);info[0]['version']='0.1.27-p39-registry';b=json.dumps(info,indent=2).encode()
  w.writestr(name,b)
 for n in ['build.py','Check.java','README.txt']:w.write(base/n,'META-INF/sonicraft-p39/'+n)
with zipfile.ZipFile(out) as w:assert w.testzip() is None
