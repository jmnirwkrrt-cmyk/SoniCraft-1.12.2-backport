package org.sonicraft.backport;
import java.util.Random;import net.minecraft.world.World;import net.minecraft.util.math.BlockPos;import net.minecraft.util.Rotation;
public final class P34Sites {
 static boolean hit(long seed,int x,int z,int[] rule){int n=rule[0],k=n-rule[1];Random r=new Random(seed+341873128712L*Math.floorDiv(x,n)+132897987541L*Math.floorDiv(z,n)+rule[2]);return Math.floorMod(x,n)==r.nextInt(k)&&Math.floorMod(z,n)==r.nextInt(k);}
 public static boolean any(long s,int x,int z){return Sites43.any(s,x,z);}static boolean extra(String k){return Sites43.extra(k);}
 public static String resolve(String prev,World w,BlockPos p){return Sites43.resolve(prev,w,p);}
 public static BlockPos crusher(BlockPos trigger,Rotation r,String name){if(!"scrap_brain_factory".equals(name))return trigger;switch(r){case CLOCKWISE_90:return trigger.add(0,0,-13);case CLOCKWISE_180:return trigger.add(13,0,0);case COUNTERCLOCKWISE_90:return trigger.add(0,0,13);default:return trigger.add(-13,0,0);}}
}
