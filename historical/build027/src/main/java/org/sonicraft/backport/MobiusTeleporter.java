package org.sonicraft.backport;

import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

/** Reuses a nearby gold portal or builds one above a safe surface, at 1:1 coordinates. */
final class MobiusTeleporter extends Teleporter {
 private final WorldServer target;
 private final BlockPos requested;
 private BlockPos prepared;
 MobiusTeleporter(WorldServer world,BlockPos requested){super(world);target=world;this.requested=requested;}
 @Override public void placeInPortal(Entity entity,float yaw){
  if(prepared!=null)place(entity,prepared);else if(!placeInExistingPortal(entity,yaw)){BlockPos base=buildPortal();if(base!=null)place(entity,base);}
 }
 @Override public boolean placeInExistingPortal(Entity entity,float yaw){
  // The remembered return portal normally makes this an O(1) lookup.
  if(valid(requested)){place(entity,requested);return true;}
  for(int radius=0;radius<=16;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++){
   if(Math.max(Math.abs(dx),Math.abs(dz))!=radius)continue;
   for(int y=target.getActualHeight()-4;y>=2;y--){BlockPos pos=new BlockPos(requested.getX()+dx,y,requested.getZ()+dz);if(valid(pos)){place(entity,pos);return true;}}
  }
  return false;
 }
 boolean prepare(Entity entity){
  if(valid(requested)){prepared=requested;return true;}
  // Preparing never moves the player before Forge performs the dimension transfer.
  for(int r=0;r<=16;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){
   if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;
   for(int y=target.getActualHeight()-4;y>=2;y--){BlockPos p=new BlockPos(requested.getX()+dx,y,requested.getZ()+dz);if(valid(p)){prepared=p;return true;}}
  }
  prepared=buildPortal();return prepared!=null;
 }
 private boolean valid(BlockPos p){return target.getBlockState(p).getBlock()==MobiusPortal.BLOCK&&MobiusPortal.frame(target,p,target.getBlockState(p).getValue(MobiusPortal.AXIS))!=null&&exit(p)!=null;}
 private BlockPos exit(BlockPos p){return exit(target,p);}
 static BlockPos exit(World target,BlockPos p){
  EnumFacing.Axis axis=target.getBlockState(p).getValue(MobiusPortal.AXIS);
  EnumFacing normal=axis==EnumFacing.Axis.X?EnumFacing.SOUTH:EnumFacing.EAST;
  while(target.getBlockState(p.down()).getBlock()==MobiusPortal.BLOCK)p=p.down();
  for(EnumFacing side:new EnumFacing[]{normal,normal.getOpposite()}){
   BlockPos q=p.offset(side);
   if(target.getBlockState(q.down()).isFullCube()&&target.isAirBlock(q)&&target.isAirBlock(q.up()))return q;
  }
  return null;
 }
 private void place(Entity e,BlockPos portal){BlockPos p=exit(portal);if(p==null)throw new IllegalStateException("SoniCraft portal has no safe exit");e.setLocationAndAngles(p.getX()+.5,p.getY(),p.getZ()+.5,e.rotationYaw,0);e.motionX=e.motionY=e.motionZ=0;e.fallDistance=0;e.timeUntilPortal=80;}
 private BlockPos buildPortal(){return buildPortal(target,requested);}
 static BlockPos buildPortal(World target,BlockPos requested){
  int x=(int)Math.max(target.getWorldBorder().minX()+4,Math.min(target.getWorldBorder().maxX()-5,requested.getX()));
  int z=(int)Math.max(target.getWorldBorder().minZ()+4,Math.min(target.getWorldBorder().maxZ()-4,requested.getZ()));
  if(x-1<target.getWorldBorder().minX()||x+3>target.getWorldBorder().maxX()||z-2<target.getWorldBorder().minZ()||z+3>target.getWorldBorder().maxZ())return null;
  int y=64;
  for(int dx=-1;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)y=Math.max(y,target.getHeight(x+dx,z+dz)+1);
  y=Math.min(y,target.getActualHeight()-5);
  BlockPos base=new BlockPos(x,y,z);
  // Never cut a cavity through an existing build.
  for(int dx=-1;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)for(int dy=-1;dy<=3;dy++)if(!target.isAirBlock(base.add(dx,dy,dz)))return null;
  for(int dx=-1;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)target.setBlockState(base.add(dx,-1,dz),Blocks.STONE.getDefaultState(),2);
  for(int dx=-1;dx<=2;dx++)for(int dy=-1;dy<=3;dy++)if(dx==-1||dx==2||dy==-1||dy==3)target.setBlockState(base.add(dx,dy,0),Blocks.GOLD_BLOCK.getDefaultState(),2);
  new MobiusPortal.Frame(base,EnumFacing.EAST,2,3).fill(target);
  return base;
 }
 @Override public boolean makePortal(Entity e){return buildPortal()!=null;}
}
