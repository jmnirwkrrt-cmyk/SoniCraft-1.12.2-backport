from pathlib import Path
import json,zipfile,zlib,copy
from nbt43 import *
r=Path(__file__).parent;b=zipfile.ZipFile(r.parent/'p47/release/sonicraft-legacy-1.12.2-0.1.27-p47-tornado.jar');o=zipfile.ZipFile(r.parent/'input/Minecraft мод/sonicraft-1.10.1-forge-1.20.1.jar')
def put(n,v):
 p=r/'assets'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(v if isinstance(v,bytes) else (json.dumps(v,ensure_ascii=False,indent=2)+'\n').encode())
biomes=[d['id'] for d in json.loads(b.read('assets/sonicraft/backport/biomes.json'))];defs=json.loads(b.read('assets/sonicraft/backport/badniks.json'))
city=['scrap_brain_zone','chemical_plant_zone','oil_ocean_zone','studiopolis_zone','casino_night_zone','night_carnival_zone','star_light_zone','spring_yard','press_garden_zone']
def spawns(ids,w):return [dict(biomes='sonicraft:'+x,weight=w,min=1,max=2) for x in ids if x in biomes]
base=dict(width=.9,height=1.6,step=.6,speed=.25,health=8,damage=5,armor=0,follow=24,knockback=1)
for id,cls,geo,health,spawn in [('egg_pawn','EggPawn46','',8,spawns(city,8)+spawns(['green_hill_zone','emerald_hill_zone','seaside_hill_zone'],3)),('eggrobo','Eggrobo','',12,spawns(['scrap_brain_zone','oil_ocean_zone','angel_island_jungle','mushroom_hill_zone','ice_cap_zone','sandopolis_zone'],4)),('flapper','Flapper','egg_flapper',8,spawns(['studiopolis_zone','chemical_plant_zone','oil_ocean_zone','seaside_hill_zone','star_light_zone'],6)),('battle_flapper','BattleFlapper','egg_flapper',12,spawns(['night_carnival_zone','casino_night_zone','scrap_brain_zone'],8))]:
 assert not any(d['id']==id for d in defs);defs.append(dict(base,id=id,javaClass=cls,geo=geo,health=health,spawns=spawn))
for d in defs:
 if d['id']=='egg_pawn_bunny':d['spawns']=spawns(['night_carnival_zone','casino_night_zone','studiopolis_zone'],8)
put('assets/sonicraft/backport/badniks.json',defs)
for dest,src in [('eggrobo','eggrobo'),('flapper','egg_flapper'),('battle_flapper','egg_flapper_bat')]:put('assets/sonicraft/textures/entities/'+dest+'.png',o.read('assets/sonicraft/textures/entities/'+src+'.png'))
for lang,labels in [('en_us',['Eggrobo','Flapper','Battle Flapper']),('ru_ru',['Эггробо','Флаппер','Боевой флаппер'])]:
 put('assets/sonicraft/lang/'+lang+'.lang',b.read('assets/sonicraft/lang/'+lang+'.lang')+('\n'+''.join('entity.sonicraft.'+id+'.name='+label+'\n' for id,label in zip(['eggrobo','flapper','battle_flapper'],labels))).encode())
rows=json.loads(b.read('assets/sonicraft/backport/features45.json'));report=[]
selected=['angel_island_boulder_feature','angel_island_boulder_scorched_feature','angel_island_fire_feature','carnival_night_building_1_feature','launch_base_building_1_feature','launch_base_building_2_feature','launch_base_pole_1_feature','launch_base_pole_2_feature','lava_reef_mountain_feature','marble_garden_pole_1_feature','marble_garden_pole_2_feature','shuttle_loop_spawn_feature','emerald_hill_shuttle_loop_spawn_feature']
for key in selected:
 c=json.loads(o.read('data/sonicraft/worldgen/configured_feature/'+key+'.json'))['config'];p=json.loads(o.read('data/sonicraft/worldgen/placed_feature/'+key+'.json'))['placement'];bio=json.loads(o.read('data/sonicraft/forge/biome_modifier/'+key+'_biome_modifier.json'))['biomes'];bio=[bio] if isinstance(bio,str) else bio
 live=[x.split(':')[1] for x in bio if x.split(':')[1] in biomes]
 if not live:report.append(dict(id=key,status='biome_not_ported',biomes=bio));continue
 name=c['structure'].split(':')[1];ignored=c.get('ignored_blocks',[]);ignored=[ignored] if isinstance(ignored,str) else ignored
 nbt=loads(o.read('data/sonicraft/structures/'+name+'.nbt'));pal=ls(nbt,'palette');cells=ls(nbt,'blocks');cells[:]=[x for x in cells if pal[x['state'][1]]['Name'][1] not in ignored]
 assert all(pal[x['state'][1]]['Name'][1]!='minecraft:jigsaw' for x in cells),name
 chance=next(x['chance'] for x in p if x['type']=='minecraft:rarity_filter');grounds=[]
 def predicates(pred):
  if pred['type']=='minecraft:all_of':
   for x in pred['predicates']:predicates(x)
  elif pred['type']=='minecraft:matching_blocks':
   assert pred.get('offset')==[0,-1,0];v=pred['blocks'];grounds.extend([v] if isinstance(v,str) else v)
  else:raise AssertionError(pred)
 for x in p:
  if x['type']=='minecraft:block_predicate_filter':predicates(x['predicate'])
 put('assets/sonicraft/structures_1201/'+name+'_p48.nbt',dumps(nbt))
 rows.append(dict(id=key,template=name+'_p48',original=name,biomes=live,chance=chance,salt=zlib.crc32(key.encode()),offset=c.get('offset',[0,0,0]),rotate=c.get('random_rotation',False),mirror=c.get('random_mirror',False),ground=grounds))
 report.append(dict(id=key,status='added',size=ls(nbt,'size'),entities=[e['nbt'][1]['id'][1] for e in ls(nbt,'entities')]))
put('assets/sonicraft/backport/features45.json',rows)
(r/'asset_report.json').write_text(json.dumps(report,indent=2));print(report)
# Independent city districts: their generation does not depend on a boss arena root.
pools=json.loads(b.read('assets/sonicraft/backport/complex_pools_022.json'));sites=json.loads(b.read('assets/sonicraft/backport/sites43.json'))
added=[]
for prefix,zone,count,y in [('casino_night_city_building_','casino_night_zone',2,-20),('night_carnival_city_building_','night_carnival_zone',1,-2),('star_light_building_','star_light_zone',2,-3),('chemical_plant_building_','chemical_plant_zone',3,-16),('press_garden_factory_ruins_','press_garden_zone',3,-9),('scrap_brain_factory_building_','scrap_brain_zone',3,-19)]:
 for i in range(1,count+1):
  template=prefix+str(i);key='district48_'+template;assert 'assets/sonicraft/structures_1201/'+template+'.nbt' in b.namelist()
  pools['roots'][key]=template;sites.append(dict(key=key,biomes=[zone],rule=[12,9,zlib.crc32(key.encode())&0x7fffffff],height=y));added.append(template)
put('assets/sonicraft/backport/complex_pools_022.json',pools);put('assets/sonicraft/backport/sites43.json',sites)
(r/'asset_report.json').write_text(json.dumps(dict(features=report,districts=added),indent=2))
