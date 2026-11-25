package org.sonicraft.backport;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.layer.*;
import net.minecraft.world.biome.Biome;
public final class ZoneWorldType extends WorldType {
 public ZoneWorldType(){super("sonicraftzones");}
 @Override public net.minecraft.world.biome.BiomeProvider getBiomeProvider(net.minecraft.world.World world){return RegionalBiomes.enabled?new MobiusWorld.Provider(world.getSeed(),RegionalBiomes.all(),true,RegionalBiomes.biomeZoom,RegionalBiomes.regionZoom):super.getBiomeProvider(world);}
 @Override public GenLayer getBiomeLayer(long seed,GenLayer parent,net.minecraft.world.gen.ChunkGeneratorSettings settings){GenLayer zones=new Zones(200L);zones=GenLayerZoom.magnify(1000L,zones,2);return new GenLayerBiomeEdge(1000L,zones);}
 static final class Zones extends GenLayer {
  Zones(long seed){super(seed);}
  @Override public int[] getInts(int x,int z,int width,int depth){int[] out=IntCache.getIntCache(width*depth);Content.BiomeDef[] defs=Content.BIOME_DEFS;for(int dz=0;dz<depth;dz++)for(int dx=0;dx<width;dx++){initChunkSeed(x+dx,z+dz);out[dx+dz*width]=Biome.getIdForBiome(Content.BIOMES.get(defs[nextInt(defs.length)].id));}return out;}
 }
}
