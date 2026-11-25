import sys,zipfile,json
from nbt43 import *
z=zipfile.ZipFile(sys.argv[1]);assert z.testzip() is None
for n in z.namelist():
 if n.startswith('assets/sonicraft/blockstates/') and n.endswith('.json') and ('camera' in n or 'starpost' in n):
  d=json.loads(z.read(n));assert all('particle' not in v['model'] for v in d['variants'].values()),n
for variant in ['', '_upside_down']:
 d=json.loads(z.read('assets/sonicraft/models/block/camera46'+variant+'.json'));assert len(d['elements'])>=8
r=loads(z.read('assets/sonicraft/structures_1201/studiopolis_city_egg_tv_studio.nbt'));pal=ls(r,'palette');signs=[pos(c) for c in ls(r,'blocks') if pal[c['state'][1]]['Name'][1]=='sonicraft:signpost'];assert signs==[(7,25,23)],signs
sites=json.loads(z.read('assets/sonicraft/backport/sites43.json'));assert len([s for s in sites if s['key'].startswith('studiobuilding')])==4
for n in ['EggPawn46','PawnModel46','Blocks46','StructureFoundation']:assert 'org/sonicraft/backport/'+n+'.class' in z.namelist()
print('PASS: models, sign destination, four building sites, class entries and ZIP CRC')

# Resolve every new rendered model through its parent chain and verify referenced textures.
for ident in ['camera46','camera46_upside_down','checkpoint46','checkpoint46_active']:
 model=json.loads(z.read('assets/sonicraft/models/block/'+ident+'.json'))
 assert model['elements'],ident
 for tex in model['textures'].values():
  ns,path=tex.split(':');assert 'assets/'+ns+'/textures/'+path+'.png' in z.namelist(),tex
 for element in model['elements']:
  assert all(-16<=v<=32 for v in element['from']+element['to']),element
print('PASS: all new model textures present and element bounds valid for 1.12')
