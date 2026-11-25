package org.sonicraft.backport;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
/** Convert substrate after vanilla cave carving, before biome decoration/ore placement.
 * Replacing stone inside genTerrainBlocks would prevent 1.12 cave generators from carving it. */
public final class ZoneTerrain {
 @SubscribeEvent public void beforePopulation(PopulateChunkEvent.Pre event){replace(event.getWorld().getChunkFromChunkCoords(event.getChunkX(),event.getChunkZ()));}
 static void replace(Chunk chunk){
  byte[] biomes=chunk.getBiomeArray();
  for(int x=0;x<16;x++)for(int z=0;z<16;z++)if(Biome.getBiome(biomes[z*16+x]&255) instanceof ZoneBiome){
   for(int y=1;y<256;y++){BlockPos p=new BlockPos((chunk.x<<4)+x,y,(chunk.z<<4)+z);if(chunk.getBlockState(p).getBlock()==Blocks.STONE)chunk.setBlockState(p,Content.BLOCKS.get("chaostone").getDefaultState());}
  }
 }
}
