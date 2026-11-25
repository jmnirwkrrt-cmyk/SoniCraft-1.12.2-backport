import zipfile,json,sys,collections
sys.path.insert(0,'p44');from nbt43 import *
b=zipfile.ZipFile('p44/release/sonicraft-legacy-1.12.2-0.1.27-p44-scrap-route.jar');o=zipfile.ZipFile('input/Minecraft мод/sonicraft-1.10.1-forge-1.20.1.jar');ids={x['id'] for x in json.loads(b.read('assets/sonicraft/backport/blocks.json'))};bio={}
for n in o.namelist():
 if '/biome_modifier/' in n and n.endswith('.json'):
  d=json.loads(o.read(n));bi=d.get('biomes');fe=d.get('features');bi=[bi] if isinstance(bi,str) else bi;fe=[fe] if isinstance(fe,str) else fe
  if isinstance(bi,list) and isinstance(fe,list):
   for f in fe:bio.setdefault(f,[]).extend(x.split(':')[-1] for x in bi)
for n in o.namelist():
 if n.startswith('data/sonicraft/worldgen/biome/') and n.endswith('.json'):
  for stage in json.loads(o.read(n))['features']:
   for f in stage:bio.setdefault(f,[]).append(n.rsplit('/',1)[1][:-5])
for n in o.namelist():
 if not(n.startswith('data/sonicraft/worldgen/configured_feature/') and n.endswith('.json')):continue
 d=json.loads(o.read(n))
 if d['type']!='sonicraft:structure_feature':continue
 key=n.rsplit('/',1)[1][:-5];c=d['config'];name=c['structure'].split(':')[1];p=json.loads(o.read('data/sonicraft/worldgen/placed_feature/'+key+'.json'));r=loads(b.read('assets/sonicraft/structures_1201/'+name+'.nbt')) if 'assets/sonicraft/structures_1201/'+name+'.nbt' in b.namelist() else None
 if r is None:continue
 if not set(bio.get('sonicraft:'+key,[])) & {x['id'] for x in json.loads(b.read('assets/sonicraft/backport/biomes.json'))}:continue
 missing=[s['Name'][1] for s in ls(r,'palette') if s['Name'][1].startswith('sonicraft:') and s['Name'][1].split(':')[1] not in ids]
 print(key,name,ls(r,'size'),'BIO',bio.get('sonicraft:'+key),'config',c,'placement',p['placement'],'MISSING',missing)
