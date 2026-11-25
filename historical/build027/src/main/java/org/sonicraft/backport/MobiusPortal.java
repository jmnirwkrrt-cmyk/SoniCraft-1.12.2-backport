package org.sonicraft.backport;

import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

/** Gold frame validation is independent of vanilla's obsidian-only portal Size. */
public final class MobiusPortal extends BlockPortal {
 public static final MobiusPortal BLOCK=new MobiusPortal();
 public static final Item IGNITER=new Igniter().setRegistryName("sonicraft","mobius").setUnlocalizedName("sonicraft.mobius").setCreativeTab(SonicraftLegacy.TAB);
 private MobiusPortal(){setRegistryName("sonicraft","mobius_portal");setUnlocalizedName("sonicraft.mobius_portal");setHardness(-1);setLightLevel(1);setTickRandomly(false);}
 static boolean empty(World w,BlockPos p){Block b=w.getBlockState(p).getBlock();return b==Blocks.AIR||b==Blocks.FIRE||b==BLOCK;}
 static final class Frame {
  final BlockPos base;final EnumFacing along;final int width,height;
  Frame(BlockPos b,EnumFacing a,int w,int h){base=b;along=a;width=w;height=h;}
  boolean complete(World w){for(int x=0;x<width;x++)for(int y=0;y<height;y++)if(w.getBlockState(base.offset(along,x).up(y)).getBlock()!=BLOCK)return false;return true;}
  void fill(World w){IBlockState state=BLOCK.getDefaultState().withProperty(AXIS,along.getAxis());for(int x=0;x<width;x++)for(int y=0;y<height;y++)w.setBlockState(base.offset(along,x).up(y),state,2);}
 }
 static Frame frame(World w,BlockPos pos,EnumFacing.Axis axis){
  if(axis==EnumFacing.Axis.Y||!empty(w,pos))return null;
  EnumFacing along=axis==EnumFacing.Axis.X?EnumFacing.EAST:EnumFacing.SOUTH;
  BlockPos base=pos;
  for(int i=0;i<21&&base.getY()>1&&empty(w,base.down());i++)base=base.down();
  for(int i=0;i<21&&empty(w,base.offset(along.getOpposite()));i++)base=base.offset(along.getOpposite());
  int width=0;while(width<22&&empty(w,base.offset(along,width)))width++;
  if(width<2||width>21)return null;
  for(int x=0;x<width;x++)if(w.getBlockState(base.offset(along,x).down()).getBlock()!=Blocks.GOLD_BLOCK)return null;
  for(int y=0;y<=21;y++){
   boolean roof=true;for(int x=0;x<width;x++)if(w.getBlockState(base.offset(along,x).up(y)).getBlock()!=Blocks.GOLD_BLOCK)roof=false;
   if(roof)return y>=3?new Frame(base,along,width,y):null;
   if(w.getBlockState(base.offset(along,-1).up(y)).getBlock()!=Blocks.GOLD_BLOCK||w.getBlockState(base.offset(along,width).up(y)).getBlock()!=Blocks.GOLD_BLOCK)return null;
   for(int x=0;x<width;x++)if(!empty(w,base.offset(along,x).up(y)))return null;
  }
  return null;
 }
 @Override public boolean trySpawnPortal(World w,BlockPos pos){for(EnumFacing.Axis axis:new EnumFacing.Axis[]{EnumFacing.Axis.X,EnumFacing.Axis.Z}){Frame f=frame(w,pos,axis);if(f!=null){f.fill(w);return true;}}return false;}
 @Override public void neighborChanged(IBlockState state,World w,BlockPos pos,Block block,BlockPos from){Frame f=frame(w,pos,state.getValue(AXIS));if(f==null||!f.complete(w))w.setBlockToAir(pos);}
 @Override public void updateTick(World w,BlockPos p,IBlockState s,Random r){} // No Nether pigmen.
 @Override public void onEntityCollidedWithBlock(World w,BlockPos pos,IBlockState state,Entity entity){
  if(w.isRemote||!(entity instanceof EntityPlayerMP)||entity.isRiding()||entity.isBeingRidden())return;
  if(entity.timeUntilPortal>0){entity.timeUntilPortal=80;return;}
  int target=entity.dimension==MobiusWorld.dimensionId?0:MobiusWorld.dimensionId;
  if(entity.dimension!=0&&entity.dimension!=MobiusWorld.dimensionId)return;
  EntityPlayerMP player=(EntityPlayerMP)entity;
  BlockPos destination=pos;
  if(target==MobiusWorld.dimensionId)player.getEntityData().setLong("SonicraftMobiusReturn",pos.toLong());
  else if(player.getEntityData().hasKey("SonicraftMobiusReturn"))destination=BlockPos.fromLong(player.getEntityData().getLong("SonicraftMobiusReturn"));
  WorldServer targetWorld=player.getServer().getWorld(target);
  if(targetWorld==null)return;
  MobiusTeleporter teleporter=new MobiusTeleporter(targetWorld,destination);
  if(!teleporter.prepare(player)){player.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation("sonicraft.portal.no_space"),true);player.timeUntilPortal=80;return;}
  player.timeUntilPortal=80;
  player.changeDimension(target,teleporter);
 }
 static boolean placeRing(EntityPlayer p,World w,BlockPos pos,EnumHand hand){
  if(!w.isAirBlock(pos)||!p.canPlayerEdit(pos,EnumFacing.UP,p.getHeldItem(hand)))return false;
  Item ring=Item.getItemFromBlock(Content.BLOCKS.get("gold_ring"));int slot=-1;
  for(int i=0;i<p.inventory.getSizeInventory();i++){ItemStack stack=p.inventory.getStackInSlot(i);if(!stack.isEmpty()&&stack.getItem()==ring){slot=i;break;}}
  if(!p.capabilities.isCreativeMode&&slot<0)return false;
  if(!w.isRemote){if(!w.setBlockState(pos,Content.BLOCKS.get("gold_ring").getDefaultState(),3))return false;
   if(!p.capabilities.isCreativeMode)p.inventory.getStackInSlot(slot).shrink(1);
   p.getHeldItem(hand).damageItem(1,p);w.playSound(null,pos,FoodContent.SOUNDS.get("ring.place"),SoundCategory.BLOCKS,1,1);
  }return true;
 }
 static final class Igniter extends Item {
  Igniter(){setMaxStackSize(1);setMaxDamage(64);}
  @Override public EnumActionResult onItemUse(EntityPlayer p,World w,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z){
   BlockPos inside=pos.offset(face);ItemStack stack=p.getHeldItem(hand);
   if(!p.canPlayerEdit(inside,face,stack))return EnumActionResult.FAIL;
   Frame frame=frame(w,inside,EnumFacing.Axis.X);if(frame==null)frame=frame(w,inside,EnumFacing.Axis.Z);if(frame==null)return placeRing(p,w,inside,hand)?EnumActionResult.SUCCESS:EnumActionResult.FAIL;
   if(w.provider.getDimension()!=0&&w.provider.getDimension()!=MobiusWorld.dimensionId)return EnumActionResult.FAIL;
   if(!w.isRemote){frame.fill(w);w.playSound(null,inside,SoundEvents.ITEM_FLINTANDSTEEL_USE,SoundCategory.BLOCKS,1,1);stack.damageItem(1,p);}
   return EnumActionResult.SUCCESS;
  }
 }
}
