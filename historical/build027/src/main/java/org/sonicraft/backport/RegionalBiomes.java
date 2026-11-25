package org.sonicraft.backport;
import java.util.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.*;
/** Select a large thematic region first, then a biome belonging to that region. */
final class RegionalBiomes {
 static boolean enabled=true;
 static int biomeZoom=7,regionZoom=2;
 static final String[][] GROUPS={
  {"scrap_brain_zone","chemical_plant_zone","oil_ocean_zone"},
  {"green_hill_zone","emerald_hill_zone","hill_top_zone","marble_zone","aquatic_ruin_zone","seaside_hill_zone","angel_island_jungle","mushroom_hill_zone"},
  {"spring_yard","star_light_zone","casino_night_zone","studiopolis_zone","night_carnival_zone"},
  {"mirage_saloon_zone","sandopolis_zone"},
  {"press_garden_zone","ice_cap_zone"}
 };
 static int group(String id){for(int g=0;g<GROUPS.length;g++)for(String value:GROUPS[g])if(value.equals(id))return g;throw new IllegalArgumentException("Ungrouped SoniCraft biome: "+id);}
 static void configure(){enabled=SonicraftLegacy.config.getBoolean("groupedBiomes","worldgen",true,"Large thematic regions in Mobius and SoniCraft Biomes worlds. New chunks only; false restores the 0.1.7 layout.");biomeZoom=SonicraftLegacy.config.getInt("biomeZoom","worldgen",7,4,9,"Biome size zoom for grouped worlds. 7 is eight times the former Mobius linear scale. Each extra step doubles size.");regionZoom=SonicraftLegacy.config.getInt("regionZoom","worldgen",2,1,4,"Additional zoom for thematic regions above biome size. 2 groups several nearby biomes together.");}
 static String[] all(){String[] p=new String[Content.BIOME_DEFS.length];for(int i=0;i<p.length;i++)p[i]=Content.BIOME_DEFS[i].id;return p;}
 static GenLayer layer(String[] palette,int zoom,int regions){
  List<int[]> groups=new ArrayList<>();for(int g=0;g<GROUPS.length;g++){List<Integer> ids=new ArrayList<>();for(String id:palette)if(group(id)==g){Biome b=Content.BIOMES.get(id);if(b==null)throw new IllegalStateException(id);ids.add(Biome.getIdForBiome(b));}if(!ids.isEmpty()){int[] a=new int[ids.size()];for(int i=0;i<a.length;i++)a[i]=ids.get(i);groups.add(a);}}
  if(groups.isEmpty())throw new IllegalArgumentException("Empty biome palette");
  GenLayer parent=GenLayerZoom.magnify(3100,new Groups(301,groups),regions);
  return new GenLayerSmooth(3700,GenLayerZoom.magnify(3300,new Members(302,parent,groups),zoom));
 }
 static final class Groups extends GenLayer {
  final List<int[]> groups;final int total;
  Groups(long seed,List<int[]> groups){super(seed);this.groups=groups;int n=0;for(int[] g:groups)n+=g.length;total=n;}
  public int[] getInts(int x,int z,int w,int h){int[] out=IntCache.getIntCache(w*h);for(int j=0;j<h;j++)for(int i=0;i<w;i++){initChunkSeed(x+i,z+j);int n=nextInt(total),g=0;while(n>=groups.get(g).length)n-=groups.get(g++).length;out[i+j*w]=g;}return out;}
 }
 static final class Members extends GenLayer {
  final List<int[]> groups;
  Members(long seed,GenLayer parent,List<int[]> groups){super(seed);this.parent=parent;this.groups=groups;}
  public int[] getInts(int x,int z,int w,int h){int[] regions=parent.getInts(x,z,w,h),out=IntCache.getIntCache(w*h);for(int j=0;j<h;j++)for(int i=0;i<w;i++){initChunkSeed(x+i,z+j);int[] choices=groups.get(regions[i+j*w]);out[i+j*w]=choices[nextInt(choices.length)];}return out;}
 }
}
