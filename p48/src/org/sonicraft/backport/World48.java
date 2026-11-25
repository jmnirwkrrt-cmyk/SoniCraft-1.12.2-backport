package org.sonicraft.backport;
import java.util.*;import net.minecraft.world.biome.Biome;import net.minecraftforge.common.BiomeManager;
public final class World48 {
 static final Set<String> ISLAND=new HashSet<>(Arrays.asList("angel_island_jungle","scorched_angel_island_jungle","marble_garden_zone","carnival_night_zone","ice_cap_zone","launch_base_zone","mushroom_hill_zone","sandopolis_zone","lava_reef_zone","sky_sanctuary_zone"));
 static boolean allowed(Biome b){return b.getRegistryName()!=null&&!ISLAND.contains(b.getRegistryName().getResourcePath());}
 public static void addBiome(BiomeManager.BiomeType type,BiomeManager.BiomeEntry entry){
  if(!allowed(entry.biome))return;
  double scale=SonicraftLegacy.config.getFloat("frequencyMultiplierP48","overworld",.33333334f,0,1,"Scale existing SoniCraft climate weights in the normal Overworld. Angel Island zones are excluded. New chunks only.");
  if(scale<=0)return;int weight=Math.max(1,(int)Math.round(entry.itemWeight*scale));BiomeManager.addBiome(type,new BiomeManager.BiomeEntry(entry.biome,weight));
 }
 public static void addSpawnBiome(Biome b){if(allowed(b)&&SonicraftLegacy.config.getFloat("frequencyMultiplierP48","overworld",.33333334f,0,1,"Scale existing weights.")>0)BiomeManager.addSpawnBiome(b);}
 public static String[] all(){List<String> a=new ArrayList<>();for(String id:RegionalBiomes.all())if(!ISLAND.contains(id))a.add(id);return a.toArray(new String[0]);}
}
