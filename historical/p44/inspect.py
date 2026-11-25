import sys,zipfile,json,collections
sys.path.insert(0,'p43');from nbt43 import *
z=zipfile.ZipFile('p43/release/sonicraft-legacy-1.12.2-0.1.27-p43-worldfix.jar')
for n in z.namelist():
 if n.startswith('assets/sonicraft/structures_1201/') and 'scrap' in n:
  r=loads(z.read(n));print('\n',n,ls(r,'size'));pal=ls(r,'palette')
  for b in ls(r,'blocks'):
   id=pal[b['state'][1]]['Name'][1]
   if any(s in id for s in ['jigsaw','elevator','crusher','collaps','switch','boss']):
    print(pos(b),id,b.get('nbt',''),pal[b['state'][1]].get('Properties',''))
