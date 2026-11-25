"""python build.py P34.jar original-1.20.1.jar output.jar (Python 3.8+)"""
import collections,hashlib,json,pathlib,struct,sys,zipfile
base,original,out=map(pathlib.Path,sys.argv[1:])
prefix='assets/sonicraft/recipes/'
version='0.1.27-p35-zone-crafting'
def dump(obj):return (json.dumps(obj,ensure_ascii=False,indent=2)+'\n').encode()
def norm(d):
 def token(x):return x['item'],x.get('data',0)
 if 'pattern' not in d:return None,tuple(sorted(token(v) for v in d['ingredients']))
 grid=[[token(d['key'][c]) if c!=' ' else None for c in row] for row in d['pattern']]
 while not any(grid[0]):grid.pop(0)
 while not any(grid[-1]):grid.pop()
 while not any(row[0] for row in grid):grid=[row[1:] for row in grid]
 while not any(row[-1] for row in grid):grid=[row[:-1] for row in grid]
 return tuple(map(tuple,grid)),tuple(sorted(x for row in grid for x in row if x))
def clash(a,b):
 g,v=a;h,w=b
 if v!=w:return False
 return g==None or h==None or g==h or g==tuple(tuple(reversed(row)) for row in h)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
with zipfile.ZipFile(base) as z,zipfile.ZipFile(original) as o:
 f={n:z.read(n) for n in z.namelist()};assert len(f)==len(z.namelist()) and z.testzip()==None
 old='0.1.27-p34-zone-mechanics';meta=json.loads(f['mcmod.info']);assert meta[0]['version']==old
 defs={'sonicraft:'+d['id'] for d in json.loads(f['assets/sonicraft/backport/blocks.json'])}
 vanilla=set('redstone iron_ingot crafting_table glass water_bucket blaze_powder nether_star gold_ingot gold_nugget iron_block string repeater diamond piston stick quartz vine'.split())
 special={'red_carpet':('carpet',14),'yellow_carpet':('carpet',4),'lapis_lazuli':('dye',4),'spruce_log':('log',1)}
 def ingredient(x):
  if x in defs:return {'item':x}
  ns,k=x.split(':')
  if ns=='minecraft' and k in vanilla:return {'item':x}
  if ns=='minecraft' and k in special:
   dest,m=special[k];return {'item':'minecraft:'+dest,'data':m}
  raise KeyError(x)
 existing=[norm(json.loads(raw)) for n,raw in f.items() if n.startswith(prefix) and n.endswith('.json')]; candidates={};added={};skipped={}
 for n in o.namelist():
  if not n.startswith('data/sonicraft/recipes/') or not n.endswith('.json'):continue
  d=json.loads(o.read(n));dest=prefix+n.rsplit('/',1)[1]
  if dest in f or d.get('type') not in ('minecraft:crafting_shaped','minecraft:crafting_shapeless') or d['result']['item'] not in defs:continue
  d.pop('category',None); vals=list(d.get('key',{}).values()) or d.get('ingredients',[])
  tags=[x for x in vals if 'tag' in x]
  if tags:
   # Expand a single log-tag ingredient into separate, concrete recipes, only for registered forms.
   if len(vals)!=1 or 'pattern' in d or d['result']['item'].split(':')[-1] not in ('hillpine_planks','green_hill_palm_planks','genesis_oak_planks'):continue
   ns,k=tags[0]['tag'].split(':')
   for log in json.loads(o.read('data/'+ns+'/tags/items/'+k+'.json'))['values']:
    if log in defs:
     candidates[dest[:-5]+'_from_'+log.split(':')[1]+'.json']=(dict(d,ingredients=[ingredient(log)]),n)
   continue
  if not any(v.get('item') in defs for v in vals):continue
  try:
   for v in vals:
    new=ingredient(v['item']);v.clear();v.update(new)
  except (KeyError,TypeError):continue
  candidates[dest]=d,n
 for name,(d,src) in candidates.items():
  key=norm(d)
  if any(clash(key,x) for x in existing) or any(other!=name and clash(key,norm(t[0])) for other,t in candidates.items()):
   skipped[name]='overlapping recipe';continue
  assert name not in f;added[name]=src;f[name]=dump(d)
 assert added
 meta[0]['version']=version;f['mcmod.info']=dump(meta);c='org/sonicraft/backport/SonicraftLegacy.class'
 def utf(x):return b'\x01'+struct.pack('>H',len(x))+x.encode()
 assert f[c].count(utf(old))==1;f[c]=f[c].replace(utf(old),utf(version))
 f['META-INF/sonicraft-p35/build.py']=pathlib.Path(__file__).read_bytes()
 f['META-INF/sonicraft-p35/recipes.json']=dump({'base_sha256':sha(base),'original_sha256':sha(original),'added':added,'skipped_overlap':skipped})
 with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as q:
  for n,b in f.items():q.writestr(n,b)
 print(len(added),'added:',[n.split('/')[-1] for n in added]);print('skipped',len(skipped))
