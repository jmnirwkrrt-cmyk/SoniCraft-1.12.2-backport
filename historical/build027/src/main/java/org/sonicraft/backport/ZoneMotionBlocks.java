package org.sonicraft.backport;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Usable 1.12.2 interpretations of the source zone movement blocks.
 * Source textures are retained; exact acceleration, particles and sounds remain TODO.
 */
final class ZoneMotionBlocks {
 private ZoneMotionBlocks() {}
 static final PropertyInteger FRAME=PropertyInteger.create("blockstate",0,1);
 static Block create(String kind){
  if(kind.equals("ice_trampoline"))return new Trampoline();
  if(kind.equals("zone_bumper"))return new Bumper();
  if(kind.equals("zone_catapult"))return new Catapult();
  if(kind.equals("dash_ring"))return new DashRing();
  if(kind.equals("quicksand"))return new Quicksand();
  throw new IllegalArgumentException("Unknown movement block "+kind);
 }
 static boolean canLaunch(World w,Entity entity){
  return !w.isRemote && !entity.isSneaking() && entity.isEntityAlive() && !(entity instanceof net.minecraft.entity.item.EntityItem);
 }
 /** Bounces players and animals; holding sneak suppresses launch. */
 static final class Trampoline extends Block {
  Trampoline(){super(Material.ROCK);setHardness(1.5F);setResistance(3.0F);setSoundType(SoundType.STONE);}
  @Override public void onEntityWalk(World w,BlockPos p,Entity e){
   if(canLaunch(w,e) && e.onGround){e.motionY=Math.max(e.motionY,0.85D);e.fallDistance=0;}
  }
  @Override public void onFallenUpon(World w,BlockPos p,Entity e,float distance){e.fallDistance=0;}
  @Override public void onLanded(World w,Entity e){
   if(e.motionY<0)e.motionY=-e.motionY*0.85D;
  }
 }
 /** Six-direction Dash Ring with a bounded impulse; source special effects are deferred. */
 static final class DashRing extends Block {
  private static final PropertyDirection FACING=PropertyDirection.create("facing");
  DashRing(){super(Material.ROCK);setHardness(1.2F);setResistance(3.0F);setSoundType(SoundType.STONE);
   setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH));setLightOpacity(0);}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(FACING,EnumFacing.byIndex(m%6));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getIndex();}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(FACING,r.rotate(s.getValue(FACING)));}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState state,Entity e){
   if(!canLaunch(w,e))return;
   int last=e.getEntityData().getInteger("sonicraftDashRingTick");
   if(last!=0 && e.ticksExisted-last<12)return;
   e.getEntityData().setInteger("sonicraftDashRingTick",e.ticksExisted);
   EnumFacing f=state.getValue(FACING);
   e.motionX=f.getXOffset()*1.25D;e.motionZ=f.getZOffset()*1.25D;
   e.motionY=f.getYOffset()!=0?f.getYOffset()*1.1D:Math.max(e.motionY,0.12D);
   e.fallDistance=0;
  }
 }
 /** Quicksand keeps the original surface geometry, slowing and sinking entities. */
 static final class Quicksand extends Block {
  Quicksand(){super(Material.SAND);setHardness(0.5F);setResistance(0.5F);setSoundType(SoundType.SAND);}
  @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){
   if(w.isRemote)return;
   e.motionX*=0.58D;e.motionZ*=0.58D;
   e.motionY=Math.max(-0.065D,Math.min(0.02D,e.motionY));e.fallDistance=0;
  }
 }
 private static abstract class Trigger extends Block {
  Trigger(){super(Material.ROCK);setHardness(1.5F);setResistance(3.0F);setSoundType(SoundType.STONE);
   setDefaultState(blockState.getBaseState().withProperty(FRAME,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FRAME);}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FRAME,meta&1);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FRAME);}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  boolean fire(World w,BlockPos p,Entity e){
   if(!canLaunch(w,e) || w.getBlockState(p).getBlock()!=this || w.getBlockState(p).getValue(FRAME)!=0)return false;
   w.setBlockState(p,getDefaultState().withProperty(FRAME,1),2);
   w.scheduleUpdate(p,this,12);
   e.fallDistance=0;
   return true;
  }
  @Override public void updateTick(World w,BlockPos p,IBlockState s,Random r){
   if(!w.isRemote && s.getValue(FRAME)!=0)w.setBlockState(p,getDefaultState(),2);
  }
 }
 /** Radial bumper; a cooldown prevents repeated impulses while an entity is touching it. */
 static final class Bumper extends Trigger {
  @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){
   if(!fire(w,p,e))return;
   double dx=e.posX-(p.getX()+0.5),dz=e.posZ-(p.getZ()+0.5);
   double mag=Math.sqrt(dx*dx+dz*dz);
   if(mag<0.01){dx=1;dz=0;mag=1;}
   e.motionX=dx/mag*0.7;e.motionZ=dz/mag*0.7;e.motionY=Math.max(e.motionY,0.38D);
  }
 }
 /** Launches forward based on approach direction, while retaining source two-frame models. */
 static final class Catapult extends Trigger {
  @Override public void onEntityWalk(World w,BlockPos p,Entity e){
   if(!e.onGround || !fire(w,p,e))return;
   double heading=Math.toRadians(e.rotationYaw);
   e.motionX-=Math.sin(heading)*0.35D;
   e.motionZ+=Math.cos(heading)*0.35D;
   e.motionY=Math.max(e.motionY,0.9D);
  }
 }
}
