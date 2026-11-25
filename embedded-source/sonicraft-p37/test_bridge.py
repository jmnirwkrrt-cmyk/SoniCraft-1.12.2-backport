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
sources['zmaster587.advancedRocketry.api.dimension.IDimensionProperties']='public interface IDimensionProperties{int getId();}'
k='zmaster587.advancedRocketry.api.dimension.solar.StellarBody'
sources[k]=sources[k].replace('public int id;',"""public int id,x,z,temp=100; public float size=1; String name="Sol";java.util.Map<Integer,zmaster587.advancedRocketry.api.dimension.IDimensionProperties> planets=new java.util.HashMap<>();
public StellarBody(){}public int getId(){return id;}public void setId(int i){id=i;} public int getPosX(){return x;}public void setPosX(int i){x=i;}public int getPosZ(){return z;}public void setPosZ(int i){z=i;}
public int getTemperature(){return temp;}public void setTemperature(int t){temp=t;}public float getSize(){return size;}public void setSize(float s){size=s;}public boolean isBlackHole(){return false;}public String getName(){return name;}public void setName(String s){name=s;}
public void addPlanet(zmaster587.advancedRocketry.api.dimension.IDimensionProperties p){planets.put(p.getId(),p);}public java.util.List<zmaster587.advancedRocketry.api.dimension.IDimensionProperties> getPlanets(){return new java.util.ArrayList<>(planets.values());}
public zmaster587.advancedRocketry.api.dimension.IDimensionProperties removePlanet(zmaster587.advancedRocketry.api.dimension.IDimensionProperties p){return planets.remove(p.getId());}""")
k='zmaster587.advancedRocketry.dimension.DimensionProperties'
sources[k]=sources[k].replace('class DimensionProperties{','class DimensionProperties implements zmaster587.advancedRocketry.api.dimension.IDimensionProperties{ public int rotationalPeriod,air=100; public float gravitationalMultiplier;public int getAtmosphereDensity(){return air;} public void setAtmosphereDensityDirect(int a){air=a;}public int getSolarOrbitalDistance(){return orbitalDist;}public int getAverageTemp(){return star.getTemperature()+air-orbitalDist;}').replace('star=s;', 'star=s;s.addPlanet(this);')
k='zmaster587.advancedRocketry.dimension.DimensionManager'
sources[k]=sources[k].replace('public static DimensionManager getInstance()', 'public Map<Integer,StellarBody> stars=new HashMap<>();public Collection<StellarBody> getStars(){return stars.values();}public void addStar(StellarBody s){stars.put(s.getId(),s);}public int getNextFreeStarId(){int i=0;while(stars.containsKey(i))i++;return i;}public static DimensionManager getInstance()')
k='org.sonicraft.backport.VerifyP36'
s0=sources[k];s0=s0[:s0.index('MobiusSpace.serverStarting(993);')]+"""m.addStar(m.home);earth.orbitalDist=101;earth.gravitationalMultiplier=1;earth.rotationalPeriod=24000;earth.hasOxygen=true;
DimensionProperties p=new DimensionProperties(993,"Mobius");p.isNativeDimension=false;p.setStar(m.home);m.map.put(993,p);
MobiusSpace.serverStarting(993);assert p.getStar()!=m.home&&m.stars.size()==2&&m.home.getPlanets().size()==1;assert p.getStar().getPlanets().contains(p)&&p.getAverageTemp()==earth.getAverageTemp()&&p.hasOxygen&&p.rotationalPeriod==24000&&p.gravitationalMultiplier==1;
assert MobiusSpace.blocksPortalEntry(0)&&!MobiusSpace.blocksPortalEntry(993);MobiusSpace.serverStarting(993);assert m.stars.size()==2&&p.getStar().getPlanets().size()==1;
m.map.remove(993);MobiusSpace.serverStarting(993);assert m.map.get(993)!=p&&m.stars.size()==2&&m.knownPlanets.contains(993);
MobiusSpace.serverStarting(11230);assert !MobiusSpace.blocksPortalEntry(0);System.out.println("PASS migration / single star galaxy / climate / reload / fresh mapping / portal fallback");}}"""
sources[k]=s0
with TemporaryDirectory() as tmp:
 t=Path(tmp); files=[]
 for name,body in sources.items():
  p=t/(name.replace('.','/')+'.java');p.parent.mkdir(exist_ok=True,parents=True);p.write_text('package '+name.rsplit('.',1)[0]+';'+body);files.append(str(p))
 run([str(jdk/'bin/javac'),'-cp',str(P/'bin'),'-d',str(t)]+files,check=True)
 run([str(jdk/'bin/java'),'-ea','-cp',str(t)+':'+str(P/'bin'),'org.sonicraft.backport.VerifyP36'],check=True)
