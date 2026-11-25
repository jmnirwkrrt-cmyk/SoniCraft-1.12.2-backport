package org.sonicraft.backport;
import java.util.List;import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.entity.*;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.*;
public final class Blocks47 {
 public static Block create(Content.BlockDef d){if(d.id.contains("elevator")&&d.kind.equals("facade_horizontal"))return new Entrance(d);if(d.id.contains("elevator")&&d.kind.equals("facade_axis"))return new Tube(d);return Blocks46.create(d);}
 static class Entrance extends Blocks43.Entrance {Entrance(Content.BlockDef d){super(d);}public IBlockState getStateForPlacement(World w,BlockPos p,EnumFacing f,float x,float y,float z,int meta,EntityLivingBase e){return getStateFromMeta(e.getHorizontalFacing().getOpposite().getHorizontalIndex());}}
 static class Tube extends StructureFacadeBlocks.Axis {
  Tube(Content.BlockDef d){super(d);setLightOpacity(0);}
  public boolean isOpaqueCube(IBlockState s){return false;}public boolean isFullCube(IBlockState s){return false;}
  public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return NULL_AABB;}
  public BlockFaceShape getBlockFaceShape(IBlockAccess w,IBlockState s,BlockPos p,EnumFacing f){return BlockFaceShape.UNDEFINED;}
  public boolean isLadder(IBlockState s,IBlockAccess w,BlockPos p,EntityLivingBase e){return true;}
  public void addCollisionBoxToList(IBlockState s,World w,BlockPos p,AxisAlignedBB box,List<AxisAlignedBB> out,Entity e,boolean actual){EnumFacing.Axis axis=EnumFacing.Axis.values()[getMetaFromState(s)%3];double t=1.0/16;
   if(axis!=EnumFacing.Axis.X){addCollisionBoxToList(p,box,out,new AxisAlignedBB(0,0,0,t,1,1));addCollisionBoxToList(p,box,out,new AxisAlignedBB(1-t,0,0,1,1,1));}
   if(axis!=EnumFacing.Axis.Y){addCollisionBoxToList(p,box,out,new AxisAlignedBB(0,0,0,1,t,1));addCollisionBoxToList(p,box,out,new AxisAlignedBB(0,1-t,0,1,1,1));}
   if(axis!=EnumFacing.Axis.Z){addCollisionBoxToList(p,box,out,new AxisAlignedBB(0,0,0,1,1,t));addCollisionBoxToList(p,box,out,new AxisAlignedBB(0,0,1-t,1,1,1));}
  }
 }
}
