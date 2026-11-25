import zipfile,json,sys,re
from nbt43 import *
z=zipfile.ZipFile(sys.argv[1]);assert z.testzip() is None
for name,maximum in [('night_carnival_curtain_light',3),('cpz_factory_pillar_corner',3),('press_garden_redstone_bulb_detector',15)]:
 v=json.loads(z.read('assets/sonicraft/blockstates/'+name+'.json'))['variants']
 for key in list(v):
  for state in range(maximum+1):assert re.sub(r'blockstate=\d+','blockstate='+str(state),key) in v
r=loads(z.read('assets/sonicraft/structures_1201/tails_lab.nbt'));assert ls(r,'size')==[26,10,11];assert any(e['nbt'][1]['id'][1]=='sonicraft:tornado' for e in ls(r,'entities'));pal=ls(r,'palette');assert not any(pal[c['state'][1]]['Name'][1]=='minecraft:jigsaw' for c in ls(r,'blocks'));assert len({pos(c) for c in ls(r,'blocks')})==len(ls(r,'blocks'))
r=loads(z.read('assets/sonicraft/structures_1201/big_the_cat_house_green_hill.nbt'));pal=ls(r,'palette');assert not any(pal[c['state'][1]]['Name'][1]=='minecraft:structure_block' for c in ls(r,'blocks'))
assert all(n in z.namelist() for n in ['assets/sonicraft/geo/tornado_plane.geo.json','assets/sonicraft/animations/tornado_plane.animation.json','assets/sonicraft/textures/entities/tornado.png'])
a=json.loads(z.read('assets/sonicraft/animations/tornado_plane.animation.json'));assert all(k in a['animations'] for k in ['idle','fly'])
def walk(d):
 if isinstance(d,dict):
  if 'item' in d:assert 'data' in d,d
  for v in d.values():walk(v)
 elif isinstance(d,list):
  for v in d:walk(v)
for n in z.namelist():
 if n.startswith('assets/sonicraft/recipes/') and n.endswith('.json'):walk(json.loads(z.read(n)))
print('PASS: variant coverage, recipe metadata, merged runway + plane, house masks, plane geometry and animations, archive CRC')
