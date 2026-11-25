package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
/** Exact block coordinates and vertical states from twelve original Marble pillar NBTs. */
public final class ZoneStructures {
 static class Template {String id;Object[][] cells;}
 static final Template[] PILLARS=Content.read("pillars.json",Template[].class);
 static boolean place(World world,BlockPos surface,Template template){
  if(world.getBlockState(surface.down()).getBlock()!=Content.BLOCKS.get("marble_zone_grass"))return false;
  Map<BlockPos,IBlockState> cells=new LinkedHashMap<>();
  for(Object[] c:template.cells){BlockPos p=surface.add(((Number)c[0]).intValue(),((Number)c[1]).intValue()-1,((Number)c[2]).intValue());
   if(p.getY()<1||p.getY()>254||!world.isBlockLoaded(p))return false;
   if(p.getY()>=surface.getY()&&!world.isAirBlock(p))return false;
   cells.put(p,Content.BLOCKS.get((String)c[3]).getDefaultState());
  }
  for(Map.Entry<BlockPos,IBlockState> cell:cells.entrySet())world.setBlockState(cell.getKey(),cell.getValue(),2);for(BlockPos p:cells.keySet())if(world.getBlockState(p).getBlock() instanceof MarbleColumn)MarbleColumn.refresh(world,p);return true;
 }
 static void decorate(World w,Random r,BlockPos chunk){for(int i=0;i<2;i++){BlockPos p=chunk.add(8+r.nextInt(16),0,8+r.nextInt(16));int variant=r.nextInt(12);if(variant<PILLARS.length&&w.isBlockLoaded(p))place(w,w.getHeight(p),PILLARS[variant]);}}
}
