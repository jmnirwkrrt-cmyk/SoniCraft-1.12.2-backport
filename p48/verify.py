import sys,json,zipfile
from pathlib import Path
from nbt43 import *
z=zipfile.ZipFile(sys.argv[1]);assert z.testzip() is None
base='assets/sonicraft/backport/'
defs=json.loads(z.read(base+'badniks.json'));by={d['id']:d for d in defs}
for id in ['egg_pawn','egg_pawn_bunny','eggrobo','flapper','battle_flapper']:
 assert len(by[id]['spawns'])>=3
 for s in by[id]['spawns']:assert s['weight']>0
for id in ['eggrobo','flapper','battle_flapper']:assert z.read('assets/sonicraft/textures/entities/'+id+'.png').startswith(b'\x89PNG')
a=json.loads(z.read('assets/sonicraft/animations/egg_flapper.animation.json'))['animations'];assert 'idle' in a
sites=json.loads(z.read(base+'sites43.json'));pools=json.loads(z.read(base+'complex_pools_022.json'))
new=[s for s in sites if s['key'].startswith('district48_')];assert len(new)==14
for s in new:
 assert s['rule'][0]>s['rule'][1]>0;assert isinstance(s['height'],int)
 t=loads(z.read('assets/sonicraft/structures_1201/'+pools['roots'][s['key']]+'.nbt'));assert ls(t,'blocks')
 for e in ls(t,'entities'):assert 'boss' not in e['nbt'][1].get('id',(0,''))[1]
features=json.loads(z.read(base+'features45.json'));new=[f for f in features if f['template'].endswith('_p48')];assert len(new)==3
for f in new:
 t=loads(z.read('assets/sonicraft/structures_1201/'+f['template']+'.nbt'));pal=ls(t,'palette')
 assert all(pal[c['state'][1]]['Name'][1] not in ['minecraft:jigsaw','minecraft:structure_block'] for c in ls(t,'blocks'))
print('PASS spawn rosters, original textures/animations, 14 independent city districts, 3 source features')
