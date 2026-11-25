package org.sonicraft.backport;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.dimension.IDimensionProperties;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
final class MobiusSpaceAR {
 static final String NAME="Mobius Prime";
 static boolean link(int id){
  DimensionManager g=DimensionManager.getInstance();
  DimensionProperties earth=g.getDimensionProperties(0);
  StellarBody sol=earth.getStar();
  if(sol==null)return false;
  boolean existing=g.isDimensionCreated(id);
  DimensionProperties p=existing?g.getDimensionProperties(id):new DimensionProperties(id,"Mobius");
  if(existing&&(p.getId()!=id||p.isNativeDimension||!"Mobius".equalsIgnoreCase(p.getName()))){
   MobiusSpace.LOG.warning("AR DIM collision: "+id);return false;
  }
  StellarBody star=null;
  for(StellarBody s:g.getStars())if(s.getId()!=sol.getId() && NAME.equals(s.getName()) && !s.isBlackHole() && s.getTemperature()==sol.getTemperature() && s.getSize()==sol.getSize() && (star==null||s.getId()<star.getId()))star=s;
  if(star==null){
   int x=0,z=0;boolean free=false;
   for(int i=1;i<=512&&!free;i++){
    x=sol.getPosX()+(sol.getPosX()>0?-1:1)*(24+(i-1)/16);z=sol.getPosZ()+(sol.getPosZ()>0?-1:1)*((i-1)%16);
    free=true;for(StellarBody s:g.getStars())if(s.getPosX()==x&&s.getPosZ()==z){free=false;break;}
   }
   if(!free)return false;
   int sid=g.getNextFreeStarId();if(sid<0)return false;
   star=new StellarBody();star.setId(sid);star.setName(NAME);star.setTemperature(sol.getTemperature());star.setSize(sol.getSize());star.setPosX(x);star.setPosZ(z);g.addStar(star);
  }
  p.isNativeDimension=false;
  p.gravitationalMultiplier=earth.gravitationalMultiplier;p.hasOxygen=earth.hasOxygen;p.rotationalPeriod=earth.rotationalPeriod;
  p.setAtmosphereDensityDirect(earth.getAtmosphereDensity());p.orbitalDist=earth.getSolarOrbitalDistance();
  if(!existing&&!g.registerDimNoUpdate(p,false))return false;
  for(StellarBody s:g.getStars())if(s!=star)for(IDimensionProperties old:s.getPlanets())if(old.getId()==id){s.removePlanet(old);break;}
  p.setStar(star);p.averageTemperature=p.getAverageTemp();
  ARConfiguration.getCurrentConfig().initiallyKnownPlanets.add(id);g.knownPlanets.add(id);
  MobiusSpace.LOG.info("Mobius DIM "+id+" orbits "+NAME+" (star "+star.getId()+"). Inbound golden portal disabled.");return true;
 }
}
