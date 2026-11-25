import json,zipfile,pathlib,re
from pathlib import Path
base=Path('work34/base/assets/sonicraft');out=Path('work34/patch/assets/sonicraft')
orig=zipfile.ZipFile(next(Path('input').rglob('sonicraft*.jar')))
def put(path,b):
 p=out/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b if isinstance(b,bytes) else b.encode())
def js(p,v):put(p,json.dumps(v,ensure_ascii=False,indent=1)+'\n')
def read(p):return json.loads(orig.read('assets/sonicraft/'+p))
blocks=json.loads((base/'backport/blocks.json').read_text());added=[]
def add(id,kind):
 if id in [d['id'] for d in blocks]:return
 blocks.append(dict(id=id,kind=kind,hardness=1,resistance=10,zeroLight=True));added.append(id)
 bs=read('blockstates/'+id+'.json');vs={}
 def model(s):
  if not s.startswith('sonicraft:'):return
  rel=s.split(':')[1];p='models/'+rel+'.json'
  if (out/p).exists():return
  d=read(p)
  for k,t in d.get('textures',{}).items():
   if not t.startswith('sonicraft:'):continue
   old=t.split(':')[1];new=old.replace('block/','blocks/',1);d['textures'][k]='sonicraft:'+new
   for ext in ['.png','.png.mcmeta']:
    path='assets/sonicraft/textures/'+old+ext
    if path in orig.namelist():put('textures/'+new+ext,orig.read(path))
  js(p,d);model(d.get('parent',''))
 for k,v in bs['variants'].items():
  filt=','.join(x for x in k.split(',') if not x.startswith('waterlogged=')) or 'normal'
  if isinstance(v,list):raise ValueError(id)
  model(v['model']);vs[filt]={**v,'model':v['model'].replace('sonicraft:block/','sonicraft:')}
 js('blockstates/'+id+'.json',dict(variants=vs));js('models/item/'+id+'.json',dict(parent=next(iter(vs.values()))['model'].replace('sonicraft:','sonicraft:block/')))
for id in ['cpz_syringe_pipe','cpz_syringe_button_pressed','mega_mack_block_green','mega_mack_block_blue_to_green','mega_mack_block_blue_to_light_blue','mega_mack_block_light_blue_to_green','scrap_brain_spinning_foothold','night_carnival_switch_platform_on','cpz_pipe_corner_y_axis']:
 add(id,'facade_horizontal' if id in ['scrap_brain_spinning_foothold','night_carnival_switch_platform_on','cpz_pipe_corner_y_axis'] else 'source_cutout')
for stage in [1,2,3]:add('night_carnival_switch_wireless_connector_stage_'+str(stage),'source_cutout')
js('backport/blocks.json',blocks)
for id in ['oil','mega_mack']:
 js('blockstates/'+id+'.json',{'variants':{**{'normal':{'model':'sonicraft:'+id}},**{'level='+str(i):{'model':'sonicraft:'+id} for i in range(16)}}})
 js('models/item/'+id+'.json',{'parent':'sonicraft:block/'+id})
for loc in ['en_us','ru_ru']:
 p='lang/'+loc+'.lang';s=(base/p).read_text();d=read('lang/en_us.json')
 names={id:d.get('block.sonicraft.'+id,id) for id in added+['mega_mack','oil']}
 if loc=='ru_ru': names.update(oil='Нефть',mega_mack='Мега-Мак',cpz_syringe_pipe='Пустая секция шприца',cpz_syringe_button_pressed='Нажатый поршень шприца')
 for id,v in names.items():
  k='tile.sonicraft.'+id+'.name';s=re.sub('(?m)^'+re.escape(k)+'=.*\n?','',s);s+='\n'+k+'='+v+'\n'
 put(p,s)
print('added',added)
p='backport/complex_pools_022.json';d=json.loads((base/p).read_text());d['roots'].update(miragebar='mirage_saloon_bar',chaogarden='chao_garden',mackpool='chemical_plant_mega_mack_pool',oceanpalace='ocean_palace',hillmountain='hill_top_mountain_1')
for pool in ['chao_garden_waterfall','chemical_plant_mega_mack_pool_bottom']:
 spec=json.loads(orig.read('data/sonicraft/worldgen/template_pool/'+pool+'.json'));entries=[dict(id=x['element']['location'].split(':')[-1],weight=x['weight']) for x in spec['elements'] if 'location' in x['element']]
 if not any(v['id']=='sonicraft:'+pool for v in d['pools']):d['pools'].append(dict(id='sonicraft:'+pool,entries=entries))
js(p,d)
for loc,names in [('en_us',('Oil','Mega Mack')),('ru_ru',('Нефть','Мега-Мак'))]:
 p='lang/'+loc+'.lang';raw=(out/p).read_text()
 for id,value in zip(['oil','mega_mack'],names):
  for k in ['fluid.'+id,'fluid.sonicraft.'+id]:raw+='\n'+k+'='+value
 put(p,raw+'\n')
