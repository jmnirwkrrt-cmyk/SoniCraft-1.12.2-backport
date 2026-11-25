import zipfile,json,sys,collections
sys.path.insert(0,'p44');from nbt43 import *
b=zipfile.ZipFile('p45/release/sonicraft-legacy-1.12.2-0.1.27-p45-source-structures.jar');o=zipfile.ZipFile('input/Minecraft мод/sonicraft-1.10.1-forge-1.20.1.jar')
for d in json.loads(b.read('assets/sonicraft/backport/blocks.json')):
 if any(s in d['id'] for s in ['checkpoint','starpost','interior_pole','interior_wall_lamp','studiopolis_camera','goal_plate']):print('BLOCK',d)
print('PAWNS',[n for n in b.namelist() if 'pawn' in n.lower() and (n.endswith('.class') or '/geo/' in n)])
p=json.loads(b.read('assets/sonicraft/backport/complex_pools_022.json'))
for pool in p['pools']:
 if 'studiopolis' in pool['id']:print('POOL',pool)
for n in b.namelist():
 if n.startswith('assets/sonicraft/structures_1201/studiopolis'):
  r=loads(b.read(n));pal=ls(r,'palette');print('TEMPLATE',n.rsplit('/',1)[1],ls(r,'size'))
  for c in ls(r,'blocks'):
   name=pal[c['state'][1]]['Name'][1]
   if name=='minecraft:jigsaw' or any(s in name for s in ['goal','starpost','checkpoint','boss']):print(pos(c),pal[c['state'][1]],c.get('nbt'))
