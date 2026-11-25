package org.sonicraft.backport;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenMinable;
/** Original zones explicitly include vanilla ores. Adapted 1.12 distributions in chaostone. */
final class ZoneOres {
 static void generate(World w,Random r,BlockPos chunk){
  ore(w,r,chunk,Blocks.COAL_ORE,17,20,128);ore(w,r,chunk,Blocks.IRON_ORE,9,20,64);
  ore(w,r,chunk,Blocks.GOLD_ORE,9,2,32);ore(w,r,chunk,Blocks.REDSTONE_ORE,8,8,16);
  ore(w,r,chunk,Blocks.DIAMOND_ORE,8,1,16);ore(w,r,chunk,Blocks.LAPIS_ORE,7,1,32);
 }
 private static void ore(World w,Random r,BlockPos chunk,Block block,int size,int count,int height){WorldGenMinable gen=new WorldGenMinable(block.getDefaultState(),size,state->state!=null&&state.getBlock()==Content.BLOCKS.get("chaostone"));for(int i=0;i<count;i++)gen.generate(w,r,chunk.add(r.nextInt(16),r.nextInt(height),r.nextInt(16)));}
}
