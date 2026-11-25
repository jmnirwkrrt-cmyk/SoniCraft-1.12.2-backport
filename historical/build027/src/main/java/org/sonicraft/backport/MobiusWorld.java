package org.sonicraft.backport;

import java.util.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.layer.*;
import net.minecraftforge.common.DimensionManager;

/** A separate dimension; the Overworld inclusion setting does not affect Mobius. */
public final class MobiusWorld extends WorldProvider {
 static DimensionType TYPE;
 static int dimensionId;
 static final String[] LEGACY_ZONES={"green_hill_zone","emerald_hill_zone","hill_top_zone","marble_zone","aquatic_ruin_zone","seaside_hill_zone","mirage_saloon_zone","press_garden_zone"};
 static final String[] ZONES=Content.read("mobius_zones.json",String[].class);
 static boolean extendedBiomes=true;
 static void register(){
  extendedBiomes=SonicraftLegacy.config.getBoolean("extendedMobiusBiomes","dimensions",true,"Use all 16 Mobius zones. False preserves the eight-zone layout from 0.1.5 for new chunks. Changing this can create biome seams at old chunk borders.");
  dimensionId=SonicraftLegacy.config.getInt("mobiusId","dimensions",11230,2,32767,"Persistent dimension ID. Choose an unused ID before creating a world; do not change it for existing saves.");
  if(DimensionManager.isDimensionRegistered(dimensionId))throw new IllegalStateException("SoniCraft: Mobius dimension ID "+dimensionId+" is occupied. Set an unused dimensions.mobiusId in sonicraft.cfg before loading the world.");
  TYPE=DimensionType.register("SoniCraft Mobius","_sonicraft_mobius",dimensionId,MobiusWorld.class,false);
  DimensionManager.registerDimension(dimensionId,TYPE);
 }
 @Override public DimensionType getDimensionType(){return TYPE;}
 @Override protected void init(){hasSkyLight=true;biomeProvider=new Provider(world.getSeed());}
 @Override public IChunkGenerator createChunkGenerator(){return new ChunkGeneratorOverworld(world,world.getSeed(),false,"");}
 @Override public boolean canRespawnHere(){return false;}
 @Override public boolean isSurfaceWorld(){return true;}
 @Override public double getMovementFactor(){return 1;}
 @Override public String getSaveFolder(){return "DIM"+getDimension();}
 static class Provider extends BiomeProvider {
  private final GenLayer zones,index;
  private final BiomeCache cache=new BiomeCache(this);
  Provider(long seed){this(seed,extendedBiomes);}
  Provider(long seed,boolean extended){this(seed,extended?ZONES:LEGACY_ZONES,extended&&RegionalBiomes.enabled,RegionalBiomes.biomeZoom,RegionalBiomes.regionZoom);}
  Provider(long seed,String[] palette,boolean grouped,int zoom,int regions){
   zones=grouped?RegionalBiomes.layer(palette,zoom,regions):GenLayerZoom.magnify(1000,new Zones(200,palette),4);
   index=new GenLayerVoronoiZoom(10,zones);
   zones.initWorldGenSeed(seed);index.initWorldGenSeed(seed);
   getBiomesToSpawnIn().clear();for(String id:palette)getBiomesToSpawnIn().add(Content.BIOMES.get(id));
  }
  @Override public Biome getBiome(net.minecraft.util.math.BlockPos p){return getBiome(p,Content.BIOMES.get(ZONES[0]));}
  @Override public Biome getBiome(net.minecraft.util.math.BlockPos p,Biome fallback){return cache.getBiome(p.getX(),p.getZ(),fallback);}
  private Biome[] sample(Biome[] result,int x,int z,int width,int height,GenLayer layer){
   IntCache.resetIntCache();if(result==null||result.length<width*height)result=new Biome[width*height];
   int[] ids=layer.getInts(x,z,width,height);for(int i=0;i<width*height;i++)result[i]=Biome.getBiome(ids[i]);return result;
  }
  @Override public Biome[] getBiomesForGeneration(Biome[] b,int x,int z,int w,int h){return sample(b,x,z,w,h,zones);}
  @Override public Biome[] getBiomes(Biome[] b,int x,int z,int w,int h){return getBiomes(b,x,z,w,h,true);}
  @Override public Biome[] getBiomes(Biome[] b,int x,int z,int w,int h,boolean cached){return sample(b,x,z,w,h,index);}
  @Override public boolean areBiomesViable(int x,int z,int radius,List<Biome> allowed){
   int minX=(x-radius)>>2,minZ=(z-radius)>>2,maxX=(x+radius)>>2,maxZ=(z+radius)>>2;
   for(Biome b:sample(null,minX,minZ,maxX-minX+1,maxZ-minZ+1,zones))if(!allowed.contains(b))return false;return true;
  }
  @Override public net.minecraft.util.math.BlockPos findBiomePosition(int x,int z,int radius,List<Biome> allowed,Random random){
   int minX=(x-radius)>>2,minZ=(z-radius)>>2,w=((x+radius)>>2)-minX+1,h=((z+radius)>>2)-minZ+1;
   Biome[] found=sample(null,minX,minZ,w,h,zones);net.minecraft.util.math.BlockPos chosen=null;int count=0;
   for(int i=0;i<found.length;i++)if(allowed.contains(found[i])&&random.nextInt(++count)==0)chosen=new net.minecraft.util.math.BlockPos((minX+i%w)<<2,0,(minZ+i/w)<<2);return chosen;
  }
  @Override public void cleanupCache(){cache.cleanupCache();}
 }
 static class Zones extends GenLayer {
  final String[] palette;
  Zones(long seed,String[] palette){super(seed);this.palette=palette;}
  @Override public int[] getInts(int x,int z,int width,int depth){int[] out=IntCache.getIntCache(width*depth);for(int dz=0;dz<depth;dz++)for(int dx=0;dx<width;dx++){initChunkSeed(x+dx,z+dz);out[dx+dz*width]=Biome.getIdForBiome(Content.BIOMES.get(palette[nextInt(palette.length)]));}return out;}
 }
}
