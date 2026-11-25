import json,zipfile,sys,struct
from nbt43 import *
z=zipfile.ZipFile(sys.argv[1]);assert z.testzip() is None and len(z.namelist())==len(set(z.namelist()))
rows=json.loads(z.read('assets/sonicraft/backport/features45.json'));assert len(rows)==13
ids={x['id'] for x in json.loads(z.read('assets/sonicraft/backport/blocks.json'))};biomes={x['id'] for x in json.loads(z.read('assets/sonicraft/backport/biomes.json'))};report=json.loads(z.read('META-INF/sonicraft-p45/asset_report.json'))
for d in rows:
 assert set(d['biomes'])<=biomes and d['chance']>0
 n='assets/sonicraft/structures_1201/'+d['template']+'.nbt';r=loads(z.read(n));pal=ls(r,'palette');w,h,depth=ls(r,'size');assert len(ls(r,'blocks'))<70000
 ignored=next(x['ignored_blocks'] for x in report if x['id']==d['id'])
 for cell in ls(r,'blocks'):
  x,y,zz=pos(cell);assert 0<=x<w and 0<=y<h and 0<=zz<depth
  name=pal[cell['state'][1]]['Name'][1];assert name not in ignored and name!='minecraft:jigsaw'
  if name.startswith('sonicraft:'):assert name.split(':')[1] in ids,name
 assert len({pos(c) for c in ls(r,'blocks')})==len(ls(r,'blocks'))
sites=json.loads(z.read('assets/sonicraft/backport/sites43.json'));assert sum(s['key']=='ghmountain' for s in sites)==1
assert next(s for s in sites if s['key']=='ghmountain')['rule']==[18,15,1612021572]
assert json.loads(z.read('assets/sonicraft/backport/complex_pools_022.json'))['roots']['ghmountain']=='green_hill_mountain'
for n in z.namelist():
 if n.startswith('org/sonicraft/') and n.endswith('.class'):assert struct.unpack('>H',z.read(n)[6:8])[0]<=52
print('PASS: 13 feature manifests, registered palettes and biomes, source masks, template bounds, GH mountain routing, Java 8 and archive')
