package org.sonicraft.backport;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.dimension.solar.StellarBody;
import zmaster587.advancedRocketry.dimension.DimensionManager;
import zmaster587.advancedRocketry.dimension.DimensionProperties;
/** Called before players join, after AR's serverAboutToStart galaxy load. */
final class MobiusSpaceAR {
    static void link(int id) {
        DimensionManager galaxy = DimensionManager.getInstance();
        DimensionProperties p;
        if (galaxy.isDimensionCreated(id)) {
            p = galaxy.getDimensionProperties(id);
            if (p.getId()!=id || p.isNativeDimension || !"Mobius".equalsIgnoreCase(p.getName())) {
                MobiusSpace.LOG.warning("AR already assigns DIM "+id+" elsewhere. Leaving its planet unchanged."); return;
            }
            // Reuse the administrator's orbit/atmosphere and the canonical star after reload.
            StellarBody host = galaxy.getStar(p.getStarId());
            if (host == null) { MobiusSpace.LOG.warning("Missing host star for existing Mobius"); return; }
            p.setStar(host);
        } else {
            StellarBody home = galaxy.getDimensionProperties(0).getStar();
            if (home == null) { MobiusSpace.LOG.warning("AR overworld has no host star; Mobius not mapped."); return; }
            p = new DimensionProperties(id,"Mobius");
            p.isNativeDimension=false;
            p.orbitalDist=125;
            p.averageTemperature=288;
            p.hasOxygen=true;
            if (!galaxy.registerDimNoUpdate(p,false)) return;
            p.setStar(home);
        }
        ARConfiguration.getCurrentConfig().initiallyKnownPlanets.add(id);
        galaxy.knownPlanets.add(id);
        MobiusSpace.LOG.info("Linked Mobius as an external planet: DIM "+id+", star "+p.getStarId());
    }
}
