/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.fml.common.Loader
 *  org.sonicraft.backport.MobiusSpaceAR
 */
package org.sonicraft.backport;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraftforge.fml.common.Loader;
import org.sonicraft.backport.MobiusSpaceAR;

public final class MobiusSpace {
    static final Logger LOG = Logger.getLogger("SoniCraft/AdvancedRocketry");
    private static boolean linked;

    public static boolean blocksPortalEntry(int n) {
        return n == 0 && linked;
    }

    private MobiusSpace() {
    }

    public static int freshDefault() {
        return 1123;
    }

    public static void serverStarting(int n) {
        linked = false;
        if (!Loader.isModLoaded((String)"advancedrocketry")) {
            return;
        }
        if (n < 2 || n >= 10000) {
            LOG.warning("Mobius DIM " + n + " is outside AR's planet range (2..9999). Existing world left unchanged. For an existing save use the P48 copy-only migrate_mobius.py tool; do not simply change mobiusId. New installations default to 1123 even without AR.");
            return;
        }
        try {
            linked = MobiusSpaceAR.link((int)n);
        }
        catch (LinkageError | RuntimeException throwable) {
            LOG.log(Level.WARNING, "Unable to link Mobius DIM " + n + " to Advanced Rocketry; portal remains available.", throwable);
        }
    }
}
