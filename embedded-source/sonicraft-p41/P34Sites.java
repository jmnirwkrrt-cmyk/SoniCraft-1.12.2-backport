package org.sonicraft.backport;
import java.util.Random;
import net.minecraft.world.World;import net.minecraft.util.math.BlockPos;import net.minecraft.util.Rotation;
public final class P34Sites {
 static String[] keys={"miragebar","chaogarden","mackpool","oceanpalace","hillmountain","aquaticlake","seasidelake","labyrinth","hydrocity","metalpit","deathegg","bigarms"};
 static String[][] zones={{"mirage_saloon_zone"},{"green_hill_zone","emerald_hill_zone","angel_island_jungle","mushroom_hill_zone","marble_garden_zone"},{"chemical_plant_zone"},{"seaside_hill_zone"},{"hill_top_zone"},{"aquatic_ruin_zone"},{"seaside_hill_zone"},{"green_hill_zone","marble_zone","spring_yard","star_light_zone"},{"angel_island_jungle"},{"chemical_plant_zone"},{"emerald_hill_zone","chemical_plant_zone","oil_ocean_zone","scrap_brain_zone"},{"angel_island_jungle"}};
 static int[][] rule={{10,9,1382979782},{60,48,921168784},{8,6,1476962428},{9,8,520965966},{7,6,1088296121},{6,5,397984357},{6,5,921771000},{32,20,1474162779},{12,6,673344742},{16,10,140091},{32,18,1102454470},{32,20,1453407575}};
 static boolean hit(long seed,int x,int z,int[] rule){int n=rule[0],k=n-rule[1];Random r=new Random(seed+341873128712L*Math.floorDiv(x,n)+132897987541L*Math.floorDiv(z,n)+rule[2]);int a=r.nextInt(k),b=r.nextInt(k);return Math.floorMod(x,n)==a&&Math.floorMod(z,n)==b;}
 public static boolean any(long seed,int x,int z){for(int[] c:rule)if(hit(seed,x,z,c))return true;return false;}
 static boolean extra(String s){for(String k:keys)if(k.equals(s))return true;return false;}
 public static String resolve(String previous,World w,BlockPos p){Object bio=w.func_180494_b(p);for(int i=0;i<keys.length;i++)for(String zone:zones[i])if(bio!=null&&bio==Content.BIOMES.get(zone)&&hit(w.func_72905_C(),p.func_177958_n()>>4,p.func_177952_p()>>4,rule[i]))return keys[i];return previous;}
 public static BlockPos crusher(BlockPos trigger,Rotation r,String name){if(!"scrap_brain_factory".equals(name))return trigger;
 switch(r){case CLOCKWISE_90:return trigger.func_177982_a(0,0,-13);case CLOCKWISE_180:return trigger.func_177982_a(13,0,0);case COUNTERCLOCKWISE_90:return trigger.func_177982_a(0,0,13);default:return trigger.func_177982_a(-13,0,0);}}
}
