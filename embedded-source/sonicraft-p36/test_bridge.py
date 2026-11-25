"""Exercise the compiled P36 bridge against isolated AR/Forge lifecycle doubles."""
from pathlib import Path
from tempfile import TemporaryDirectory
from subprocess import run
import sys
P=Path(__file__).resolve().parent
jdk=Path(sys.argv[1]).resolve()
sources={
'net.minecraftforge.fml.common.Loader': 'public class Loader {public static boolean ar;public static boolean isModLoaded(String id){return ar&&id.equals("advancedrocketry");}}',
'zmaster587.advancedRocketry.api.ARConfiguration':'import java.util.*; public class ARConfiguration{public HashSet<Integer> initiallyKnownPlanets=new HashSet<>(); static ARConfiguration c=new ARConfiguration(); public static ARConfiguration getCurrentConfig(){return c;}}',
'zmaster587.advancedRocketry.api.dimension.solar.StellarBody':'public class StellarBody{public int id;public StellarBody(int i){id=i;}}',
'zmaster587.advancedRocketry.dimension.DimensionProperties':'''import zmaster587.advancedRocketry.api.dimension.solar.*; public class DimensionProperties{int id;String name;public boolean isNativeDimension=true,hasOxygen;public int orbitalDist,averageTemperature; StellarBody star;public DimensionProperties(int i,String n){id=i;name=n;} public int getId(){return id;} public String getName(){return name;} public int getStarId(){return star.id;} public StellarBody getStar(){return star;} public void setStar(StellarBody s){star=s;}}''',
'zmaster587.advancedRocketry.dimension.DimensionManager':'''import java.util.*;import zmaster587.advancedRocketry.api.dimension.solar.*;public class DimensionManager{static DimensionManager m=new DimensionManager();public Set<Integer> knownPlanets=new HashSet<>();public Map<Integer,DimensionProperties> map=new HashMap<>();public StellarBody home=new StellarBody(3);public static DimensionManager getInstance(){return m;}public StellarBody getStar(int i){return i==3?home:null;}public boolean isDimensionCreated(int id){return map.containsKey(id);} public DimensionProperties getDimensionProperties(int id){return map.get(id);}public boolean registerDimNoUpdate(DimensionProperties p,boolean forge){if(forge)throw new AssertionError("double Forge registration");return map.putIfAbsent(p.id,p)==null;}}''',
'org.sonicraft.backport.VerifyP36':'''import zmaster587.advancedRocketry.dimension.*;import net.minecraftforge.fml.common.Loader; public class VerifyP36{public static void main(String[] args){DimensionManager m=DimensionManager.getInstance();DimensionProperties earth=new DimensionProperties(0,"Earth");earth.setStar(m.home);m.map.put(0,earth);assert MobiusSpace.freshDefault()==11230;MobiusSpace.serverStarting(900);assert m.map.size()==1;Loader.ar=true;assert MobiusSpace.freshDefault()==1123;MobiusSpace.serverStarting(11230);MobiusSpace.serverStarting(10000);assert m.map.size()==1;MobiusSpace.serverStarting(993);DimensionProperties p=m.map.get(993);assert p.getName().equals("Mobius")&&!p.isNativeDimension&&p.getStar()==m.home&&p.hasOxygen&&m.knownPlanets.contains(993);p.orbitalDist=163;MobiusSpace.serverStarting(993);assert m.map.get(993)==p&&p.orbitalDist==163;DimensionProperties other=new DimensionProperties(453,"Mine");m.map.put(453,other);MobiusSpace.serverStarting(453);assert m.map.get(453)==other&&!m.knownPlanets.contains(453);System.out.println("PASS: no AR, reserved star IDs, configured ID, host star, external mapping, reload and conflict");}}'''}
with TemporaryDirectory() as tmp:
 t=Path(tmp); files=[]
 for name,body in sources.items():
  p=t/(name.replace('.','/')+'.java');p.parent.mkdir(exist_ok=True,parents=True);p.write_text('package '+name.rsplit('.',1)[0]+';'+body);files.append(str(p))
 run([str(jdk/'bin/javac'),'-cp',str(P/'bin'),'-d',str(t)]+files,check=True)
 run([str(jdk/'bin/java'),'-ea','-cp',str(t)+':'+str(P/'bin'),'org.sonicraft.backport.VerifyP36'],check=True)
