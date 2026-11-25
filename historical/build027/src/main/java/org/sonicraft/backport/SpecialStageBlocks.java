package org.sonicraft.backport;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** First-pass 1.12.2 physics for Sonic 3 special-stage spheres and checkpoints.
 * Original stage timers, ring patterns, victory transitions and saves are NOT implemented.
 * All impulses are deliberately bounded so placing a block in a survival world is safe.
 */
final class SpecialStageBlocks {
 private SpecialStageBlocks() {}
 private static final PropertyInteger FRAME2=PropertyInteger.create("blockstate",0,1);
 private static final PropertyInteger FRAME3=PropertyInteger.create("blockstate",0,2);
 static Block create(String kind) {
  if(kind.equals("stage_blue"))return new Blue();
  if(kind.equals("stage_red")||kind.equals("stage_yellow")||kind.equals("stage_bumper"))return new Sphere(kind);
  if(kind.equals("stage_up")||kind.equals("stage_down")||kind.equals("stage_reverse"))return new Motion(kind);
  if(kind.equals("stage_checkpoint")||kind.equals("stage_checkpoint_top"))return new Checkpoint(kind);
  if(kind.equals("stage_electric"))return new Electric();
  throw new IllegalArgumentException("Unrecognized special-stage block: "+kind);
 }
 private static boolean ready(World world,Entity e,String key,int cool) {
  if(world.isRemote||e.isSneaking()||!e.isEntityAlive())return false;
  int last=e.getEntityData().getInteger(key);
  if(last!=0 && e.ticksExisted-last<cool)return false;
  e.getEntityData().setInteger(key,e.ticksExisted==0?1:e.ticksExisted);
  return true;
 }
 private static abstract class PassThrough extends Block {
  PassThrough(){super(Material.ROCK);setHardness(1.5f);setResistance(3.0f);setSoundType(SoundType.STONE);setLightOpacity(0);}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return NULL_AABB;}
 }
 static final class Sphere extends PassThrough {
  final String kind;
  Sphere(String k){kind=k;}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){
   if(!ready(w,e,"sonicraftStageSphere",9))return;
   if(kind.equals("stage_red")){
    // Outside the original timed minigame, red spheres slow rather than killing the player.
    if(e instanceof EntityPlayer)((EntityPlayer)e).addPotionEffect(new PotionEffect(MobEffects.SLOWNESS,40,1));
    e.motionX*=0.25D;e.motionZ*=0.25D;
   }else if(kind.equals("stage_yellow")){
    e.motionY=Math.max(e.motionY,0.72D);e.fallDistance=0;
   }else{
    double dx=e.posX-p.getX()-0.5D, dz=e.posZ-p.getZ()-0.5D;
    double len=Math.sqrt(dx*dx+dz*dz);
    if(len<0.01D){dx=1;dz=0;len=1;}
    e.motionX=dx/len*0.65D;e.motionZ=dz/len*0.65D;
    e.motionY=Math.max(0.25D,e.motionY);e.fallDistance=0;
   }
   e.velocityChanged=true;
  }
 }
 static final class Blue extends PassThrough {
  Blue(){setDefaultState(blockState.getBaseState().withProperty(FRAME2,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FRAME2);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(FRAME2,m&1);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FRAME2);}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){
   if(!ready(w,e,"sonicraftBlueSphere",8)||!(e instanceof EntityPlayer))return;
   Block red=Content.BLOCKS.get("red_sphere");
   if(red!=null)w.setBlockState(p,red.getDefaultState(),3);
  }
 }
 static final class Motion extends PassThrough {
  final String kind;
  Motion(String k){kind=k;if(kind.equals("stage_reverse"))setDefaultState(blockState.getBaseState().withProperty(FRAME2,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FRAME2);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(FRAME2,m&1);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FRAME2);}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){
   if(!ready(w,e,"sonicraftStageMotion",12))return;
   if(kind.equals("stage_up"))e.motionY=Math.max(e.motionY,0.75D);
   else if(kind.equals("stage_down"))e.motionY=Math.min(e.motionY,-0.38D);
   else{e.motionX=-e.motionX;e.motionZ=-e.motionZ;}
   e.fallDistance=0;e.velocityChanged=true;
  }
 }
 static final class Checkpoint extends Block {
  final String kind;
  Checkpoint(String k){super(Material.ROCK);kind=k;setHardness(1.5f);setResistance(3f);setDefaultState(blockState.getBaseState().withProperty(FRAME3,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FRAME3);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(FRAME3,m%3);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FRAME3);}
  @Override public void onEntityWalk(World w,BlockPos p,Entity e){
   if(!ready(w,e,"sonicraftStageCheckpoint",35))return;
   if(kind.equals("stage_checkpoint_top")){e.motionY=Math.max(e.motionY,0.65D);e.fallDistance=0;}
   else if(e instanceof EntityPlayer)((EntityPlayer)e).setSpawnPoint(p.up(),true);
  }
 }
 static final class Electric extends Block {
  static final PropertyDirection FACING=PropertyDirection.create("facing",EnumFacing.Plane.HORIZONTAL);
  Electric(){super(Material.IRON);setHardness(3f);setResistance(6f);setSoundType(SoundType.METAL);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(FACING,EnumFacing.byHorizontalIndex(m&3));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getHorizontalIndex();}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(FACING,r.rotate(s.getValue(FACING)));}
  @Override public void onEntityWalk(World w,BlockPos p,Entity e){zap(w,e);}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){zap(w,e);}
  private void zap(World w,Entity e){if(ready(w,e,"sonicraftElectricBlock",18))e.attackEntityFrom(net.minecraft.util.DamageSource.MAGIC,2f);}
 }
}
