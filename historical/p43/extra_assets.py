import json,zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parent
b=zipfile.ZipFile(ROOT.parent/'social42/release/sonicraft-legacy-1.12.2-0.1.27-p42-companions.jar')
o=zipfile.ZipFile(ROOT.parent/'input/Minecraft мод/sonicraft-1.10.1-forge-1.20.1.jar')
def put(n,v):
 p=ROOT/'assets'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(v if isinstance(v,bytes) else (json.dumps(v,ensure_ascii=False,indent=2)+'\n').encode())
def get(n):
 p=ROOT/'assets'/n;return p.read_bytes() if p.exists() else b.read(n)
def model(n):
 d=json.loads(o.read(n));d.pop('render_type',None)
 s=json.dumps(d).replace('sonicraft:block/','sonicraft:blocks/').replace('sonicraft:item/','sonicraft:items/')
 d=json.loads(s);put(n,d)
 for t in d.get('textures',{}).values():
  if t.startswith('sonicraft:'):
   dst='assets/sonicraft/textures/'+t.split(':')[1]+'.png';src=dst.replace('/blocks/','/block/').replace('/items/','/item/')
   if src in o.namelist():put(dst,o.read(src))
 # Reuse the existing adapted parent mesh.
blocks=json.loads(b.read('assets/sonicraft/backport/blocks.json'))
recipes=[];adapted={}
for n in o.namelist():
 if not ('/recipes/' in n and '_item_box_recipe.json' in n):continue
 d=json.loads(o.read(n));d.pop('category',None);id=d['result']['item'].split(':')[1]
 blocks.append(dict(id=id,kind='character_monitor',hardness=1.0,resistance=10,material='metal',modelNonOpaque=True))
 for ing in d['ingredients']:
  old=dict(ing)
  if ing.get('tag')=='sonicraft:fishing_rods':ing.clear();ing['item']='minecraft:fishing_rod'
  elif ing.get('tag')=='sonicraft:chao_jewels':continue
  else:
   replace={'minecraft:honey_bottle':'minecraft:sugar','minecraft:cocoa_beans':'minecraft:dye','sonicraft:shuriken_star':'minecraft:arrow','sonicraft:time_machine_frame':'minecraft:clock','sonicraft:redstone_badnik_core':'sonicraft:badnik_scrap'}
   if ing.get('item') in replace:
    ing['item']=replace[ing['item']]
    if ing['item']=='minecraft:dye':ing['data']=3
  if old!=ing:adapted[json.dumps(old)]=ing
 d['ingredients']=[[{'item':'minecraft:diamond'},{'item':'minecraft:emerald'}] if x.get('tag')=='sonicraft:chao_jewels' else x for x in d['ingredients']]
 put('assets/sonicraft/recipes/'+n.rsplit('/',1)[1],d);recipes.append(id)
 model('assets/sonicraft/models/block/'+id+'.json')
 put('assets/sonicraft/models/item/'+id+'.json',{'parent':'sonicraft:block/'+id})
 put('assets/sonicraft/blockstates/'+id+'.json',{'variants':{'facing='+f:{'model':'sonicraft:'+id,'y':y} for f,y in [('north',0),('east',90),('south',180),('west',270)]}})
put('assets/sonicraft/backport/blocks.json',blocks)
for x in blocks:
 if 'leaves' not in x['id']:continue
 n='assets/sonicraft/blockstates/'+x['id']+'.json';d=json.loads(b.read(n));v=d['variants'].get('normal',next(iter(d['variants'].values())))
 put(n,{'variants':{'check_decay='+c+',decayable='+a:v for c in ['false','true'] for a in ['false','true']}})
model('assets/sonicraft/models/item/red_star_ring.json')
d=json.loads(o.read('data/sonicraft/recipes/item_box_recipe.json'));d.pop('category',None);d['key']['b']={'item':'minecraft:glass'}
put('assets/sonicraft/recipes/item_box_recipe.json',d)
# Explicit backport recipe: original red star collection mechanic is not implemented.
put('assets/sonicraft/recipes/red_star_ring_p43.json',{'type':'minecraft:crafting_shaped','pattern':[' R ','RER',' R '],'key':{'R':{'item':'sonicraft:gold_ring'},'E':{'item':'minecraft:emerald'}},'result':{'item':'sonicraft:red_star_ring'}})
for lang in ['en_us','ru_ru']:
 src=json.loads(o.read('assets/sonicraft/lang/'+lang+'.json'));n='assets/sonicraft/lang/'+lang+'.lang';s=get(n).decode()+'\n'
 for id in recipes:s+='tile.sonicraft.'+id+'.name='+src.get('block.sonicraft.'+id,id)+'\n'
 s+='item.sonicraft.red_star_ring.name='+src.get('item.sonicraft.red_star_ring','Red Star Ring')+'\n';put(n,s.encode())
(ROOT/'extra_asset_report.json').write_text(json.dumps({'character_monitors':recipes,'recipe_adaptations':adapted,'red_star_ring_recipe':'4 rings + emerald','leaves':[x['id'] for x in blocks if 'leaves' in x['id']]},indent=2))
print('Added',len(recipes),'character monitors and 8 true leaf blocks')
