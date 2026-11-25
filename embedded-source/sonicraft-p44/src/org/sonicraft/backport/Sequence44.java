package org.sonicraft.backport;
import net.minecraft.world.World;import net.minecraft.util.math.BlockPos;
public final class Sequence44 {
 public static void register(World w,BlockPos platform,BlockPos window,BlockPos roof){ScrapBrainSequence.register(w,platform.up(18),window.up(18),roof.up(18));}
}
