package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
final class GreenHillLandmark {
 static class Template {int[] size,offset;Object[][] cells;}
 static final Template TEMPLATE=Content.read("green_hill_mountain.json",Template.class);
 static boolean ground(Block b){return b==Content.BLOCKS.get("ghz_grass")||b==Content.BLOCKS.get("ghz_dirt")||b==Content.BLOCKS.get("chaostone")||b==Content.BLOCKS.get("southite");}
 static boolean place(World w,BlockPos surface){
  if(w.getBlockState(surface.down()).getBlock()!=Content.BLOCKS.get("ghz_grass"))return false;
  BlockPos origin=surface.add(TEMPLATE.offset[0],TEMPLATE.offset[1],TEMPLATE.offset[2]);Map<BlockPos,IBlockState> cells=new LinkedHashMap<>();Set<Long> columns=new HashSet<>();
  for(Object[] c:TEMPLATE.cells){int x=((Number)c[0]).intValue(),y=((Number)c[1]).intValue(),z=((Number)c[2]).intValue();BlockPos p=origin.add(x,y,z);
   if(p.getY()<1||p.getY()>254||!w.isBlockLoaded(p))return false;
   if(w.getBiome(p)!=Content.BIOMES.get("green_hill_zone"))return false;
   if(!w.isAirBlock(p)&&!ground(w.getBlockState(p).getBlock()))return false;
   long column=((long)x<<32)|(z&0xffffffffL);
   if(columns.add(column)){BlockPos foot=origin.add(x,-1,z);if(!w.isBlockLoaded(foot)||!ground(w.getBlockState(foot).getBlock()))return false;}
   cells.put(p,Content.BLOCKS.get((String)c[3]).getDefaultState());
  }
  for(Map.Entry<BlockPos,IBlockState> cell:cells.entrySet())w.setBlockState(cell.getKey(),cell.getValue(),2);return true;
 }
 static void decorate(World w,Random r,BlockPos chunk){if(r.nextInt(5)!=0)return;BlockPos p=chunk.add(8+r.nextInt(16),0,8+r.nextInt(16));if(w.isBlockLoaded(p))place(w,w.getHeight(p));}
}
