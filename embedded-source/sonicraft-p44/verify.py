import sys,zipfile,json,struct
from collections import deque
from nbt43 import *
z=zipfile.ZipFile(sys.argv[1]);assert z.testzip() is None and len(z.namelist())==len(set(z.namelist()))
p='assets/sonicraft/structures_1201/'
r=loads(z.read(p+'scrap_brain_factory.nbt'));pal=ls(r,'palette');g={pos(b):pal[b['state'][1]]['Name'][1] for b in ls(r,'blocks')}
for x in range(11,14):
 for zz in range(11,14):
  assert g[x,7,zz]=='minecraft:water'
  assert all(g[x,y,zz]=='minecraft:air' for y in range(8,36))
  assert g[x,36,zz]=='sonicraft:scrap_brain_collapsing_platform'
assert sum(v=='minecraft:water' for v in g.values())==671
assert g[4,7,6]=='sonicraft:red_spring'
assert all(g[4,y,6]=='minecraft:air' for y in range(8,16))
assert g[4,16,6]=='sonicraft:player_teleporter_point_a'
targets=[ls(e,'pos') for e in ls(r,'entities') if e['nbt'][1]['id'][1]=='sonicraft:player_teleporter_point_b_entity'];assert targets==[[20.5,44.,8.5]]
rot=[lambda x,z:(x,z),lambda x,z:(25-z,x),lambda x,z:(25-x,25-z),lambda x,z:(z,25-x)]
for f in rot:
 ax,az=f(4.5,6.5);bx,bz=f(20.5,8.5);assert abs(ax-bx)<32 and abs(az-bz)<32 and 44-16<32
passable={'minecraft:air','minecraft:water','sonicraft:gold_ring','sonicraft:player_teleporter_point_a','sonicraft:scrap_brain_vine'}
def clear(p):return all(g.get((p[0],p[1]+dy,p[2])) in passable for dy in [0,1])
q=deque([(12,8,12)]);seen=set(q)
while q:
 x,y,zz=q.popleft()
 for dx,dy,dz in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
  v=x+dx,y+dy,zz+dz
  if 1<=v[1]<=16 and v not in seen and clear(v):seen.add(v);q.append(v)
assert (4,8,6) in seen and (4,15,6) in seen
msg='Может, я и в форме друга, но пока мы не подружимся, тискать себя не дам!'
assert 'sonicraft.companion.friend_first='+msg in z.read('assets/sonicraft/lang/ru_ru.lang').decode()
src=z.read('META-INF/sonicraft-p44/src/org/sonicraft/backport/Companions42.java').decode();assert src.count('if(!cuddleFriend(')==2
assert 'p.getUniqueID().equals(n.getUniqueId(OWNER))' in src
for n in z.namelist():
 if n.startswith('org/sonicraft/') and n.endswith('.class'):assert struct.unpack('>H',z.read(n)[6:8])[0]<=52
print('PASS: landing pool, clear fall shaft, geometric route, spring-to-portal shaft, destination in all rotations, friend gates, exact message, archive and Java 8')
