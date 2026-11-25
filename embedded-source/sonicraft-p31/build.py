"""Usage: python build.py P30.jar Original.jar Out.jar JDK8-bin ASM-core.jar ASM-tree.jar"""
import sys,zipfile,json,pathlib,hashlib,struct,tempfile,subprocess,os,collections,gzip,re
base,original,out,jdk,asm,tree=map(lambda x:pathlib.Path(x).resolve(),sys.argv[1:]);here=pathlib.Path(__file__).parent
enc=lambda v:(json.dumps(v,indent=2,ensure_ascii=False)+'\n').encode()
def utf(s):
 b=s.encode();return b'\x01'+struct.pack('>H',len(b))+b
version='0.1.27-p31-zone-loot'; before='0.1.27-p30-crafting'
with zipfile.ZipFile(base) as b,zipfile.ZipFile(original) as o,tempfile.TemporaryDirectory() as td:
 t=pathlib.Path(td); files=set(b.namelist());assert len(files)==len(b.namelist()) and 'META-INF/sonicraft-p30/recipes.json' in files
 a='assets/sonicraft/'; blocks=json.loads(b.read(a+'backport/blocks.json'));foods=json.loads(b.read(a+'backport/foods.json'))
 known={'sonicraft:'+d['id'] for d in blocks+foods};known.update(['sonicraft:special_stage_key','sonicraft:badnik_scrap','sonicraft:piko_piko_hammer'])
 vanilla=set('blaze_rod coal comparator compass diamond emerald feather fire_charge fishing_rod glass gold_ingot gunpowder ice iron_boots iron_chestplate iron_helmet iron_horse_armor iron_ingot iron_leggings iron_nugget iron_sword lever packed_ice paper quartz redstone redstone_lamp redstone_torch repeater rotten_flesh shield snowball'.split())
 aliases={'cod':('fish',0),'salmon':('fish',1),'tropical_fish':('fish',2),'pufferfish':('fish',3),'ink_sac':('dye',0),'lapis_lazuli':('dye',4),'light_blue_dye':('dye',12),'magenta_dye':('dye',13),'yellow_dye':('dye',11),'lime_concrete':('concrete',5),'enchanted_golden_apple':('golden_apple',1)}
 changes={}; tables=[]; skips=collections.Counter(); active=0;empty=0
 for n in sorted(o.namelist()):
  if not n.endswith('.json') or not(n.startswith('data/sonicraft/loot_tables/chests/') or n=='data/sonicraft/loot_tables/night_carnival_chest.json'):continue
  name=n.split('/loot_tables/')[1][:-5];tables.append('sonicraft:'+name);d=json.loads(o.read(n));pools=[]
  for pi,p in enumerate(d['pools']):
   assert not set(p)-{'rolls','bonus_rolls','entries'};p=dict(p);entries=[]
   for ei,e in enumerate(p['entries']):
    assert set(e)=={'type','name','weight','functions'} and e['type']=='minecraft:item'
    original_id=e['name'];short=original_id.split(':')[1];f=list(e['functions']);assert all(x['function'] in ['set_count','enchant_with_levels'] for x in f)
    value=original_id
    if original_id.startswith('minecraft:') and short in aliases:
     value,meta=aliases[short];value='minecraft:'+value;f.append({'function':'set_data','data':meta})
    elif not(original_id in known or original_id.startswith('minecraft:') and short in vanilla):value=None
    entry={'entryName':'slot_'+str(ei),'weight':e['weight']}
    if value:entry.update(type='item',name=value,functions=f);active+=1
    else:entry.update(type='empty');skips[original_id]+=1;empty+=1
    entries.append(entry)
   p.update(name='pool_'+str(pi),entries=entries);pools.append(p)
  changes[a+'loot_tables/'+name+'.json']=enc({'pools':pools})
 assert len(tables)==11
 src='package org.sonicraft.backport; public final class ZoneChestLoot {public static boolean accept(String id,String vanilla){if(id.startsWith(vanilla))return true;switch(id){'+''.join('case '+json.dumps(s)+':' for s in tables)+'return true;default:return false;}}}'
 (t/'ZoneChestLoot.java').write_text(src);sp='org/sonicraft/backport/StructurePort.class';(t/'SP.class').write_bytes(b.read(sp))
 cp=os.pathsep.join(map(str,[asm,tree,t]));subprocess.run([jdk/'javac','-cp',cp,'-d',t,t/'ZoneChestLoot.java',here/'Patch.java'],check=True);subprocess.run([jdk/'java','-cp',cp,'Patch',t/'SP.class'],check=True)
 helper='org/sonicraft/backport/ZoneChestLoot.class';changes[helper]=(t/helper).read_bytes();changes[sp]=(t/'SP.class').read_bytes()
 (t/'Check.java').write_text('import org.sonicraft.backport.ZoneChestLoot;public class Check{public static void main(String[] args){for(String s:args)if(!ZoneChestLoot.accept(s,"minecraft:chests/"))throw new AssertionError(s);for(String s:new String[]{"","other:chests/test","sonicraft:chests/fake"})if(ZoneChestLoot.accept(s,"minecraft:chests/"))throw new AssertionError(s);}}');subprocess.run([jdk/'javac','-cp',cp,'-d',t,t/'Check.java'],check=True);subprocess.run([jdk/'java','-Xverify:all','-cp',cp,'Check','minecraft:chests/simple_dungeon',*tables],check=True)
 cls='org/sonicraft/backport/SonicraftLegacy.class';raw=b.read(cls);assert raw.count(utf(before))==1;changes[cls]=raw.replace(utf(before),utf(version))
 info=json.loads(b.read('mcmod.info'));assert info[0]['version']==before;info[0]['version']=version;changes['mcmod.info']=enc(info)
 counts=collections.Counter()
 for n in files:
  if n.endswith('.nbt'):
   for k in re.findall(rb'LootTable..(sonicraft:[a-z_/0-9]+)',gzip.decompress(b.read(n))):
    if k.decode() in tables:counts[k.decode()]+=1
 assert set(counts)==set(tables)
 meta='META-INF/sonicraft-p31/';changes[meta+'build.py']=pathlib.Path(__file__).read_bytes();changes[meta+'Patch.java']=(here/'Patch.java').read_bytes();changes[meta+'ZoneChestLoot.java']=src.encode();report=dict(source_sha256=hashlib.sha256(original.read_bytes()).hexdigest(),base_sha256=hashlib.sha256(base.read_bytes()).hexdigest(),references=counts,implemented_entries=active,empty_entries=empty,unavailable_ids=skips);changes[meta+'loot.json']=enc(report)
 with zipfile.ZipFile(out,'w',zipfile.ZIP_DEFLATED) as z:
  for n in b.namelist():z.writestr(n,changes.get(n,b.read(n)))
  for n in sorted(changes.keys()-files):z.writestr(n,changes[n])
 with zipfile.ZipFile(out) as z:
  assert z.testzip() is None and len(z.namelist())==len(set(z.namelist()))
  for n in files-changes.keys():assert z.read(n)==b.read(n)
 print(version,len(tables),'tables',sum(counts.values()),'references',active,'supported entries',empty,'weighted empty')
