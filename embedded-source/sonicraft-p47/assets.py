import zipfile,json,copy,re
from pathlib import Path
from nbt43 import *
root=Path(__file__).parent;b=zipfile.ZipFile(root/'base-mcp.jar');o=zipfile.ZipFile(root.parent/'input/Minecraft мод/sonicraft-1.10.1-forge-1.20.1.jar');report={};changes={}
def put(n,v):
 raw=v if isinstance(v,bytes) else (json.dumps(v,ensure_ascii=False,indent=2)+'\n').encode();changes[n]=raw;p=root/'assets'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(raw)
# Cover all metadata values exposed by the ported properties, preserving every existing variant.
for id,maximum in [('night_carnival_curtain_light',3),('cpz_factory_pillar_corner',3),('press_garden_redstone_bulb_detector',15)]:
 n='assets/sonicraft/blockstates/'+id+'.json';d=json.loads(b.read(n));v=d['variants'];added=[]
 for key,value in list(v.items()):
  if 'blockstate=' not in key:continue
  for state in range(maximum+1):
   k=re.sub(r'blockstate=\d+','blockstate='+str(state),key)
   if k not in v:v[k]=copy.deepcopy(value);added.append(k)
 put(n,d);report[id]=added
# Forge 1.12 demands explicit metadata on subtype ingredients/results, including slabs.
fixed=[]
for n in b.namelist():
 if not n.startswith('assets/sonicraft/recipes/') or not n.endswith('.json'):continue
 d=json.loads(b.read(n));changed=[False]
 def walk(v):
  if isinstance(v,dict):
   if 'item' in v and 'data' not in v:v['data']=0;changed[0]=True
   for x in v.values():walk(x)
  elif isinstance(v,list):
   for x in v:walk(x)
 walk(d)
 if changed[0]:put(n,d);fixed.append(n)
report['recipe_metadata']=len(fixed)
prefix='assets/sonicraft/structures_1201/'
# The runway is a required part of Tails's workshop, including its original plane spawn.
r=loads(b.read(prefix+'tails_lab.nbt'));d=loads(b.read(prefix+'tails_lab_runway.nbt'));pal=ls(r,'palette');blocks=ls(r,'blocks');entities=ls(r,'entities');r['entities']=(9,(10,entities))
for c in blocks:ls(c,'pos')[0]+=15
for c in ls(d,'blocks'):
 c=copy.deepcopy(c);ls(c,'pos')[2]+=2;s=ls(d,'palette')[c['state'][1]]
 if s not in pal:pal.append(s)
 c['state']=(3,pal.index(s));blocks.append(c)
for c in blocks:
 if pal[c['state'][1]]['Name'][1]=='minecraft:jigsaw':
  air={'Name':(8,'minecraft:air')}
  if air not in pal:pal.append(air)
  c['state']=(3,pal.index(air));c.pop('nbt',None)
for e in ls(d,'entities'):
 e=copy.deepcopy(e)
 for key in ['pos','blockPos']:ls(e,key)[2]+=2
 entities.append(e)
r['size']=(9,(3,[26,10,11]));put(prefix+'tails_lab.nbt',dumps(r))
r=loads(b.read(prefix+'big_the_cat_house_green_hill.nbt'));pal=ls(r,'palette');ls(r,'blocks')[:]=[c for c in ls(r,'blocks') if pal[c['state'][1]]['Name'][1] not in ('minecraft:structure_block','minecraft:bedrock')];put(prefix+'big_the_cat_house_green_hill.nbt',dumps(r))
pools=json.loads(b.read('assets/sonicraft/backport/complex_pools_022.json'));pools['roots'].update(bighouse='big_the_cat_house_green_hill',tailsworkshop='tails_lab');put('assets/sonicraft/backport/complex_pools_022.json',pools)
sites=json.loads(b.read('assets/sonicraft/backport/sites43.json'));sites += [dict(key='bighouse',biomes=['green_hill_zone'],rule=[14,10,1947001]),dict(key='tailsworkshop',biomes=['green_hill_zone','emerald_hill_zone'],rule=[16,12,1947002])];put('assets/sonicraft/backport/sites43.json',sites)
# Original Gecko geometry and flight animation, not a substitute aircraft model.
for n in ['geo/tornado_plane.geo.json','animations/tornado_plane.animation.json','textures/entities/tornado.png']:
 put('assets/sonicraft/'+n,o.read('assets/sonicraft/'+n))
# Flat inventory icon from the original item atlas when available.
icons=[n for n in o.namelist() if n.startswith('assets/sonicraft/textures/item/') and ('tornado' in n or 'plane' in n) and n.endswith('.png')]
report['plane_icons']=icons
if icons:put('assets/sonicraft/textures/items/tornado47.png',o.read('assets/sonicraft/textures/item/tornado_item.png'))
else:put('assets/sonicraft/textures/items/tornado47.png',o.read('assets/sonicraft/textures/entities/tornado.png'))
put('assets/sonicraft/models/item/tornado_item.json',{'parent':'item/generated','textures':{'layer0':'sonicraft:items/tornado47'}})
put('assets/sonicraft/recipes/tornado47.json',{'type':'minecraft:crafting_shaped','pattern':['WWW','IEI','R R'],'key':{'W':{'item':'minecraft:planks','data':0},'I':{'item':'minecraft:iron_ingot','data':0},'E':{'item':'minecraft:diamond','data':0},'R':{'item':'minecraft:redstone','data':0}},'result':{'item':'sonicraft:tornado_item','data':0}})
for lang,lines in [('en_us','entity.sonicraft.tornado.name=Tornado\nitem.sonicraft.tornado_item.name=Tornado\nkey.sonicraft.flight_down=Aircraft: descend\n'),('ru_ru','entity.sonicraft.tornado.name=Торнадо\nitem.sonicraft.tornado_item.name=Самолёт «Торнадо»\nkey.sonicraft.flight_down=Самолёт: снижение\n')]:
 n='assets/sonicraft/lang/'+lang+'.lang';put(n,b.read(n)+b'\n'+lines.encode())
(root/'asset_report.json').write_text(json.dumps(report,ensure_ascii=False,indent=2));print('Assets prepared:',len(changes),'plane icon candidates:',icons)

# GeckoLib 3 expects constant vector objects under a numeric keyframe.
n='assets/sonicraft/animations/tornado_plane.animation.json';anim=json.loads(changes[n])
for animation in anim['animations'].values():
 for bone in animation.get('bones',{}).values():
  for channel,v in list(bone.items()):
   if isinstance(v,dict) and 'vector' in v:bone[channel]={'0.0':v}
put(n,anim)
