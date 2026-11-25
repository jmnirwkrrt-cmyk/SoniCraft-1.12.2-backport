package org.sonicraft.backport;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.properties.PropertyBool;
import java.util.Random;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Marble Cave block shapes and minimal 1.12.2 interactive adaptations.
 * Mechanisms not faithfully ported are flagged by StructurePort before dungeon generation. */
final class MarbleCaveBlocks {
 private MarbleCaveBlocks(){}
 static final class Stairs extends BlockStairs {
  Stairs(){super(net.minecraft.init.Blocks.STONE.getDefaultState());setHardness(1.5f);setResistance(10f);setSoundType(SoundType.STONE);useNeighborBrightness=true;}
 }
 static final class Chain extends BlockRotatedPillar {
  private static final AxisAlignedBB VERTICAL=new AxisAlignedBB(.3125,0,.3125,.6875,1,.6875);
  private static final AxisAlignedBB X_AXIS=new AxisAlignedBB(0,.3125,.3125,1,.6875,.6875);
  private static final AxisAlignedBB Z_AXIS=new AxisAlignedBB(.3125,.3125,0,.6875,.6875,1);
  Chain(){super(Material.IRON);setHardness(2f);setResistance(12f);setSoundType(SoundType.METAL);}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){switch(s.getValue(AXIS)){case X:return X_AXIS;case Z:return Z_AXIS;default:return VERTICAL;}}
 }
 static final class Spikes extends BlockDirectional {
  private static final AxisAlignedBB UP=new AxisAlignedBB(0,0,0,1,.5,1),DOWN=new AxisAlignedBB(0,.5,0,1,1,1);
  private static final AxisAlignedBB NORTH=new AxisAlignedBB(0,0,0,1,1,.5), SOUTH=new AxisAlignedBB(0,0,.5,1,1,1);
  private static final AxisAlignedBB EAST=new AxisAlignedBB(.5,0,0,1,1,1), WEST=new AxisAlignedBB(0,0,0,.5,1,1);
  Spikes(){super(Material.IRON);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.UP));setHardness(2f);setResistance(10f);setSoundType(SoundType.METAL);}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){switch(s.getValue(FACING)){case UP:return UP;case DOWN:return DOWN;case NORTH:return NORTH;case SOUTH:return SOUTH;case EAST:return EAST;default:return WEST;}}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byIndex(meta%6));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getIndex();}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(FACING,r.rotate(s.getValue(FACING)));}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity entity){if(!w.isRemote)entity.attackEntityFrom(net.minecraft.util.DamageSource.CACTUS,2.0F);}
 }
 static final class Panel extends MarbleDecorative.Facing {
  Panel(){super();}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){switch(s.getValue(FACING)){
   case NORTH:return new AxisAlignedBB(0,0,0,1,1,.1875);
   case SOUTH:return new AxisAlignedBB(0,0,.8125,1,1,1);
   case EAST:return new AxisAlignedBB(.8125,0,0,1,1,1);
   default:return new AxisAlignedBB(0,0,0,.1875,1,1);
  }}
 }
 static final class Torch extends BlockHorizontal {
  static final PropertyInteger STATUS=PropertyInteger.create("blockstate",0,1);
  Torch(){super(Material.CIRCUITS);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH).withProperty(STATUS,0));setHardness(0);setLightLevel(.86F);setSoundType(SoundType.WOOD);}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,STATUS);}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.getHorizontal(meta&3)).withProperty(STATUS,(meta>>2)&1);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getHorizontalIndex()|(s.getValue(STATUS)<<2);}
  @Override public IBlockState withRotation(IBlockState s,Rotation rot){return s.withProperty(FACING,rot.rotate(s.getValue(FACING)));}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return new AxisAlignedBB(.25,.2,.25,.75,.8,.75);}
 }
 static final class Spring extends BlockDirectional {
  Spring(){this(EnumFacing.UP);}
  Spring(EnumFacing facing){super(Material.IRON);setDefaultState(blockState.getBaseState().withProperty(FACING,facing));setHardness(1f);setSoundType(SoundType.METAL);}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byIndex(meta%6));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getIndex();}
  @Override public IBlockState withRotation(IBlockState s,Rotation rot){return s.withProperty(FACING,rot.rotate(s.getValue(FACING)));}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){
   if(w.isRemote||e.isSneaking())return;
   EnumFacing f=s.getValue(FACING);e.motionX=f.getXOffset()*1.1;e.motionY=f.getYOffset()>0?.85:f.getYOffset()<0?-.6:Math.max(.15,e.motionY);e.motionZ=f.getZOffset()*1.1;e.velocityChanged=true;e.fallDistance=0;
  }
 }
 static final class Monitor extends MarbleDecorative.Facing {
  private final String reward;
  Monitor(String reward){this.reward=reward;setHardness(.7f);setSoundType(SoundType.GLASS);}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.TRANSLUCENT;}
  @Override public boolean onBlockActivated(World world,BlockPos pos,IBlockState state,EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){
   if(!world.isRemote){
    if(reward.equals("rings")){
     ItemStack rings=new ItemStack(Content.BLOCKS.get("gold_ring"),10);
     player.inventory.addItemStackToInventory(rings);
     if(!rings.isEmpty())player.dropItem(rings,false);
    }
    else if(reward.equals("shield"))player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE,20*60,1));
    else if(reward.equals("invincibility"))player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE,20*20,4));
    world.setBlockToAir(pos);
   }
   return true;
  }
 }

 /** Pressure switch: the 1.20.1 "blockstate=2" value is its detector-visible pressed state. */
 static final class FloorSwitch extends Block {
  static final PropertyInteger STATUS=PropertyInteger.create("blockstate",0,3);
  FloorSwitch(){super(Material.ROCK);setDefaultState(blockState.getBaseState().withProperty(STATUS,0));setHardness(1.5f);setResistance(10f);setSoundType(SoundType.STONE);setTickRandomly(false);}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,STATUS);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(STATUS,m&3);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(STATUS);}
  @Override public void onEntityWalk(World w,BlockPos p,Entity e){super.onEntityWalk(w,p,e);press(w,p,e);}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){press(w,p,e);}
  private void press(World w,BlockPos p,Entity e){
   if(w.isRemote||!(e instanceof EntityLivingBase))return;
   IBlockState state=w.getBlockState(p);if(state.getBlock()!=this)return;
   if(state.getValue(STATUS)!=2)w.setBlockState(p,state.withProperty(STATUS,2),3);
   w.scheduleUpdate(p,this,15);
   Detector.refresh(w,p.down());
  }
  @Override public void updateTick(World w,BlockPos p,IBlockState s,Random r){
   if(!w.isRemote&&s.getValue(STATUS)==2){
    w.setBlockState(p,s.withProperty(STATUS,0),3);
    Detector.refresh(w,p.down());
   }
  }
 }
 /** Reads the marble switch above it; outputs vanilla 15-tick redstone power while pressed. */
 static final class Detector extends Block {
  static final PropertyInteger STATUS=PropertyInteger.create("blockstate",0,1);
  Detector(){super(Material.ROCK);setDefaultState(blockState.getBaseState().withProperty(STATUS,0));setHardness(1.5f);setResistance(10f);setSoundType(SoundType.STONE);}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,STATUS);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(STATUS,m&1);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(STATUS);}
  @Override public boolean canProvidePower(IBlockState state){return true;}
  @Override public int getWeakPower(IBlockState s,IBlockAccess w,BlockPos p,EnumFacing facing){return s.getValue(STATUS)==1?15:0;}
  @Override public int getStrongPower(IBlockState s,IBlockAccess w,BlockPos p,EnumFacing facing){return getWeakPower(s,w,p,facing);}
  @Override public void onBlockAdded(World w,BlockPos p,IBlockState s){refresh(w,p);}
  @Override public void neighborChanged(IBlockState s,World w,BlockPos p,Block neighbor,BlockPos from){refresh(w,p);}
  static void refresh(World w,BlockPos p){
   if(w.isRemote||w.getBlockState(p).getBlock()!=Content.BLOCKS.get("switch_detector"))return;
   IBlockState source=w.getBlockState(p.up());
   int power=source.getBlock() instanceof FloorSwitch&&source.getValue(FloorSwitch.STATUS)==2?1:0;
   IBlockState own=w.getBlockState(p);
   if(own.getValue(STATUS)!=power)w.setBlockState(p,own.withProperty(STATUS,power),3);
  }
 }

 /** Source _on variant: forward-facing hot jet, no destructive fire-block spread.
  * This is a 1.12 gameplay adaptation; the source particle/sound logic is not ported. */
 static final class Flamethrower extends MarbleDecorative.Facing {
  Flamethrower(){super();setHardness(1.5f);setResistance(10f);setLightLevel(.3f);}
  @Override public void onBlockAdded(World w,BlockPos p,IBlockState state){if(!w.isRemote)w.scheduleUpdate(p,this,10);}
  @Override public void updateTick(World w,BlockPos p,IBlockState state,Random random){
   if(w.isRemote||state.getBlock()!=this)return;
   EnumFacing face=state.getValue(FACING);
   // Natural stone walls block the jet, unlike a ray that passes through them.
   for(int distance=1;distance<=5;distance++){
    BlockPos at=p.offset(face,distance);
    if(!w.isBlockLoaded(at)||w.getBlockState(at).isFullCube())break;
    AxisAlignedBB zone=new AxisAlignedBB(at).grow(.20,.15,.20);
    for(EntityLivingBase victim:w.getEntitiesWithinAABB(EntityLivingBase.class,zone)){
     if(victim instanceof EntityPlayer&&((EntityPlayer)victim).capabilities.isCreativeMode)continue;
     victim.attackEntityFrom(DamageSource.IN_FIRE,2f);victim.setFire(2);
    }
   }
   w.scheduleUpdate(p,this,10);
  }
 }

 /** Short-range sensor with redstone output. Uses the original _on block ID;
  * powered state is stored as 1.12 metadata so the signal survives save/load. */
 static final class ProximitySensor extends Block {
  static final PropertyBool POWERED=PropertyBool.create("powered");
  ProximitySensor(){super(Material.ROCK);setDefaultState(blockState.getBaseState().withProperty(POWERED,false));setHardness(1.5f);setResistance(10f);setSoundType(SoundType.STONE);}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,POWERED);}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(POWERED,(meta&1)!=0);}
  @Override public int getMetaFromState(IBlockState state){return state.getValue(POWERED)?1:0;}
  @Override public boolean canProvidePower(IBlockState state){return true;}
  @Override public int getWeakPower(IBlockState state,IBlockAccess access,BlockPos pos,EnumFacing facing){return state.getValue(POWERED)?15:0;}
  @Override public int getStrongPower(IBlockState state,IBlockAccess access,BlockPos pos,EnumFacing facing){return getWeakPower(state,access,pos,facing);}
  @Override public void onBlockAdded(World w,BlockPos p,IBlockState state){if(!w.isRemote)w.scheduleUpdate(p,this,5);}
  @Override public void updateTick(World w,BlockPos p,IBlockState state,Random random){
   if(w.isRemote||state.getBlock()!=this)return;
   boolean occupied=false;
   AxisAlignedBB area=new AxisAlignedBB(p).grow(5,3,5);
   for(EntityPlayer player:w.getEntitiesWithinAABB(EntityPlayer.class,area)){
    if(!player.isSpectator()){occupied=true;break;}
   }
   if(state.getValue(POWERED)!=occupied)w.setBlockState(p,state.withProperty(POWERED,occupied),3);
   w.scheduleUpdate(p,this,10);
  }
 }

 /** Simplified hazard for the source's animated GeckoLib crushing cylinder.
  * Animated geometry remains unported: this prevents a harmless decorative trap. */
 static final class Crusher extends Block {
  Crusher(){super(Material.ROCK);setHardness(2f);setResistance(10f);setSoundType(SoundType.STONE);}
  @Override public boolean isOpaqueCube(IBlockState state){return false;}
  @Override public boolean isFullCube(IBlockState state){return false;}
  @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess w,BlockPos p){return new AxisAlignedBB(.125,0,.125,.875,1,.875);}
  @Override public void onBlockAdded(World w,BlockPos p,IBlockState state){if(!w.isRemote)w.scheduleUpdate(p,this,10);}
  @Override public void updateTick(World w,BlockPos p,IBlockState state,Random random){
   if(w.isRemote||state.getBlock()!=this)return;
   // Scan the rim as well: entities usually collide with the cylinder's outer face.
   AxisAlignedBB zone=new AxisAlignedBB(p).grow(.15,.05,.15);
   for(EntityLivingBase e:w.getEntitiesWithinAABB(EntityLivingBase.class,zone)){
    if(e instanceof EntityPlayer&&((EntityPlayer)e).capabilities.isCreativeMode)continue;
    e.attackEntityFrom(DamageSource.GENERIC,4f);
   }
   w.scheduleUpdate(p,this,10);
  }
 }

}
