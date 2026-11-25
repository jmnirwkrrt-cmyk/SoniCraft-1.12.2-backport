import zipfile,json,zlib,collections
from pathlib import Path
from nbt43 import *
R=Path(__file__).resolve().parent;b=zipfile.ZipFile(R.parent/'p44/release/sonicraft-legacy-1.12.2-0.1.27-p44-scrap-route.jar');o=zipfile.ZipFile(R.parent/'input/Minecraft мод/sonicraft-1.10.1-forge-1.20.1.jar')
selected=['green_hill_bridge_lake','green_hill_mountain_feature','ghz_totem_feature','marble_ruins_feature','ice_cap_pit_1_feature','sandopolis_playground_1_feature','sandopolis_tower_feature','aquatic_ruin_platform_1_feature','aquatic_ruin_big_platform_1_feature','aquatic_ruin_floating_platform_feature','seaside_hill_columns','mushroom_hill_mushroom_spawn_feature','hill_top_rexon_lava_lake']
def put(n,v):
 p=R/'assets'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(v if isinstance(v,bytes) else (json.dumps(v,indent=2,ensure_ascii=False)+'\n').encode())
rows=[];report=[]
for key in selected:
 c=json.loads(o.read('data/sonicraft/worldgen/configured_feature/'+key+'.json'))['config'];p=json.loads(o.read('data/sonicraft/worldgen/placed_feature/'+key+'.json'))['placement'];mod=json.loads(o.read('data/sonicraft/forge/biome_modifier/'+key+'_biome_modifier.json'));bio=mod['biomes'];bio=[bio] if isinstance(bio,str) else bio
 name=c['structure'].split(':')[1];ignored=c.get('ignored_blocks',[]);ignored=[ignored] if isinstance(ignored,str) else ignored
 r=loads(o.read('data/sonicraft/structures/'+name+'.nbt'));pal=ls(r,'palette');blocks=ls(r,'blocks');before=len(blocks);blocks[:]=[x for x in blocks if pal[x['state'][1]]['Name'][1] not in ignored]
 assert all(pal[x['state'][1]]['Name'][1]!='minecraft:jigsaw' for x in blocks)
 put('assets/sonicraft/structures_1201/'+name+'_p45.nbt',dumps(r))
 chance=next(x['chance'] for x in p if x['type']=='minecraft:rarity_filter');grounds=[]
 def predicates(pred):
  if pred['type']=='minecraft:all_of':
   for x in pred['predicates']:predicates(x)
  elif pred['type']=='minecraft:matching_blocks':
   assert pred.get('offset')==[0,-1,0];v=pred['blocks'];grounds.extend([v] if isinstance(v,str) else v)
  elif pred['type']=='minecraft:would_survive':grounds.append('sonicraft:ghz_grass')
  else:raise AssertionError(pred)
 for x in p:
  if x['type']=='minecraft:block_predicate_filter':predicates(x['predicate'])
 row=dict(id=key,template=name+'_p45',original=name,biomes=[x.split(':')[1] for x in bio],chance=chance,salt=zlib.crc32(key.encode()),offset=c.get('offset',[0,0,0]),rotate=c.get('random_rotation',False),mirror=c.get('random_mirror',False),ground=grounds)
 rows.append(row);report.append(dict(**row,size=ls(r,'size'),ignored_blocks=ignored,removed_cells=before-len(blocks),entities=[e['nbt'][1]['id'][1] for e in ls(r,'entities')]))
put('assets/sonicraft/backport/features45.json',rows)
pools=json.loads(b.read('assets/sonicraft/backport/complex_pools_022.json'));pools['roots']['ghmountain']='green_hill_mountain';put('assets/sonicraft/backport/complex_pools_022.json',pools)
sites=json.loads(b.read('assets/sonicraft/backport/sites43.json'));rule=json.loads(o.read('data/sonicraft/worldgen/structure_set/green_hill_mountain.json'))['placement'];sites.append(dict(key='ghmountain',biomes=['green_hill_zone'],rule=[rule['spacing'],rule['separation'],rule['salt']]));put('assets/sonicraft/backport/sites43.json',sites)
(R/'asset_report.json').write_text(json.dumps(report,indent=2));print('Prepared',len(rows),'features + GH mountain route')
