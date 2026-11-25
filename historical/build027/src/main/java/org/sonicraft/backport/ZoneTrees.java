package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
public final class ZoneTrees {
 static class Templates {Object[][][] green,beach;}
 static final Templates DATA=Content.read("trees.json",Templates.class);
 public static boolean generate(World w,Random r,BlockPos base,String style){
  Map<BlockPos,IBlockState> cells=new LinkedHashMap<>();
  if(style.equals("mushroom")){
   int h=4+r.nextInt(3);IBlockState cap=Content.BLOCKS.get("mushroom_hill_toadstool_block").getDefaultState(),stem=Content.BLOCKS.get("mushroom_hill_toadstool_stem").getDefaultState();
   for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)if(Math.abs(x)+Math.abs(z)<4)cells.put(base.add(x,h,z),cap);
   for(int y=0;y<h;y++)cells.put(base.up(y),stem);
  }else if(style.equals("pine")){
   Block log=Content.BLOCKS.get("hillpine_log"),leaves=Content.BLOCKS.get("hillpine_leaves");if(log==null||leaves==null)return false;
   int h=6+r.nextInt(3);for(int y=2;y<=h;y++){int radius=y==h?0:(h-y)%2==0?2:1;for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)if(Math.abs(x)+Math.abs(z)<=radius+1)cells.put(base.add(x,y,z),leaves.getDefaultState());}
   for(int y=0;y<h;y++)cells.put(base.up(y),log.getDefaultState());
  }else{
   Object[][][] variants=style.equals("beach")?DATA.beach:DATA.green;
   for(Object[] cell:variants[r.nextInt(variants.length)]){int x=((Number)cell[0]).intValue(),y=((Number)cell[1]).intValue(),z=((Number)cell[2]).intValue();cells.put(base.add(x,y,z),Content.BLOCKS.get((String)cell[3]).getDefaultState());}
  }
  // Preflight every cell before changing the world; never force-load neighboring chunks.
  for(BlockPos p:cells.keySet()){if(p.getY()<1||p.getY()>254||!w.isBlockLoaded(p))return false;IBlockState old=w.getBlockState(p);if(!old.getBlock().isAir(old,w,p)&&!old.getBlock().isLeaves(old,w,p)&&!old.getMaterial().isReplaceable())return false;}
  for(Map.Entry<BlockPos,IBlockState> cell:cells.entrySet())w.setBlockState(cell.getKey(),cell.getValue(),2);
  return true;
 }
}
