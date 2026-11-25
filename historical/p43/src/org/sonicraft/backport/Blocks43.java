package org.sonicraft.backport;
import java.util.List;import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.entity.*;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.*;
public final class Blocks43 {
 public static Block create(Content.BlockDef d){if(d.kind.equals("character_monitor"))return new CharacterMonitor43(d);if(d.id.contains("leaves"))return new Leaves43();if(d.id.equals("death_egg_magnet_button")||d.id.equals("death_egg_magnet_button_on")||d.id.equals("damaged_death_egg_magnet_button"))return new Magnet43(d);if(d.id.equals("scrap_brain_elevator_top")||d.id.equals("scrap_brain_elevator_bottom"))return new Entrance(d);return P38Gel.create(d);}
 static class Entrance extends StructureFacadeBlocks.Horizontal {
  Entrance(Content.BlockDef d){super(d);setLightOpacity(0);}
  public boolean isOpaqueCube(IBlockState s){return false;}public boolean isFullCube(IBlockState s){return false;}
  public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return null;}
  public void addCollisionBoxToList(IBlockState s,World w,BlockPos p,AxisAlignedBB mask,List<AxisAlignedBB> out,Entity e,boolean actual){
   EnumFacing f=EnumFacing.getHorizontal(getMetaFromState(s)&3);double t=1.0/16;
   if(f!=EnumFacing.NORTH)addCollisionBoxToList(p,mask,out,new AxisAlignedBB(0,0,0,1,1,t));
   if(f!=EnumFacing.SOUTH)addCollisionBoxToList(p,mask,out,new AxisAlignedBB(0,0,1-t,1,1,1));
   if(f!=EnumFacing.WEST)addCollisionBoxToList(p,mask,out,new AxisAlignedBB(0,0,0,t,1,1));
   if(f!=EnumFacing.EAST)addCollisionBoxToList(p,mask,out,new AxisAlignedBB(1-t,0,0,1,1,1));
  }
  public boolean isLadder(IBlockState s,IBlockAccess w,BlockPos p,EntityLivingBase e){return true;}
  public BlockFaceShape getBlockFaceShape(IBlockAccess w,IBlockState s,BlockPos p,EnumFacing f){return BlockFaceShape.UNDEFINED;}
 }
}
