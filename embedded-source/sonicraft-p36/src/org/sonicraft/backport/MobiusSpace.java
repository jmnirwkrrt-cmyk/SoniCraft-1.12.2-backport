package org.sonicraft.backport;
import java.util.logging.Logger;
import net.minecraftforge.fml.common.Loader;
/** Keep every optional AR symbol out of classes needed by a standalone server. */
public final class MobiusSpace {
    static final Logger LOG = Logger.getLogger("SoniCraft/AdvancedRocketry");
    private MobiusSpace() {}
    public static int freshDefault() { return Loader.isModLoaded("advancedrocketry") ? 1123 : 11230; }
    public static void serverStarting(int id) {
        if (!Loader.isModLoaded("advancedrocketry")) return;
        if (id < 2 || id >= 10000) {
            LOG.warning("Mobius DIM "+id+" is outside AR's planet range (2..9999). Existing world left unchanged; select a free dimensions.mobiusId before creating a NEW world.");
            return;
        }
        try { MobiusSpaceAR.link(id); }
        catch (LinkageError | RuntimeException e) {
            LOG.log(java.util.logging.Level.WARNING,"Unable to link Mobius DIM "+id+" to Advanced Rocketry; portal remains available.",e);
        }
    }
}
