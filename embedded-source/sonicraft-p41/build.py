import zipfile,hashlib,json,sys
from pathlib import Path
from templates import edit
src,orig,out=map(Path,sys.argv[1:]);p=Path(__file__).parent;assert hashlib.sha256(src.read_bytes()).hexdigest()=='3d19344ee2caf66c11c5e9a4b896400838cc65fd64d071e7fb6a7ef6c83f93f5'
with zipfile.ZipFile(src) as z,zipfile.ZipFile(orig) as o,zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as w:
 ch={f.relative_to(p/'bin').as_posix():f.read_bytes() for f in (p/'bin').rglob('*.class')};assert len(ch)==12
 for n in ['mcmod.info','org/sonicraft/backport/SonicraftLegacy.class']:
  a=z.read(n);old=b'0.1.27-p40-dashring';new=b'0.1.27-p41-frontier';assert len(old)==len(new) and a.count(old)==1;ch[n]=a.replace(old,new)
 for n in ('chemical_plant_city','press_garden_factory','star_light_city_center','studiopolis_city_egg_tv_studio','cpz_metal_sonic_arena','chemical_plant_city_metal_sonic_arena','death_egg','scrap_brain_factory','big_arms_arena','aquatic_ruin_lake','seaside_hill_lake'):
  k='assets/sonicraft/structures_1201/'+n+'.nbt';ch[k]=edit(z.read(k),n)
 k='assets/sonicraft/backport/complex_pools_022.json';d=json.loads(z.read(k));d['roots'].update(aquaticlake='aquatic_ruin_lake',seasidelake='seaside_hill_lake',metalpit='cpz_metal_sonic_arena',deathegg='death_egg',bigarms='big_arms_arena')
 for pool in ['seaside_hill_lake_island']:
  srcpool=json.loads(o.read('data/sonicraft/worldgen/template_pool/'+pool+'.json'));assert 'sonicraft:'+pool not in {p['id'] for p in d['pools']}
  d['pools'].append({'id':'sonicraft:'+pool,'entries':[{'id':e['element']['location'].split(':')[1],'weight':e['weight']} for e in srcpool['elements']]})
 ch[k]=json.dumps(d,separators=(',',':')).encode()
 sn='assets/sonicraft/sounds.json';sounds=json.loads(z.read(sn));sounds['balloon.pop']=json.loads(o.read(sn))['balloon.pop']
 for e in sounds['balloon.pop']['sounds']:
  ns,file=e['name'].split(':');n=f'assets/{ns}/sounds/{file}.ogg';ch[n]=o.read(n);assert ch[n][:4]==b'OggS'
 ch[sn]=json.dumps(sounds,separators=(',',':')).encode()
 for lang,label in [('en_us','Metal Sonic (Chemical Plant)'),('ru_ru','Метал Соник (Chemical Plant)')]:
  n='assets/sonicraft/lang/'+lang+'.lang';ch[n]=z.read(n)+('\nentity.sonicraft.metal_sonic_boss.name='+label+'\n').encode()
 for n in z.namelist():
  if n not in ch:w.writestr(n,z.read(n))
 for n,b in ch.items():w.writestr(n,b)
 for n in ['build.py','templates.py','Patch.java','P34Sites.java','MetalArenaBoss.java','Util41.java','README.txt']:w.write(p/n,'META-INF/sonicraft-p41/'+n)
with zipfile.ZipFile(out) as v:assert v.testzip() is None and len(v.namelist())==len(set(v.namelist()))
