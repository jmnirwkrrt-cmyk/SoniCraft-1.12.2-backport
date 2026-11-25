package org.sonicraft.backport;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
public final class BulkBlocks {
 static Material material(Content.BlockDef d){return d.material.equals("wood")?Material.WOOD:d.material.equals("metal")?Material.IRON:Material.ROCK;}
 static SoundType sound(Content.BlockDef d){return d.material.equals("wood")?SoundType.WOOD:d.material.equals("metal")?SoundType.METAL:SoundType.STONE;}
 static void configure(Block b,Content.BlockDef d){b.setHardness(d.hardness).setResistance(d.resistance*5f/3f);if(d.tool)b.setHarvestLevel(d.material.equals("wood")?"axe":"pickaxe",0);}
 static Block create(Content.BlockDef d){switch(d.kind){case "bulk_stairs":return new Stairs(d);case "bulk_fence":return new Fence(d);case "bulk_pane":return new Pane(d);case "bulk_gate":return new Gate(d);case "bulk_door":return new Door(d);case "bulk_door_locked":return new LockedDoor(d);case "bulk_trapdoor":return new Trapdoor(d);case "bulk_pressure_plate":return new PressurePlate(d);case "source_cutout":return new Cutout(d);default:return new Cube(d);}}
 static class Cube extends Block {
  final Content.BlockDef d;Cube(Content.BlockDef d){super(material(d));this.d=d;setSoundType(sound(d));configure(this,d);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }
 /** Source decoration with cutout rendering: static collision adapts to 1.12 rather than recreating 1.20 geometry. */
 static class Cutout extends Cube {
  Cutout(Content.BlockDef d){super(d);setLightOpacity(0);}
  @Override public boolean isOpaqueCube(IBlockState state){return false;}
  @Override public boolean isFullCube(IBlockState state){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
 }
 static class Stairs extends BlockStairs {
  final Content.BlockDef d;Stairs(Content.BlockDef d){super(new Cube(d).getDefaultState());this.d=d;setSoundType(sound(d));configure(this,d);useNeighborBrightness=true;}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }
 static class Fence extends BlockFence {
  final Content.BlockDef d;Fence(Content.BlockDef d){super(material(d),material(d).getMaterialMapColor());this.d=d;setSoundType(sound(d));configure(this,d);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }

 /** Natural four-way connections and narrow collision of source iron bars/railings. */
 static class Pane extends BlockPane {
  final Content.BlockDef d;
  Pane(Content.BlockDef d){super(material(d),true);this.d=d;setSoundType(sound(d));configure(this,d);setLightOpacity(0);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }
 static class Door extends BlockDoor {
  final Content.BlockDef d;
  Door(Content.BlockDef d){super(material(d));this.d=d;setSoundType(sound(d));configure(this,d);setLightOpacity(0);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }
 /** Locked geometry: do not substitute a freely opening vanilla door for a quest/boss gate.
  * BlockDoor stores its 32 visual combinations across TWO physical blocks, so this
  * does not violate 1.12's 4-bit metadata limit. Manual activation is disabled. A redstone circuit can still open the door
  * as an interim puzzle-compatible substitute for source lock/quest logic. */
 static class LockedDoor extends Door {
  LockedDoor(Content.BlockDef d){super(d);}
  @Override public boolean onBlockActivated(net.minecraft.world.World w,BlockPos p,IBlockState s,net.minecraft.entity.player.EntityPlayer player,EnumHand hand,EnumFacing face,float x,float y,float z){return false;}
 }
 /** Source 16-variant trapdoor uses vanilla 1.12.2 facing, half and open states. */
 static class Trapdoor extends BlockTrapDoor {
  final Content.BlockDef d;
  Trapdoor(Content.BlockDef d){super(material(d));this.d=d;setSoundType(sound(d));configure(this,d);setLightOpacity(0);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }
 /** A working vanilla 1.12.2 pressure plate, including entity collision and redstone output. */
 static class PressurePlate extends BlockPressurePlate {
  final Content.BlockDef d;
  PressurePlate(Content.BlockDef d){super(material(d),BlockPressurePlate.Sensitivity.EVERYTHING);this.d=d;setSoundType(sound(d));configure(this,d);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }
 static class Gate extends BlockFenceGate {
  final Content.BlockDef d;Gate(Content.BlockDef d){super(BlockPlanks.EnumType.OAK);this.d=d;setSoundType(sound(d));configure(this,d);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }
}
