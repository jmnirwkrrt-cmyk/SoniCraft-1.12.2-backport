package org.sonicraft.backport;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraftforge.common.*;
public final class LegacyBlocks {
 public static Block create(Content.BlockDef d){
  if(d.kind.startsWith("extended_")||d.kind.equals("source_boss_marker"))return ExtendedStructureBlocks.create(d);
  if(d.kind.startsWith("stage_"))return SpecialStageBlocks.create(d.kind);
  if(d.kind.equals("ice_trampoline")||d.kind.equals("zone_bumper")||d.kind.equals("zone_catapult")||d.kind.equals("dash_ring")||d.kind.equals("quicksand"))return ZoneMotionBlocks.create(d.kind);
  if(d.kind.equals("adapted_clapper_spring"))return new MarbleCaveBlocks.Spring(net.minecraft.util.EnumFacing.NORTH);
  if(d.kind.startsWith("shape_"))return ShapeBlocks.create(d);
  if(d.kind.startsWith("facade_")||d.kind.equals("conveyor")||d.kind.equals("conveyor_state"))return StructureFacadeBlocks.create(d);
  if(d.id.equals("marble_zone_stone_flamethrower_on"))return new MarbleCaveBlocks.Flamethrower();
  if(d.id.equals("player_proximity_sensor_on"))return new MarbleCaveBlocks.ProximitySensor();
  if(d.id.equals("marble_crusher_cylinder"))return new MarbleCaveBlocks.Crusher();
  if(d.id.equals("switch_marble"))return new MarbleCaveBlocks.FloorSwitch();
  if(d.id.equals("switch_detector"))return new MarbleCaveBlocks.Detector();
  if(d.kind.equals("source_facing"))return new MarbleCaveBlocks.Spikes();
  if(d.kind.equals("marble_stairs"))return new MarbleCaveBlocks.Stairs();
  if(d.kind.equals("marble_chain"))return new MarbleCaveBlocks.Chain();
  if(d.kind.equals("marble_spikes"))return new MarbleCaveBlocks.Spikes();
  if(d.kind.equals("marble_panel"))return new MarbleCaveBlocks.Panel();
  if(d.kind.equals("marble_torch"))return new MarbleCaveBlocks.Torch();
  if(d.kind.equals("marble_spring"))return new MarbleCaveBlocks.Spring();
  if(d.kind.startsWith("marble_monitor_"))return new MarbleCaveBlocks.Monitor(d.kind.substring("marble_monitor_".length()));
  if(d.kind.equals("marble_column"))return new MarbleColumn(d.id);
  if(d.kind.equals("axis"))return new MarbleDecorative.Axis();
  if(d.kind.equals("facing"))return new MarbleDecorative.Facing();
  if(d.kind.equals("solid_lava"))return new MarbleDecorative.SolidLava();
  if(d.kind.equals("signpost"))return new GoalSignpost(d.id);
  if(d.kind.startsWith("bulk_"))return BulkBlocks.create(d);
  if(d.kind.equals("metal")||d.kind.equals("zone_rock")||d.kind.equals("oil")||d.kind.equals("scaffold"))return IndustrialBlocks.create(d);
  if(d.kind.equals("tallflower"))return new TallZoneFlower();
  if(d.kind.equals("flower")||d.kind.equals("flowerbed"))return new ZoneFlower(d.kind.equals("flowerbed"));
  if(d.kind.equals("plant"))return new ZonePlant();
  if(d.kind.equals("ring"))return new FoodContent.Ring();
  if(d.kind.equals("mint"))return new FoodContent.Mint();
  if(d.kind.equals("sapling"))return new Sapling(d.id);
  if(d.kind.equals("leaves"))return new Leaves();
  if(d.kind.equals("log"))return new Timber();
  if(d.kind.equals("wood"))return new Solid(Material.WOOD,2f,SoundType.WOOD);
  if(d.kind.equals("rock"))return new Solid(Material.ROCK,1.5f,SoundType.STONE);
  return new Ground();
 }
 static class ZonePlant extends BlockBush {
  ZonePlant(){super(Material.PLANTS);setHardness(0);setSoundType(SoundType.PLANT);}
  @Override protected boolean canSustainBush(IBlockState state){return state.getMaterial()==Material.GROUND||state.getMaterial()==Material.GRASS;}
 }
 static class ZoneFlower extends ZonePlant {
  final boolean bed;
  ZoneFlower(boolean bed){this.bed=bed;}
  @Override public Block.EnumOffsetType getOffsetType(){return bed?Block.EnumOffsetType.NONE:Block.EnumOffsetType.XZ;}
  @Override public net.minecraft.util.math.AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return bed?new net.minecraft.util.math.AxisAlignedBB(0,0,0,1,.125,1):super.getBoundingBox(s,w,p).offset(s.getOffset(w,p));}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,net.minecraft.util.EnumFacing f){return 100;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,net.minecraft.util.EnumFacing f){return 60;}
 }
 static class Solid extends Block {Solid(Material m,float h,SoundType sound){super(m);setHardness(h);setResistance(h*3);setSoundType(sound);}}
 static class Ground extends Solid {Ground(){super(Material.GROUND,.6f,SoundType.GROUND);}@Override public boolean canSustainPlant(IBlockState state,IBlockAccess world,BlockPos pos,net.minecraft.util.EnumFacing face,IPlantable plant){return true;}}
 static class Timber extends Solid {Timber(){super(Material.WOOD,2f,SoundType.WOOD);}@Override public boolean isWood(IBlockAccess w,BlockPos p){return true;}@Override public boolean canSustainLeaves(IBlockState s,IBlockAccess w,BlockPos p){return true;}}
 static class Leaves extends Solid {Leaves(){super(Material.LEAVES,.2f,SoundType.PLANT);setLightOpacity(1);}@Override public boolean isOpaqueCube(IBlockState s){return false;}@Override public boolean isLeaves(IBlockState s,IBlockAccess w,BlockPos p){return true;}}
 static class Sapling extends BlockBush implements IGrowable {
  final String id;Sapling(String id){this.id=id;setTickRandomly(true);setHardness(0);setSoundType(SoundType.PLANT);}
  @Override protected boolean canSustainBush(IBlockState s){return s.getMaterial()==Material.GROUND||s.getMaterial()==Material.GRASS;}
  @Override public void updateTick(World w,BlockPos p,IBlockState s,Random r){super.updateTick(w,p,s,r);if(!w.isRemote&&w.getLightFromNeighbors(p.up())>=9&&r.nextInt(7)==0)grow(w,r,p,s);}
  @Override public boolean canGrow(World w,BlockPos p,IBlockState s,boolean client){return true;}
  @Override public boolean canUseBonemeal(World w,Random r,BlockPos p,IBlockState s){return r.nextFloat()<.45;}
  @Override public void grow(World w,Random r,BlockPos p,IBlockState s){ZoneTrees.generate(w,r,p,id.contains("pine")?"pine":"green");}
 }
}
