package org.sonicraft.backport;
import java.util.Random;import net.minecraft.block.Block;import net.minecraft.block.state.IBlockState;import net.minecraft.util.math.BlockPos;import net.minecraft.world.chunk.Chunk;import net.minecraft.world.biome.Biome;
/** Source oil_delta: 40 attempts, size 7..11 and hardened-oil rims. Chunk-local, before structures. */
public final class Oil43 {
 public static void generate(Chunk c){Block oil=Content.BLOCKS.get("oil"),floor=Content.BLOCKS.get("hardened_oil");if(oil==null||floor==null)return;
  int[][] heights=new int[16][16];byte[] biomes=c.getBiomeArray();boolean any=false;
  for(int x=0;x<16;x++)for(int z=0;z<16;z++){
   if(Biome.getBiome(biomes[z*16+x]&255)!=Content.BIOMES.get("oil_ocean_zone"))continue;
   for(int y=254;y>4;y--){BlockPos p=new BlockPos(c.x*16+x,y,c.z*16+z);if(c.getBlockState(p).getBlock()==floor){heights[x][z]=y;any=true;break;}}
  }if(!any)return;
  Random r=new Random(c.getWorld().getSeed()^c.x*341873128712L^c.z*132897987541L^0x43a11L);
  for(int i=0;i<40;i++){int cx=r.nextInt(16),cz=r.nextInt(16),cy=heights[cx][cz],rx=7+r.nextInt(5),rz=7+r.nextInt(5),rim=r.nextInt(3);if(cy==0)continue;
   for(int x=Math.max(0,cx-rx);x<=Math.min(15,cx+rx);x++)for(int z=Math.max(0,cz-rz);z<=Math.min(15,cz+rz);z++){
    int y=heights[x][z];if(y==0||Math.abs(y-cy)>1)continue;
    double d=(x-cx)*(x-cx)/(double)(rx*rx)+(z-cz)*(z-cz)/(double)(rz*rz);if(d>=1-rim*.075)continue;
    BlockPos p=new BlockPos(c.x*16+x,y,c.z*16+z);Block top=c.getBlockState(p).getBlock();if(top!=floor&&top!=oil)continue;
    c.setBlockState(p.down(),floor.getDefaultState());c.setBlockState(p,oil.getDefaultState());
   }
  }c.markDirty();
 }
}
