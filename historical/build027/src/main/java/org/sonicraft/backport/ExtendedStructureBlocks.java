package org.sonicraft.backport;

import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/**
 * Backport of 24/32/40-state source decorative blocks. Metadata stores only their
 * orientation; extra *persistent* variants are held in a syncable tile entity.
 * In particular, no state bits are silently truncated to four-bit 1.12 metadata.
 */
final class ExtendedStructureBlocks {
 private static final PropertyDirection HORIZONTAL=PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
 private static final PropertyDirection FACING=PropertyDirection.create("facing");
 private static final PropertyInteger TWO=PropertyInteger.create("blockstate",0,1);
 private static final PropertyInteger FOUR=PropertyInteger.create("blockstate",0,3);
 private static final PropertyInteger SIX=PropertyInteger.create("blockstate",0,5);
 private static final PropertyInteger EIGHT=PropertyInteger.create("blockstate",0,7);
 private static final PropertyInteger TEN=PropertyInteger.create("blockstate",0,9);
 private static final PropertyBool POWERED=PropertyBool.create("powered");
 enum Mount implements IStringSerializable { FLOOR, WALL, CEILING;
  @Override public String getName(){return name().toLowerCase(Locale.ROOT);}
 }
 private static final PropertyEnum<Mount> FACE=PropertyEnum.create("face",Mount.class);

 static Block create(Content.BlockDef def){switch(def.kind){
  case "extended_mount":return new Mounted(def,false);
  case "extended_mount_switch":return new Mounted(def,true);
  case "extended_button":return new Button(def);
  case "extended_horizontal6":return new Panel6(def);
  case "extended_horizontal8":return new Panel8(def);
  case "extended_horizontal10":return new Panel10(def);
  case "extended_six4":return new SixFacing(def);
  case "extended_wall":return new Wall(def);
  case "extended_rail":return new BulkBlocks.Pane(def);
  case "source_boss_marker":return new Marker(def);
  default:throw new IllegalArgumentException("Unrecognized extended source block: "+def.kind);
 }}

 public static final class StateTile extends TileEntity {
  public StateTile(){}
  int variant=0, mount=0; boolean powered=false;
  @Override public NBTTagCompound writeToNBT(NBTTagCompound n){super.writeToNBT(n);n.setInteger("Variant",variant);n.setInteger("Mount",mount);n.setBoolean("Powered",powered);return n;}
  @Override public void readFromNBT(NBTTagCompound n){super.readFromNBT(n);variant=n.getInteger("Variant");mount=n.getInteger("Mount");powered=n.getBoolean("Powered");}
  @Override public NBTTagCompound getUpdateTag(){return writeToNBT(new NBTTagCompound());}
  @Override public SPacketUpdateTileEntity getUpdatePacket(){return new SPacketUpdateTileEntity(pos,0,getUpdateTag());}
  @Override public void onDataPacket(NetworkManager net,SPacketUpdateTileEntity packet){readFromNBT(packet.getNbtCompound());if(world!=null)world.markBlockRangeForRenderUpdate(pos,pos);}
  void updated(){markDirty();if(world!=null)world.notifyBlockUpdate(pos,world.getBlockState(pos),world.getBlockState(pos),3);}
 }

 /** Must be called after World.setBlockState: the 1.20 palette state has already been rotated. */
 static void remember(World w,BlockPos pos,IBlockState source){
  if(!(source.getBlock() instanceof Base))return;
  TileEntity te=w.getTileEntity(pos);
  if(!(te instanceof StateTile))return;
  StateTile saved=(StateTile)te;
  if(source.getPropertyKeys().contains(FACE))saved.mount=((Mount)source.getValue(FACE)).ordinal();
  if(source.getPropertyKeys().contains(TWO))saved.variant=source.getValue(TWO);
  if(source.getPropertyKeys().contains(FOUR))saved.variant=source.getValue(FOUR);
  if(source.getPropertyKeys().contains(SIX))saved.variant=source.getValue(SIX);
  if(source.getPropertyKeys().contains(EIGHT))saved.variant=source.getValue(EIGHT);
  if(source.getPropertyKeys().contains(TEN))saved.variant=source.getValue(TEN);
  if(source.getPropertyKeys().contains(POWERED))saved.powered=source.getValue(POWERED);
  saved.updated();
 }

 private static abstract class Base extends Block {
  final Content.BlockDef definition;
  Base(Content.BlockDef d){super(BulkBlocks.material(d));definition=d;setSoundType(BulkBlocks.sound(d));BulkBlocks.configure(this,d);if(d.zeroLight)setLightOpacity(0);}
  @Override public boolean hasTileEntity(IBlockState s){return true;}
  @Override public TileEntity createTileEntity(World w,IBlockState s){return new StateTile();}
  @Override public BlockRenderLayer getBlockLayer(){return definition.zeroLight?BlockRenderLayer.CUTOUT:BlockRenderLayer.SOLID;}
  @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){
   return definition.id.endsWith("_sign")?NULL_AABB:super.getCollisionBoundingBox(s,w,p);
  }
  @Override public boolean isOpaqueCube(IBlockState state){return !definition.zeroLight;}
  @Override public boolean isFullCube(IBlockState state){return !definition.zeroLight;}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return definition.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return definition.fireSpread;}
  StateTile get(IBlockAccess w,BlockPos p){TileEntity te=w.getTileEntity(p);return te instanceof StateTile?(StateTile)te:null;}
 }
 private static final class Mounted extends Base {
  final boolean isSwitch;
  Mounted(Content.BlockDef d,boolean isSwitch){super(d);this.isSwitch=isSwitch;setDefaultState(blockState.getBaseState().withProperty(HORIZONTAL,EnumFacing.NORTH).withProperty(FACE,Mount.FLOOR).withProperty(TWO,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HORIZONTAL,FACE,TWO);}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(HORIZONTAL,EnumFacing.byHorizontalIndex(meta&3));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(HORIZONTAL).getHorizontalIndex();}
  @Override public IBlockState getActualState(IBlockState s,IBlockAccess w,BlockPos p){StateTile te=get(w,p);return te==null?s:s.withProperty(FACE,Mount.values()[Math.floorMod(te.mount,3)]).withProperty(TWO,Math.floorMod(te.variant,2));}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(HORIZONTAL,r.rotate(s.getValue(HORIZONTAL)));}
  @Override public boolean onBlockActivated(World w,BlockPos p,IBlockState s,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){
   if(!isSwitch)return false;if(!w.isRemote){StateTile t=get(w,p);if(t!=null){t.variant=1-t.variant;t.updated();w.notifyNeighborsOfStateChange(p,this,false);}}return true;
  }
  @Override public boolean canProvidePower(IBlockState s){return isSwitch;}
  @Override public int getWeakPower(IBlockState s,IBlockAccess w,BlockPos p,EnumFacing side){StateTile t=get(w,p);return isSwitch&&t!=null&&t.variant!=0?15:0;}
 }
 private static final class Button extends Base {
  Button(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(HORIZONTAL,EnumFacing.NORTH).withProperty(FACE,Mount.FLOOR).withProperty(POWERED,false));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HORIZONTAL,FACE,POWERED);}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(HORIZONTAL,EnumFacing.byHorizontalIndex(meta&3));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(HORIZONTAL).getHorizontalIndex();}
  @Override public IBlockState getActualState(IBlockState s,IBlockAccess w,BlockPos p){StateTile te=get(w,p);return te==null?s:s.withProperty(FACE,Mount.values()[Math.floorMod(te.mount,3)]).withProperty(POWERED,te.powered);}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(HORIZONTAL,r.rotate(s.getValue(HORIZONTAL)));}
  @Override public boolean onBlockActivated(World w,BlockPos p,IBlockState s,EntityPlayer player,EnumHand hand,EnumFacing side,float x,float y,float z){
   if(!w.isRemote){StateTile t=get(w,p);if(t!=null&&!t.powered){t.powered=true;t.updated();w.notifyNeighborsOfStateChange(p,this,false);w.scheduleUpdate(p,this,20);}}return true;
  }
  @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){
   IBlockState current=getActualState(s,w,p);Mount mount=current.getValue(FACE);EnumFacing dir=current.getValue(HORIZONTAL);
   if(mount==Mount.FLOOR)return new AxisAlignedBB(.25,0,.25,.75,.125,.75);
   if(mount==Mount.CEILING)return new AxisAlignedBB(.25,.875,.25,.75,1,.75);
   switch(dir){
    case NORTH:return new AxisAlignedBB(.25,.375,0,.75,.625,.125);
    case SOUTH:return new AxisAlignedBB(.25,.375,.875,.75,.625,1);
    case EAST:return new AxisAlignedBB(.875,.375,.25,1,.625,.75);
    default:return new AxisAlignedBB(0,.375,.25,.125,.625,.75);
   }
  }
  @Override public void updateTick(World w,BlockPos p,IBlockState s,java.util.Random random){StateTile t=get(w,p);if(!w.isRemote&&t!=null&&t.powered){t.powered=false;t.updated();w.notifyNeighborsOfStateChange(p,this,false);}}
  @Override public boolean canProvidePower(IBlockState s){return true;}
  @Override public int getWeakPower(IBlockState s,IBlockAccess w,BlockPos p,EnumFacing side){StateTile t=get(w,p);return t!=null&&t.powered?15:0;}
 }
 private static abstract class Panel extends Base {
  final int variants;
  Panel(Content.BlockDef d,int variants){super(d);this.variants=variants;setDefaultState(blockState.getBaseState().withProperty(HORIZONTAL,EnumFacing.NORTH).withProperty(variantProperty(),0));}
  abstract PropertyInteger variantProperty();
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(HORIZONTAL,EnumFacing.byHorizontalIndex(meta&3));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(HORIZONTAL).getHorizontalIndex();}
  @Override public IBlockState getActualState(IBlockState s,IBlockAccess w,BlockPos p){StateTile t=get(w,p);return t==null?s:s.withProperty(variantProperty(),Math.floorMod(t.variant,variants));}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(HORIZONTAL,r.rotate(s.getValue(HORIZONTAL)));}
 }
 private static final class Panel6 extends Panel {Panel6(Content.BlockDef d){super(d,6);} @Override PropertyInteger variantProperty(){return SIX;} @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HORIZONTAL,SIX);}}
 private static final class Panel8 extends Panel {Panel8(Content.BlockDef d){super(d,8);} @Override PropertyInteger variantProperty(){return EIGHT;} @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HORIZONTAL,EIGHT);}}
 private static final class Panel10 extends Panel {Panel10(Content.BlockDef d){super(d,10);} @Override PropertyInteger variantProperty(){return TEN;} @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HORIZONTAL,TEN);}}
 private static final class SixFacing extends Base {
  SixFacing(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH).withProperty(FOUR,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,FOUR);}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.byIndex(meta%6));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getIndex();}
  @Override public IBlockState getActualState(IBlockState s,IBlockAccess w,BlockPos p){StateTile t=get(w,p);return t==null?s:s.withProperty(FOUR,Math.floorMod(t.variant,4));}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(FACING,r.rotate(s.getValue(FACING)));}
 }
 static final class Marker extends Block {
  Marker(Content.BlockDef d){super(Material.CIRCUITS);setHardness(-1);setResistance(6000000);setLightOpacity(0);}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return NULL_AABB;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
 }
 /** The source wall uses none/low/tall arms computed from neighbors, not saved metadata. */
 enum Side implements IStringSerializable { NONE,LOW,TALL; @Override public String getName(){return name().toLowerCase(Locale.ROOT);} }
 static final class Wall extends Block {
  private static final PropertyBool UP=PropertyBool.create("up");
  private static final PropertyEnum<Side> NORTH=PropertyEnum.create("north",Side.class), EAST=PropertyEnum.create("east",Side.class), SOUTH=PropertyEnum.create("south",Side.class), WEST=PropertyEnum.create("west",Side.class);
  Wall(Content.BlockDef d){super(BulkBlocks.material(d));BulkBlocks.configure(this,d);setSoundType(BulkBlocks.sound(d));setDefaultState(blockState.getBaseState().withProperty(UP,true).withProperty(NORTH,Side.NONE).withProperty(EAST,Side.NONE).withProperty(SOUTH,Side.NONE).withProperty(WEST,Side.NONE));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,UP,NORTH,EAST,SOUTH,WEST);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState();}
  @Override public int getMetaFromState(IBlockState s){return 0;}
  private Side side(IBlockAccess w,BlockPos p,EnumFacing f){IBlockState n=w.getBlockState(p.offset(f));Block other=n.getBlock();if(other!=this&&!n.isFullCube())return Side.NONE;IBlockState a=w.getBlockState(p.offset(f).up());return a.getBlock()==this?Side.TALL:Side.LOW;}
  @Override public IBlockState getActualState(IBlockState s,IBlockAccess w,BlockPos p){Side n=side(w,p,EnumFacing.NORTH),e=side(w,p,EnumFacing.EAST),so=side(w,p,EnumFacing.SOUTH),we=side(w,p,EnumFacing.WEST);boolean upright=!(n!=Side.NONE&&so!=Side.NONE&&e==Side.NONE&&we==Side.NONE)&&!(e!=Side.NONE&&we!=Side.NONE&&n==Side.NONE&&so==Side.NONE);return s.withProperty(NORTH,n).withProperty(EAST,e).withProperty(SOUTH,so).withProperty(WEST,we).withProperty(UP,upright);}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){IBlockState a=getActualState(s,w,p);double n=a.getValue(NORTH)==Side.NONE?.25:0,e=a.getValue(EAST)==Side.NONE?.75:1,so=a.getValue(SOUTH)==Side.NONE?.75:1,we=a.getValue(WEST)==Side.NONE?.25:0;return new AxisAlignedBB(we,0,n,e,1.0,so);}
 }
}
