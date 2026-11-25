# Preserve NBT tag types (including empty lists) and foreign entity/tile payloads.
import gzip,io,struct,collections
def edit(raw,name):
 f=io.BytesIO(gzip.decompress(raw));fmt={1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}
 def read(t):
  def num(k):return struct.unpack('>'+k,f.read(struct.calcsize('>'+k)))[0]
  if t in fmt:return num(fmt[t])
  if t==8:return f.read(num('H')).decode()
  if t==10:
   v={}
   while k:=num('B'):
    key=read(8);v[key]=(k,read(k))
   return v
  if t==9:
   k=num('B');return k,[read(k) for _ in range(num('i'))]
  if t in (7,11,12):return f.read(num('i')*{7:1,11:4,12:8}[t])
  raise ValueError(t)
 assert f.read(1)==b'\x0a';rootname=read(8);root=read(10);assert not f.read()
 def enc(t,v):
  if t in fmt:return struct.pack('>'+fmt[t],v)
  if t==8:
   b=v.encode();return struct.pack('>H',len(b))+b
  if t==10:return b''.join(bytes([k])+enc(8,n)+enc(k,x) for n,(k,x) in v.items())+b'\x00'
  if t==9:
   k,l=v;return struct.pack('>Bi',k,len(l))+b''.join(enc(k,x) for x in l)
  return struct.pack('>i',len(v)//{7:1,11:4,12:8}[t])+v
 def ls(d,k):return d[k][1][1]
 pal=ls(root,'palette');blocks=ls(root,'blocks');entities=ls(root,'entities');coord=lambda b:tuple(ls(b,'pos'))
 grid={coord(b):b for b in blocks};ids=[b['Name'][1] for b in pal]
 def ident(p):return ids[grid[p]['state'][1]] if p in grid else ''
 def setid(p,id):
  if id not in ids:ids.append(id);pal.append({'Name':(8,id)})
  grid[p]['state']=(3,ids.index(id));grid[p].pop('nbt',None)
 if name in ('aquatic_ruin_lake','seaside_hill_lake'):
  # Keep one full lining layer around water and every carved/special block;
  # do not replace tens of thousands of natural underground blocks with identical filler.
  fill='sonicraft:chaostone';keep=set()
  for x,y,z in grid:
   if ident((x,y,z))!=fill:
    keep.update(((x,y,z),(x+1,y,z),(x-1,y,z),(x,y+1,z),(x,y-1,z),(x,y,z+1),(x,y,z-1)))
  blocks[:]=[b for b in blocks if coord(b) in keep]
  assert len(blocks)<70000
  return gzip.compress(b'\x0a'+enc(8,rootname)+enc(10,root),mtime=0)
 air='minecraft:air';sign='sonicraft:signpost';positions=[]
 for e in entities:
  n=e['nbt'][1]['id'][1];pos=tuple(ls(e,'blockPos'))
  if not n.endswith('_spawnpoint') or n.endswith('egg_crusher_spawnpoint'):continue
  key=n.replace('_spawnpoint','_spawn_trigger');marks=[p for p in grid if ident(p)==key]
  if not marks and key!='sonicraft:death_egg_robot_spawn_trigger':continue
  if pos in positions:continue
  positions.append(pos)
  for p in marks:setid(p,air)
  assert ident(pos) in (air,'minecraft:structure_void',key),(name,pos,ident(pos))
  setid(pos,key)
 # Choose a walkable two-block opening on an existing arena floor by the original trigger.
 initial=[coord(b) for b in blocks if ident(coord(b)).endswith('_spawn_trigger')]
 assert initial,name
 x,y,z=initial[0]
 candidates=[p for p in grid if abs(p[0]-x)<=12 and abs(p[2]-z)<=12 and abs(p[1]-y)<=12 and ident(p)==air and ident((p[0],p[1]+1,p[2]))==air and ident((p[0],p[1]-1,p[2])) not in ('',air) and any(q in ident((p[0],p[1]-1,p[2])) for q in ('floor','road','iron_block','concrete','planks','bricks','metal','factory_wall','yellow_tile','factory_block')) and min((p[0]-a)**2+(p[2]-c)**2 for a,b,c in initial)>=9]
 assert candidates,(name,initial)
 sp=min(candidates,key=lambda p:(abs(p[1]-y)*4+abs(p[0]-x)+abs(p[2]-z),p));setid(sp,sign)
 if name=='big_arms_arena':
  # Elevated structure: omit exterior air only; retain every enclosed room.
  sx,sy,sz=ls(root,'size');outside=set();q=collections.deque(p for p in grid if (0 in p or p[0]==sx-1 or p[1]==sy-1 or p[2]==sz-1) and ident(p)==air)
  while q:
   p=q.popleft()
   if p in outside:continue
   outside.add(p);x,y,z=p
   for r in ((x-1,y,z),(x+1,y,z),(x,y-1,z),(x,y+1,z),(x,y,z-1),(x,y,z+1)):
    if r not in outside and ident(r)==air:q.append(r)
  blocks[:]=[b for b in blocks if coord(b) not in outside]
 assert len(blocks)<=70000
 return gzip.compress(b'\x0a'+enc(8,rootname)+enc(10,root),mtime=0)
