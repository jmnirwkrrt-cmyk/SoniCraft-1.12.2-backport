import zipfile,json,collections,copy,sys,hashlib
from pathlib import Path
from nbt43 import *
ROOT=Path(__file__).resolve().parent
base=zipfile.ZipFile(ROOT.parent/'social42/release/sonicraft-legacy-1.12.2-0.1.27-p42-companions.jar')
orig=zipfile.ZipFile(ROOT.parent/'input/Minecraft мод/sonicraft-1.10.1-forge-1.20.1.jar')
changes={};report={}
def put(n,v):changes[n]=(json.dumps(v,ensure_ascii=False,indent=2)+'\n').encode() if not isinstance(v,bytes) else v
prefix='assets/sonicraft/structures_1201/'
def get(n):return loads(changes.get(prefix+n+'.nbt',base.read(prefix+n+'.nbt')))
def save(n,r):put(prefix+n+'.nbt',dumps(r))
def setstate(r,b,name):
 pal=ls(r,'palette');d={'Name':(8,name)}
 if d not in pal:pal.append(d)
 b['state']=(3,pal.index(d));b.pop('nbt',None)
# Join the mandatory flooded basement to the factory root: no partial jigsaw fallback can omit it.
r=get('scrap_brain_factory');d=get('scrap_brain_factory_dungeon');pal=ls(r,'palette');blocks=ls(r,'blocks');entities=ls(r,'entities')
for b in blocks:
 x,y,z=pos(b);b['pos']=(9,(3,[x,y+18,z]))
 if (x,y,z)==(12,0,12):setstate(r,b,'minecraft:air')
for e in entities:
 for k in ('pos','blockPos'):
  v=ls(e,k);v[1]+=18
palmap={}
for i,s in enumerate(ls(d,'palette')):
 if s not in pal:pal.append(s)
 palmap[i]=pal.index(s)
for b in ls(d,'blocks'):
 if pos(b)==(12,17,12):setstate(d,b,'minecraft:air')
 # setstate can append to dungeon palette; map it too.
 s=ls(d,'palette')[b['state'][1]]
 if s not in pal:pal.append(s)
 b['state']=(3,pal.index(s));blocks.append(b)
r['size']=(9,(3,[25,66,25]));entities.extend(ls(d,'entities'))
seen=set();dedup=[]
for e in entities:
 key=(e['nbt'][1]['id'][1],tuple(ls(e,'pos')))
 if key not in seen:seen.add(key);dedup.append(e)
entities[:]=dedup
save('scrap_brain_factory',r)
assert sum(pal[b['state'][1]]['Name'][1]=='minecraft:water' for b in blocks)==662
assert sum(e['nbt'][1]['id'][1]=='sonicraft:scrap_brain_eggman' for e in entities)==1
report['scrap_factory']={'size':[25,66,25],'water_blocks':662,'scene_eggmen':1,'mandatory_basement':True}
# Honour the original block-ignore processors (bedrock is a template mask, never generated terrain).
ignored={}
for n in orig.namelist():
 if not n.startswith('data/sonicraft/worldgen/template_pool/') or not n.endswith('.json'):continue
 for e in json.loads(orig.read(n))['elements']:
  el=e['element'];name=el.get('location','').split(':')[-1];processors=el.get('processors',{})
  if not isinstance(processors,dict):continue
  ids=set()
  for proc in processors.get('processors',[]):
   if proc.get('processor_type')=='minecraft:block_ignore':ids.update(b['Name'] for b in proc['blocks'])
  ignored.setdefault(name,set()).update(ids)
removed={}
for name,ids in ignored.items():
 if prefix+name+'.nbt' not in base.namelist():continue
 if name in ('scrap_brain_factory','scrap_brain_factory_dungeon'):continue
 # Retain room air for all underground sections; bedrock and structure blocks are intentional masks.
 ids &= {'minecraft:bedrock','minecraft:structure_block'}
 if not ids:continue
 t=get(name);pal=ls(t,'palette');lst=ls(t,'blocks');before=len(lst);lst[:]=[b for b in lst if pal[b['state'][1]]['Name'][1] not in ids]
 if len(lst)!=before:removed[name]=before-len(lst);save(name,t)
report['source_mask_blocks_removed']=removed
# Keep all original pool links, including previously unreachable Mystic Cave and Aquatic Ruin chains.
pools=json.loads(base.read('assets/sonicraft/backport/complex_pools_022.json'));known={p['id'] for p in pools['pools']}
for n in orig.namelist():
 if not n.startswith('data/sonicraft/worldgen/template_pool/') or not n.endswith('.json'):continue
 id='sonicraft:'+n.rsplit('/',1)[1][:-5]
 if id in known:continue
 entries=[]
 for e in json.loads(orig.read(n))['elements']:
  loc=e['element'].get('location','').split(':')[-1]
  if prefix+loc+'.nbt' in base.namelist():entries.append({'id':loc,'weight':e['weight']})
 if entries:pools['pools'].append({'id':id,'entries':entries})
pools['roots'].update(magnet='death_egg_magnet',mysticbridge='mystic_cave_bridge_center',aquatictemple='aquatic_ruin_lake_spawner')
put('assets/sonicraft/backport/complex_pools_022.json',pools)
keys=['miragebar','chaogarden','mackpool','oceanpalace','hillmountain','aquaticlake','seasidelake','labyrinth','hydrocity','metalpit','bigarms']
biomes=[['mirage_saloon_zone'],['green_hill_zone','emerald_hill_zone','angel_island_jungle','mushroom_hill_zone','marble_garden_zone'],['chemical_plant_zone'],['seaside_hill_zone'],['hill_top_zone'],['aquatic_ruin_zone'],['seaside_hill_zone'],['green_hill_zone','marble_zone','spring_yard','star_light_zone'],['angel_island_jungle'],['chemical_plant_zone'],['angel_island_jungle']]
rules=[[10,9,1382979782],[60,48,921168784],[8,6,1476962428],[9,8,520965966],[7,6,1088296121],[6,5,397984357],[6,5,921771000],[32,20,1474162779],[12,6,673344742],[16,10,140091],[32,20,1453407575]]
sites=[dict(key=k,biomes=b,rule=r) for k,b,r in zip(keys,biomes,rules)]
for key,name in [('magnet','death_egg_magnet'),('mysticbridge','mystic_cave_bridge'),('aquatictemple','aquatic_ruin_temple')]:
 d=json.loads(orig.read('data/sonicraft/worldgen/structure/'+name+'.json'));r=json.loads(orig.read('data/sonicraft/worldgen/structure_set/'+name+'.json'))['placement'];bio=d['biomes'];bio=[bio] if isinstance(bio,str) else bio
 sites.append(dict(key=key,biomes=[b.split(':')[1] for b in bio],rule=[r['spacing'],r['separation'],r['salt']]))
sites=[s for s in sites if s['key']!='aquaticlake']
assert 'deathegg' not in [s['key'] for s in sites]
put('assets/sonicraft/backport/sites43.json',sites);report['new_natural_sites']=sites[-3:]
# All registered block definitions plus their already-ported item data. No invented loot replacements.
known={'sonicraft:'+d['id'] for d in json.loads(base.read('assets/sonicraft/backport/blocks.json'))+json.loads(base.read('assets/sonicraft/backport/foods.json'))}
# Content adds definitions in manifests, not all of them are listed in the initial blocks.json.
for n in ['completion_025.json','completion_026.json','final_pass_024.json','industrial_022.json','motion_023.json']:
 obj=json.loads(base.read('assets/sonicraft/backport/'+n))
 if isinstance(obj,dict):known.update('sonicraft:'+s for s in obj.get('new_ids',[]) if isinstance(s,str))
known.update('sonicraft:item_box_'+s for s in ['sonic','tails','knuckles','amy','big','blaze','charmy','cream','espio','omega','rouge','shadow','silver','vector'])
known.add('sonicraft:red_star_ring')
known.update('sonicraft:'+s for s in ['special_stage_key','badnik_scrap','piko_piko_hammer','bubblegum'])
# Determine further registrations from P30's inventory of items, if available.
for n in base.namelist():
 if n.startswith('META-INF/sonicraft-p30/') and n.endswith('.json') and 'recipe' not in n:
  pass
aliases={'cod':('fish',0),'salmon':('fish',1),'tropical_fish':('fish',2),'pufferfish':('fish',3),'ink_sac':('dye',0),'lapis_lazuli':('dye',4),'light_blue_dye':('dye',12),'magenta_dye':('dye',13),'yellow_dye':('dye',11),'lime_concrete':('concrete',5),'enchanted_golden_apple':('golden_apple',1)}
newvanilla={'amethyst_shard','nautilus_shell','raw_iron','basalt','netherite_scrap','powder_snow_bucket','copper_ingot'}
omit=collections.Counter();restored=collections.Counter();tables=[]
for n in orig.namelist():
 if not n.endswith('.json') or not(n.startswith('data/sonicraft/loot_tables/chests/') or n in ['data/sonicraft/loot_tables/mobius_loot.json','data/sonicraft/loot_tables/night_carnival_chest.json']):continue
 d=json.loads(orig.read(n));d.pop('type',None)
 for pi,pool in enumerate(d['pools']):
  pool['name']='source_pool_'+str(pi)
  for ei,e in enumerate(pool['entries']):
   name=e['name'];short=name.split(':')[1];e['type']='item';e['entryName']='source_entry_'+str(ei)
   if name.startswith('minecraft:') and short in aliases:
    mapped,meta=aliases[short];e['name']='minecraft:'+mapped;e.setdefault('functions',[]).append({'function':'set_data','data':meta})
   elif name.startswith('sonicraft:') and name not in known or name.startswith('minecraft:') and short in newvanilla:
    omit[name]+=1;e.clear();e.update(type='empty',entryName='source_entry_'+str(ei),weight=json.loads(orig.read(n))['pools'][pi]['entries'][ei]['weight']);continue
   restored[name]+=1
 tables.append(n.split('/loot_tables/')[1]);put('assets/sonicraft/loot_tables/'+tables[-1],d)
report['loot']={'tables':tables,'available_entries':sum(restored.values()),'unsupported_entries':dict(omit),'policy':'Original weights and counts; unsupported entries remain weighted empty, not unrelated substitutes.'}
# Use the original idle asset for the four real pistons; normalize gecko 4 vector wrappers to GeckoLib 3.
g=json.loads(orig.read('assets/sonicraft/animations/egg_crusher.animation.json'))
def normalize(v):
 if isinstance(v,dict):
  if set(v)=={'vector'}:return normalize(v['vector'])
  return {k:normalize(x) for k,x in v.items()}
 if isinstance(v,list):return [normalize(x) for x in v]
 return v
put('assets/sonicraft/animations/egg_crusher.animation.json',normalize(g))
for n in ['geo/egg_crusher.geo.json','textures/entities/egg_crusher.png']:put('assets/sonicraft/'+n,orig.read('assets/sonicraft/'+n))
# New messages and names; do not overwrite existing translations.
labels={'en_us':['The magnet is incomplete.','There is not enough height above the magnet.','Death Egg could not be placed. Clear the area above and try again.','Eggman (Scrap Brain)','Egg Crusher piston'], 'ru_ru':['Магнит не собран.','Над магнитом недостаточно места по высоте.','Не удалось разместить Яйцо смерти. Освободите пространство над магнитом и попробуйте снова.','Эггман (Scrap Brain)','Поршень Egg Crusher']}
for lang,values in labels.items():
 n='assets/sonicraft/lang/'+lang+'.lang';keys=['sonicraft.magnet.incomplete','sonicraft.magnet.height','sonicraft.magnet.blocked','entity.sonicraft.scrap_brain_eggman.name','entity.sonicraft.egg_crusher_piston.name'];put(n,base.read(n)+('\n'+''.join(k+'='+v+'\n' for k,v in zip(keys,values))).encode())
for n,b in changes.items():
 p=ROOT/'assets'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b)
(ROOT/'asset_report.json').write_text(json.dumps(report,indent=2,ensure_ascii=False))
print('Assets:',len(changes),'loot entries',sum(restored.values()),'unsupported',sum(omit.values()),'mask templates',len(removed))
