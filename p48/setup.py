from pathlib import Path
import shutil,re
r=Path('p48');s=r/'src/org/sonicraft/backport'
for n in ['Remap.java','LinkCheck.java','GeckoLoadTest.java','nbt43.py']:shutil.copy('p47/'+n,r/n)
f=Path('p48/inspect/org/sonicraft/backport/MobiusBadnikSpawner.java').read_text()
a=f.index('        for (Badniks.Definition definition : Badniks.DEFINITIONS) {');b=f.index('\n    static List<String>',a)
f=f[:a]+'''        for (Badniks.Definition d : Badniks.DEFINITIONS) {
            if(d.id.equals("egg_crusher"))continue;
            if(d.spawns!=null)for(Badniks.Spawn spawn:d.spawns)add(spawn.biomes.replace("sonicraft:",""),d.id,spawn.weight);
            String[] zones=ADAPTED_ZONES.get(d.id);
            if(zones!=null)for(String zone:zones)add(zone,d.id,6);
        }
    }
''' + f[b:]
f=f.replace('    static {','    static class Entry { final String id; final int weight; Entry(String id,int weight){this.id=id;this.weight=weight;} }\n    static {')
# Only count actual successful spawns against the running global cap.
f=f.replace('                    ++spawned;','                    ++spawned;\n                    ++n;').replace('if (++n < globalCap)','if (n < globalCap)')
(s/'MobiusBadnikSpawner.java').write_text(f)
# Original Eggrobo model, converted using the proven pawn conversion path.
f=Path('p46/model.py').read_text().replace('Modelegg_pawn.java','Modeleggrobo.java').replace("assert len(names)==21,len(names)","assert len(names)>0")
f=f.replace('PawnModel46','EggroboModel48').replace("Path('p46/src/","Path('p48/src/")
# bone names are inspected below before animation adaptation
Path('p48/model.py').write_text(f)
