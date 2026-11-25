package org.sonicraft.backport;
import java.util.*;import net.minecraft.world.World;import net.minecraft.util.math.BlockPos;
public final class Sites43 {
 static final Site[] DATA=Content.read("sites43.json",Site[].class);
 static class Site {String key;String[] biomes;int[] rule;Integer height;}
 public static boolean any(long seed,int x,int z){for(Site s:DATA)if(P34Sites.hit(seed,x,z,s.rule))return true;return false;}
 public static boolean extra(String key){for(Site s:DATA)if(s.key.equals(key))return true;return false;}
 public static String resolve(String previous,World w,BlockPos p){for(Site s:DATA)for(String b:s.biomes)if(w.getBiome(p)==Content.BIOMES.get(b)&&Content.BIOMES.containsKey(b)&&P34Sites.hit(w.getSeed(),p.getX()>>4,p.getZ()>>4,s.rule))return s.key;return previous;}
 public static int height(String key){for(Site s:DATA)if(s.key.equals(key)&&s.height!=null)return s.height;if(key.equals("bighouse"))return -10;if(key.equals("tailsworkshop"))return 0;if(key.startsWith("studiobuilding"))return -22;if(key.equals("ghmountain"))return 1;if(key.equals("scrap"))return -18;if(key.equals("mysticbridge"))return -19;return Util41.height(key);}
}
