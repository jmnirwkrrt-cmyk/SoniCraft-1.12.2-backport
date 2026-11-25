package org.sonicraft.backport;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** Metadata-safe orientation and texture-variant adapters for ported world structures.
 * Machine logic is NOT implemented here. Unsupported source states are rejected by the loader. */
final class StructureFacadeBlocks {
 private static final PropertyDirection HORIZONTAL=PropertyDirection.create("facing",EnumFacing.Plane.HORIZONTAL);
 private static final PropertyDirection FACING=PropertyDirection.create("facing");
 private static final PropertyEnum<EnumFacing.Axis> AXIS=PropertyEnum.create("axis",EnumFacing.Axis.class);
 private static final PropertyInteger VARIANT=PropertyInteger.create("blockstate",0,15);
 private static final PropertyInteger FOUR=PropertyInteger.create("blockstate",0,3);
 private static final PropertyInteger TWO=PropertyInteger.create("blockstate",0,1);
 static Block create(Content.BlockDef d){switch(d.kind){
  case "facade_horizontal":return new Horizontal(d);
  case "conveyor":return new Conveyor(d);
  case "conveyor_state":return new ConveyorState(d);
  case "facade_facing":return new AnyFacing(d);
  case "facade_axis":return new Axis(d);
  case "facade_axisvariant":return new AxisVariant(d);
  case "facade_facingvariant":return new FacingVariant(d);
  case "facade_variant":return new Variant(d);
  case "facade_attachment":return new Attachment(d);
  case "facade_hvariant":return new HorizontalVariant(d);
  default:throw new IllegalArgumentException("Not a facade block: "+d.kind);
 }}
 private static abstract class Base extends Block {
  final Content.BlockDef d;
  Base(Content.BlockDef def){super(BulkBlocks.material(def));d=def;setSoundType(BulkBlocks.sound(def));BulkBlocks.configure(this,def);if(d.zeroLight)setLightOpacity(0);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
  @Override public boolean isOpaqueCube(IBlockState s){return !d.zeroLight;}
  @Override public boolean isFullCube(IBlockState s){return !d.zeroLight;}
  @Override public BlockRenderLayer getBlockLayer(){return d.zeroLight?BlockRenderLayer.CUTOUT:BlockRenderLayer.SOLID;}
 }

 enum AttachFace implements IStringSerializable {FLOOR("floor"),WALL("wall"),CEILING("ceiling");
  final String id;AttachFace(String id){this.id=id;}public String getName(){return id;}}
 private static final PropertyEnum<AttachFace> ATTACH_FACE=PropertyEnum.create("face",AttachFace.class);
 static final class Attachment extends Base {
  Attachment(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(ATTACH_FACE,AttachFace.FLOOR).withProperty(HORIZONTAL,EnumFacing.NORTH));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,ATTACH_FACE,HORIZONTAL);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(ATTACH_FACE,AttachFace.values()[(m>>2)%3]).withProperty(HORIZONTAL,EnumFacing.byHorizontalIndex(m&3));}
  @Override public int getMetaFromState(IBlockState s){return (s.getValue(ATTACH_FACE).ordinal()<<2)|s.getValue(HORIZONTAL).getHorizontalIndex();}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(HORIZONTAL,r.rotate(s.getValue(HORIZONTAL)));}
  @Override public boolean isOpaqueCube(IBlockState state){return false;}
  @Override public boolean isFullCube(IBlockState state){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
 }
 static final class Horizontal extends Base {
  Horizontal(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(HORIZONTAL,EnumFacing.NORTH));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HORIZONTAL);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(HORIZONTAL,EnumFacing.byHorizontalIndex(m&3));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(HORIZONTAL).getHorizontalIndex();}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(HORIZONTAL,r.rotate(s.getValue(HORIZONTAL)));}
 }
 /** A small, bounded conveyor adapter (entities/items on the surface move in the facing direction).
  * It does not reproduce the source factory's piston/printing/quest machinery. */
 static final class Conveyor extends Base {
  Conveyor(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(HORIZONTAL,EnumFacing.NORTH));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HORIZONTAL);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(HORIZONTAL,EnumFacing.byHorizontalIndex(m&3));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(HORIZONTAL).getHorizontalIndex();}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(HORIZONTAL,r.rotate(s.getValue(HORIZONTAL)));}
  private void propel(World w,BlockPos p,Entity e){
   if(w.isRemote||e.isSneaking())return;
   EnumFacing f=w.getBlockState(p).getValue(HORIZONTAL);
   if(f.getXOffset()!=0 && e.motionX*f.getXOffset()<0.18D)e.addVelocity(f.getXOffset()*0.027D,0,0);
   if(f.getZOffset()!=0 && e.motionZ*f.getZOffset()<0.18D)e.addVelocity(0,0,f.getZOffset()*0.027D);
  }
  @Override public void onEntityWalk(World w,BlockPos p,Entity e){propel(w,p,e);}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){propel(w,p,e);}
 }
 /** Chemical Plant conveyor with two authentic visual source frames; movement is adapted. */
 static final class ConveyorState extends Base {
  ConveyorState(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(HORIZONTAL,EnumFacing.NORTH).withProperty(TWO,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HORIZONTAL,TWO);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(HORIZONTAL,EnumFacing.byHorizontalIndex(m&3)).withProperty(TWO,(m>>2)&1);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(HORIZONTAL).getHorizontalIndex()|(s.getValue(TWO)<<2);}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(HORIZONTAL,r.rotate(s.getValue(HORIZONTAL)));}
  private void propel(World w,BlockPos p,Entity e){
   if(w.isRemote||e.isSneaking())return;
   EnumFacing f=w.getBlockState(p).getValue(HORIZONTAL);
   if(f.getXOffset()!=0&&e.motionX*f.getXOffset()<0.16D)e.addVelocity(f.getXOffset()*0.027D,0,0);
   if(f.getZOffset()!=0&&e.motionZ*f.getZOffset()<0.16D)e.addVelocity(0,0,f.getZOffset()*0.027D);
  }
  @Override public void onEntityWalk(World w,BlockPos p,Entity e){propel(w,p,e);}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){propel(w,p,e);}
 }
 static final class AnyFacing extends Base {
  AnyFacing(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(FACING,EnumFacing.byIndex(m%6));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getIndex();}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(FACING,r.rotate(s.getValue(FACING)));}
 }
 static final class Axis extends Base {
  Axis(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(AXIS,EnumFacing.Axis.Y));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,AXIS);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(AXIS,EnumFacing.Axis.values()[m%3]);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(AXIS).ordinal();}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){
   if(r==Rotation.CLOCKWISE_90||r==Rotation.COUNTERCLOCKWISE_90){EnumFacing.Axis a=s.getValue(AXIS);return s.withProperty(AXIS,a==EnumFacing.Axis.X?EnumFacing.Axis.Z:a==EnumFacing.Axis.Z?EnumFacing.Axis.X:a);}return s;
  }
 }
 /** Axis + source decorative substate (3*4=12 values, fits 1.12 metadata). */
 static final class AxisVariant extends Base {
  AxisVariant(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(AXIS,EnumFacing.Axis.Y).withProperty(FOUR,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,AXIS,FOUR);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(AXIS,EnumFacing.Axis.values()[(m&15)%3]).withProperty(FOUR,Math.min(3,(m&15)/3));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(AXIS).ordinal()+3*s.getValue(FOUR);}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){if(r==Rotation.CLOCKWISE_90||r==Rotation.COUNTERCLOCKWISE_90){EnumFacing.Axis a=s.getValue(AXIS);return s.withProperty(AXIS,a==EnumFacing.Axis.X?EnumFacing.Axis.Z:a==EnumFacing.Axis.Z?EnumFacing.Axis.X:a);}return s;}
 }
 /** Six orientations and two visual states (6 x 2 = 12 saved metadata values). */
 static final class FacingVariant extends Base {
  FacingVariant(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH).withProperty(TWO,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING,TWO);}
  @Override public IBlockState getStateFromMeta(int m){int i=m&15;return getDefaultState().withProperty(FACING,EnumFacing.byIndex(i%6)).withProperty(TWO,Math.min(1,i/6));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getIndex()+6*s.getValue(TWO);}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(FACING,r.rotate(s.getValue(FACING)));}
 }
 static final class Variant extends Base {
  Variant(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(VARIANT,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,VARIANT);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(VARIANT,m&15);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(VARIANT);}
 }
 static final class HorizontalVariant extends Base {
  HorizontalVariant(Content.BlockDef d){super(d);setDefaultState(blockState.getBaseState().withProperty(HORIZONTAL,EnumFacing.NORTH).withProperty(FOUR,0));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HORIZONTAL,FOUR);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(HORIZONTAL,EnumFacing.byHorizontalIndex(m&3)).withProperty(FOUR,(m>>2)&3);}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(HORIZONTAL).getHorizontalIndex()|(s.getValue(FOUR)<<2);}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(HORIZONTAL,r.rotate(s.getValue(HORIZONTAL)));}
 }
}
