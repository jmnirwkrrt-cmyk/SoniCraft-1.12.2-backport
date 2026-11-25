import json,sys,zipfile,struct
from pathlib import Path
from nbt43 import loads,ls,pos
z=zipfile.ZipFile(sys.argv[1]);assert z.testzip() is None
names=set(z.namelist());assert len(names)==len(z.namelist())
for n in names:
 if n.endswith('.json') and n.startswith('assets/'):json.loads(z.read(n))
blocks=json.loads(z.read('assets/sonicraft/backport/blocks.json'));ids={x['id'] for x in blocks};assert len(ids)==len(blocks)
known={'sonicraft:'+x for x in ids}|{'sonicraft:'+x['id'] for x in json.loads(z.read('assets/sonicraft/backport/foods.json'))}|{'sonicraft:red_star_ring','sonicraft:badnik_scrap','sonicraft:bubblegum'}
monitors=[x['id'] for x in blocks if x['kind']=='character_monitor'];assert len(monitors)==14
for n in names:
 if n.startswith('assets/sonicraft/recipes/') and (n.endswith('_item_box_recipe.json') or n.endswith('/item_box_recipe.json') or n.endswith('/red_star_ring_p43.json')):
  d=json.loads(z.read(n));ings=d.get('ingredients',list(d.get('key',{}).values()))
  for ing in ings+[d['result']]:
   for i in ing if isinstance(ing,list) else [ing]:
    assert 'tag' not in i
    if i['item'].startswith('sonicraft:'):assert i['item'] in known,(n,i)
for id in monitors:
 d=json.loads(z.read('assets/sonicraft/models/block/'+id+'.json'));assert 'assets/sonicraft/models/'+d['parent'].split(':')[1]+'.json' in names
 for t in d['textures'].values():
  if not t.startswith('#'):assert 'assets/sonicraft/textures/'+t.split(':')[1]+'.png' in names,(id,t)
 assert len(json.loads(z.read('assets/sonicraft/blockstates/'+id+'.json'))['variants'])==4
for x in blocks:
 if 'leaves' in x['id']:assert len(json.loads(z.read('assets/sonicraft/blockstates/'+x['id']+'.json'))['variants'])==4
p='assets/sonicraft/structures_1201/'
r=loads(z.read(p+'scrap_brain_factory.nbt'));pal=ls(r,'palette');bs=ls(r,'blocks')
assert ls(r,'size')==[25,66,25]
assert sum(pal[b['state'][1]]['Name'][1]=='minecraft:water' for b in bs)==662
assert sum(e['nbt'][1]['id'][1]=='sonicraft:scrap_brain_eggman' for e in ls(r,'entities'))==1
assert len({pos(b) for b in bs})==len(bs)
shafts=[pos(b) for b in bs if pal[b['state'][1]]['Name'][1]=='sonicraft:egg_crusher_shaft_frame'];assert len(shafts)>=4,shafts
sites=json.loads(z.read('assets/sonicraft/backport/sites43.json'));assert not {'deathegg','aquaticlake'}&{s['key'] for s in sites}
pools=json.loads(z.read('assets/sonicraft/backport/complex_pools_022.json'))
for pool in pools['pools']:
 for e in pool['entries']:assert p+e['id']+'.nbt' in names,e
for n in names:
 if n.startswith('org/sonicraft/') and n.endswith('.class'):assert struct.unpack('>H',z.read(n)[6:8])[0]<=52,n
print('PASS: JSON, 14 monitor recipes/models, 8 leaf states, flooded factory, one Eggman, four shafts, pool template references, no natural Death Egg, Java 8 bytecode')
