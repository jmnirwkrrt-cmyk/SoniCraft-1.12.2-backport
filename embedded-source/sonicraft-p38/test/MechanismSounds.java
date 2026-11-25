package org.sonicraft.backport;
import net.minecraft.world.World;import net.minecraft.util.math.BlockPos;
class MechanismSounds {
 static int count;static String last;
 static void play(World w,BlockPos p,String id){count++;last=id;}
 static void playChemical(World w,BlockPos p,String id){play(w,p,id);}
}
