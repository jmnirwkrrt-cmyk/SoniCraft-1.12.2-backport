"""Usage: python3 build.py P29.jar original.jar output.jar"""
import sys,zipfile,json,collections,hashlib,struct,pathlib
from itertools import chain
def js(x):return (json.dumps(x,indent=2,ensure_ascii=False)+'\n').encode()
base,orig,out=map(pathlib.Path,sys.argv[1:4])
a='assets/sonicraft/';r=a+'recipes/';version='0.1.27-p30-crafting'
with zipfile.ZipFile(base) as b,zipfile.ZipFile(orig) as o:
 files=set(b.namelist());assert len(files)==len(b.namelist()) and 'META-INF/sonicraft-p29/provenance.json' in files
 defs={x['id']:x['kind'] for x in json.loads(b.read(a+'backport/blocks.json'))}
 safe=set('bulk_cube bulk_stairs shape_slab shape_wall rock bulk_fence bulk_gate bulk_door bulk_pane ground wood log metal zone_rock axis facade_axis facade_horizontal bulk_pressure_plate'.split())
 def ported(i):return i=='sonicraft:badnik_scrap' or (i.startswith('sonicraft:') and defs.get(i.split(':')[1]) in safe)
 vanilla=set('stick iron_ingot sandstone red_sandstone iron_bars redstone gold_ingot stone glass sand clay_ball cobblestone brick paper diamond emerald string coal glowstone_dust'.split())
 def ingredient(i):return ported(i) or (i.startswith('minecraft:') and i.split(':')[1] in vanilla)
 def layout(d):
  def val(v):return (v.get('item',''),v.get('data',0)) if isinstance(v,dict) else ('?',0)
  if 'pattern' in d:
   grid=tuple(tuple(val(d['key'].get(c,{})) for c in row) for row in d['pattern'])
   return ('grid',min(grid,tuple(tuple(reversed(row)) for row in grid)))
  return ('mix',tuple(sorted(val(v) for v in d.get('ingredients',[]))))
 def bag(key):
  return tuple(sorted(v for v in chain.from_iterable(key[1]) if v[0])) if key[0]=='grid' else key[1]
 def clash(k,l):return k==l or (k[0]!=l[0] and bag(k)==bag(l))
 existing=[layout(json.loads(b.read(n))) for n in files if n.startswith(r) and n.endswith('.json')]
 options={};skips=[]
 for n in sorted(o.namelist()):
  if not n.startswith('data/sonicraft/recipes/') or not n.endswith('.json'):continue
  dest=r+n.split('/')[-1]
  if dest in files:continue
  d=json.loads(o.read(n))
  if d.get('type') not in ['minecraft:crafting_shaped','minecraft:crafting_shapeless'] or not ported(d.get('result',{}).get('item','')):continue
  vals=list(d.get('key',{}).values()) or d.get('ingredients',[])
  if not vals or not all(isinstance(v,dict) and set(v)=={'item'} and ingredient(v['item']) for v in vals):continue
  if not any(ported(v['item']) for v in vals) and not n.endswith('/aquatic_ruin_bricks_recipe.json'):continue
  d.pop('category',None);assert set(d)<=set('type group pattern key ingredients result'.split())
  if 'pattern' in d:
   rows=d['pattern'];assert 1<=len(rows)<=3 and len(set(map(len,rows)))==1 and 1<=len(rows[0])<=3 and set(''.join(rows))-{' '}==set(d['key'])
  else:assert 1<=len(vals)<=9
  assert 1<=d['result'].get('count',1)<=64
  options[dest]=d
 changes={}
 for n,d in options.items():
  key=layout(d)
  if any(clash(key,k) for k in existing) or any(m!=n and clash(key,layout(other)) for m,other in options.items()):skips.append(n);continue
  changes[n]=js(d)
 names=sorted(changes);assert names
 meta=json.loads(b.read('mcmod.info'));assert meta[0]['version']=='0.1.27-p29-springs';meta[0]['version']=version;changes['mcmod.info']=js(meta)
 cls='org/sonicraft/backport/SonicraftLegacy.class'
 def utf(v):v=v.encode();return b'\x01'+struct.pack('>H',len(v))+v
 raw=b.read(cls);before=utf('0.1.27-p26-casino');assert raw.count(before)==1;changes[cls]=raw.replace(before,utf(version))
 report={'base_sha256':hashlib.sha256(base.read_bytes()).hexdigest(),'original_sha256':hashlib.sha256(orig.read_bytes()).hexdigest(),'added':names,'excluded_conflicts':skips}
 changes['META-INF/sonicraft-p30/recipes.json']=js(report);changes['META-INF/sonicraft-p30/build.py']=pathlib.Path(__file__).read_bytes()
 with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as w:
  for n in b.namelist():w.writestr(n,changes.get(n,b.read(n)))
  for n in sorted(changes.keys()-files):w.writestr(n,changes[n])
 with zipfile.ZipFile(out) as w:
  assert not w.testzip() and len(w.namelist())==len(set(w.namelist()))
  for n in files-{'mcmod.info',cls}:assert b.read(n)==w.read(n),n
  for n in names:
   original=json.loads(o.read('data/sonicraft/recipes/'+n.split('/')[-1]));original.pop('category',None);assert original==json.loads(w.read(n))
 print('Added',len(names),'conflicting skipped',len(skips));print('\n'.join(names))
