from nbt43 import *
from collections import deque
import json
r=loads(open('p44/assets/assets/sonicraft/structures_1201/scrap_brain_factory.nbt','rb').read());pal=ls(r,'palette');g={pos(b):pal[b['state'][1]]['Name'][1] for b in ls(r,'blocks')}
passable={'minecraft:air','minecraft:water','sonicraft:gold_ring','sonicraft:player_teleporter_point_a','sonicraft:scrap_brain_vine'}
def clear(p):return all(g.get((p[0],p[1]+dy,p[2])) in passable for dy in [0,1])
start=(12,8,12);target=(4,15,6);q=deque([start]);prev={start:None}
while q:
 p=q.popleft()
 if p==target:break
 for dx,dy,dz in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
  n=(p[0]+dx,p[1]+dy,p[2]+dz)
  if 1<=n[1]<=16 and n not in prev and clear(n):prev[n]=p;q.append(n)
print('Two-block-clear connectivity:',target in prev,'reachable cells',len(prev))
if target in prev:
 path=[];p=target
 while p is not None:path.append(p);p=prev[p]
 path.reverse();print('steps',len(path),'spring column accessible:',(4,8,6) in prev)
 open('p44/route_path.json','w').write(json.dumps(path))
