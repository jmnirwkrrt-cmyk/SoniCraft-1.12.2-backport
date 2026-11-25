import zipfile,json,copy
from pathlib import Path
from nbt43 import *
root=Path(__file__).parent;b=zipfile.ZipFile(root/'base-mcp.jar');o=zipfile.ZipFile(root.parent/'input/Minecraft мод/sonicraft-1.10.1-forge-1.20.1.jar')
def put(n,v):
 p=root/'assets'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(v if isinstance(v,bytes) else (json.dumps(v,ensure_ascii=False,indent=2)+'\n').encode())
# Source geometry converted into static 1.12 block elements, with original per-face UVs.
for variant in ['', '_upside_down']:
 g=json.loads(o.read('assets/sonicraft/geo/studiopolis_camera'+variant+'.geo.json'))['minecraft:geometry'][0];els=[];desc=g['description']
 for bone in g['bones']:
  assert not any(bone.get('rotation',[0,0,0])),bone
  for c in bone.get('cubes',[]):
   assert not any(c.get('rotation',[0,0,0])),c
   origin=c['origin'];size=c['size'];frm=[origin[0]+8,origin[1],origin[2]+8];to=[a+s for a,s in zip(frm,size)];faces={}
   for face,uv in c['uv'].items():
    u,v=uv['uv'];du,dv=uv['uv_size'];faces[face]={'texture':'#all','uv':[u*16/desc['texture_width'],v*16/desc['texture_height'],(u+du)*16/desc['texture_width'],(v+dv)*16/desc['texture_height']]}
   els.append({'from':frm,'to':to,'faces':faces})
 put('assets/sonicraft/models/block/camera46'+variant+'.json',{'textures':{'all':'sonicraft:blocks/studiopolis_camera','particle':'sonicraft:blocks/studiopolis_camera'},'elements':els})
# Source geometry converted into static 1.12 block elements, with original per-face UVs.
for variant in ['', '_active']:
 g=json.loads(o.read('assets/sonicraft/geo/starpost.geo.json'))['minecraft:geometry'][0];els=[];desc=g['description']
 for bone in g['bones']:
  if bone['name']=='base_s1':continue
  assert not any(bone.get('rotation',[0,0,0])),bone
  for c in bone.get('cubes',[]):
   assert not any(c.get('rotation',[0,0,0])),c
   origin=c['origin'];size=c['size'];frm=[origin[0]+8,origin[1],origin[2]+8];to=[a+s for a,s in zip(frm,size)];faces={}
   uvs=c['uv']
   if isinstance(uvs,list):
    u,v=uvs;x,y,z=size
    uvs={f:{'uv':uv,'uv_size':sz} for f,uv,sz in [('north',[u+z,v+z],[x,y]),('south',[u+2*z+x,v+z],[x,y]),('west',[u,v+z],[z,y]),('east',[u+z+x,v+z],[z,y]),('up',[u+z,v],[x,z]),('down',[u+z+x,v],[x,z])]}
   for face,uv in uvs.items():
    u,v=uv['uv'];du,dv=uv['uv_size'];faces[face]={'texture':'#all','uv':[u*16/desc['texture_width'],v*16/desc['texture_height'],(u+du)*16/desc['texture_width'],(v+dv)*16/desc['texture_height']]}
   els.append({'from':frm,'to':to,'faces':faces})
 put('assets/sonicraft/models/block/checkpoint46'+variant+'.json',{'textures':{'all':'sonicraft:blocks/starpost_sonic_2'+variant,'particle':'sonicraft:blocks/starpost_sonic_2'+variant},'elements':els})
state=json.loads(b.read('assets/sonicraft/blockstates/studiopolis_camera.json'))
for k,v in state['variants'].items():v['model']='sonicraft:camera46'+('_upside_down' if 'blockstate=1' in k else '')
put('assets/sonicraft/blockstates/studiopolis_camera.json',state)
put('assets/sonicraft/models/item/studiopolis_camera.json',{'parent':'sonicraft:block/camera46'})
state=json.loads(b.read('assets/sonicraft/blockstates/starpost_sonic_2.json'))
for k,v in state['variants'].items():
 v['model']='sonicraft:checkpoint46'+('_active' if 'blockstate=2' in k else '')
for facing in ['north','east','south','west']:state['variants']['blockstate=3,facing='+facing]=copy.deepcopy(state['variants']['blockstate=2,facing='+facing])
put('assets/sonicraft/blockstates/starpost_sonic_2.json',state)
# Original structure puts the sign over the east entry pressure plate. Move it west, beyond the arena.
prefix='assets/sonicraft/structures_1201/';r=loads(b.read(prefix+'studiopolis_city_egg_tv_studio.nbt'));pal=ls(r,'palette');cells={pos(c):c for c in ls(r,'blocks')}
old=cells[(19,26,23)];assert pal[old['state'][1]]['Name'][1]=='sonicraft:signpost';new=cells[(7,25,23)];assert pal[new['state'][1]]['Name'][1]=='minecraft:air';old['state'],new['state']=new['state'],old['state']
put(prefix+'studiopolis_city_egg_tv_studio.nbt',dumps(r))
# Additional city building sites ensure the four original buildings can appear independently of a full road graph.
pools=json.loads(b.read('assets/sonicraft/backport/complex_pools_022.json'));sites=json.loads(b.read('assets/sonicraft/backport/sites43.json'))
for i in range(1,5):
 key='studiobuilding'+str(i);pools['roots'][key]='studiopolis_city_building_'+str(i);sites.append({'key':key,'biomes':['studiopolis_zone'],'rule':[8,6,13946000+i]})
put('assets/sonicraft/backport/complex_pools_022.json',pools);put('assets/sonicraft/backport/sites43.json',sites)
# Natural spawn roster for the original ordinary pawn is test_zone, not every city biome.
for n in ['egg_pawn','egg_pawn_glow']:
 put('assets/sonicraft/textures/entities/'+n+'.png',o.read('assets/sonicraft/textures/entities/'+n+'.png'))
print('PASS camera original geometry, checkpoint variants, exit sign and four building sites')

put('assets/sonicraft/textures/blocks/starpost_sonic_2_active.png',o.read('assets/sonicraft/textures/block/starpost_sonic_2_active.png'))

for lang,lines in [('en_us','entity.sonicraft.egg_pawn.name=Egg Pawn\nsonicraft.checkpoint_set=Respawn point set\n'),('ru_ru','entity.sonicraft.egg_pawn.name=Эгг-пешка\nsonicraft.checkpoint_set=Точка возрождения установлена\n')]:
 n='assets/sonicraft/lang/'+lang+'.lang';put(n,b.read(n)+b'\n'+lines.encode())
