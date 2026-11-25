package org.sonicraft.backport;
import java.util.*;
import net.minecraft.init.Bootstrap;
import net.minecraft.block.Block;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.layer.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public class PortSmokeCheck {
 public static void main(String[] args){Bootstrap.register();for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d);b.setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);for(net.minecraft.block.state.IBlockState s:b.getBlockState().getValidStates())if(b.getMetaFromState(s)!=b.getMetaFromState(b.getStateFromMeta(b.getMetaFromState(s))))throw new AssertionError(d.id);}
  for(Content.BiomeDef d:Content.BIOME_DEFS){ZoneBiome b=new ZoneBiome(d);b.setRegistryName("sonicraft",d.id);ForgeRegistries.BIOMES.register(b);Content.BIOMES.put(d.id,b);}
  ZoneWorldType type=new ZoneWorldType();GenLayer[] a=GenLayer.initializeAllBiomeGenerators(125678L,type,null);int[] values=Arrays.copyOf(a[1].getInts(-512,-512,1024,1024),1024*1024);Set<Integer> found=new HashSet<>();for(int id:values){if(Biome.getBiome(id)==null)throw new AssertionError("Unknown biome "+id);found.add(id);}IntCache.resetIntCache();GenLayer[] b=GenLayer.initializeAllBiomeGenerators(125678L,type,null);if(!Arrays.equals(values,Arrays.copyOf(b[1].getInts(-512,-512,1024,1024),values.length)))throw new AssertionError("Non deterministic");int zones=0;for(ZoneBiome zone:Content.BIOMES.values())if(found.contains(Biome.getIdForBiome(zone)))zones++;if(zones<3)throw new AssertionError("Low diversity");System.out.println("PASS: "+Content.BLOCK_DEFS.length+" blocks and metadata roundtrips; 20 biomes; deterministic 1024x1024 map; "+zones+" zones in sample");
 }
}
