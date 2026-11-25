import zipfile,json,collections
from pathlib import Path
from nbt43 import *
R=Path(__file__).resolve().parent
z=zipfile.ZipFile(R.parent/'p43/release/sonicraft-legacy-1.12.2-0.1.27-p43-worldfix.jar')
def put(n,data):
 p=R/'assets'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
report={}
for template in ['scrap_brain_factory','scrap_brain_factory_dungeon']:
 n='assets/sonicraft/structures_1201/'+template+'.nbt';r=loads(z.read(n));pal=ls(r,'palette');cells={pos(b):b for b in ls(r,'blocks')}
 water={'Name':(8,'minecraft:water'),'Properties':(10,{'level':(8,'0')})}
 if water not in pal:pal.append(water)
 changed=[]
 for x in range(11,14):
  for zz in range(11,14):
   b=cells[x,7,zz];assert pal[b['state'][1]]['Name'][1]=='sonicraft:smooth_scrap_brain_stone'
   assert pal[cells[x,6,zz]['state'][1]]['Name'][1] not in ['minecraft:air','minecraft:water']
   b['state']=(3,pal.index(water));changed.append([x,7,zz])
 put(n,dumps(r));report[template]={'new_landing_water':changed}
for lang,msg in [('ru_ru','Может, я и в форме друга, но пока мы не подружимся, тискать себя не дам!'),('en_us',"I may be friend-shaped, but until we become friends, I won't let you cuddle me!")]:
 n='assets/sonicraft/lang/'+lang+'.lang';put(n,z.read(n)+('\nsonicraft.companion.friend_first='+msg+'\n').encode())
# Audit source teleports throughout the port, not just the factory.
uses=[]
for n in z.namelist():
 if n.startswith('assets/sonicraft/structures_1201/') and n.endswith('.nbt'):
  r=loads(z.read(n));pal=ls(r,'palette');a=[list(pos(b)) for b in ls(r,'blocks') if pal[b['state'][1]]['Name'][1]=='sonicraft:player_teleporter_point_a'];b=[ls(e,'pos') for e in ls(r,'entities') if e['nbt'][1]['id'][1]=='sonicraft:player_teleporter_point_b_entity']
  if a or b:uses.append({'template':n.rsplit('/',1)[1],'entrances':a,'destinations':b})
report['teleport_templates']=uses
(R/'asset_report.json').write_text(json.dumps(report,indent=2));print('Patched landing pools; audited',len(uses),'teleport templates')
