package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class EmeraldProgressionCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static class World extends FoodContentCheck.TestWorld {
  int scheduled,delay;@Override public void scheduleUpdate(BlockPos p,Block b,int ticks){scheduled++;delay=ticks;}
  @Override public boolean setBlockState(BlockPos p,IBlockState s,int flags){Block old=getBlockState(p).getBlock();super.setBlockState(p,s,flags);if(old!=s.getBlock())s.getBlock().onBlockAdded(this,p,s);return true;}
 }
 static class Player extends FoodContentCheck.Player {Player(World w){super(w);}@Override public void sendStatusMessage(ITextComponent text,boolean actionBar){} }
 public static void main(String[] args)throws Exception{
  Bootstrap.register();for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);ForgeRegistries.BLOCKS.register(b);ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));}for(Item item:EmeraldProgression.ITEMS.values())ForgeRegistries.ITEMS.register(item);
  World w=new World();Player p=new Player(w);p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(EmeraldProgression.KEY,9));require(!EmeraldProgression.redeem(p,EnumHand.OFF_HAND),"Offhand duplicate ignored");
  for(int i=0;i<7;i++){require(EmeraldProgression.redeem(p,EnumHand.MAIN_HAND),"Redeem");require(EmeraldProgression.stored(p)==(1<<(i+1))-1,"Source color order");}require(p.getHeldItemMainhand().getCount()==2,"Key cost");EmeraldProgression.redeem(p,EnumHand.MAIN_HAND);int rings=0;for(ItemStack s:p.inventory.mainInventory)if(s.getItem()==Item.getItemFromBlock(Content.BLOCKS.get("gold_ring")))rings+=s.getCount();require(rings==50,"Full set gives 50 rings");
  java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();net.minecraft.nbt.CompressedStreamTools.writeCompressed(p.getEntityData(),bytes);NBTTagCompound tag=net.minecraft.nbt.CompressedStreamTools.readCompressed(new java.io.ByteArrayInputStream(bytes.toByteArray()));Player reload=new Player(w);reload.getEntityData().setTag(net.minecraft.entity.player.EntityPlayer.PERSISTED_NBT_TAG,tag.getCompoundTag(net.minecraft.entity.player.EntityPlayer.PERSISTED_NBT_TAG));require(EmeraldProgression.stored(reload)==127,"Save/load");Player clone=new Player(w);EmeraldProgression.clonePlayer(new net.minecraftforge.event.entity.player.PlayerEvent.Clone(clone,p,true));require(EmeraldProgression.stored(clone)==127,"Death clone");
  require(EmeraldProgression.withdraw(p,0)&&EmeraldProgression.stored(p)==126&&(EmeraldProgression.inventory(p)&1)!=0,"Withdrawal");require(!EmeraldProgression.withdraw(p,0)&&!EmeraldProgression.withdraw(p,7)&&!EmeraldProgression.withdraw(p,-1),"No duplicate or invalid index");EmeraldProgression.redeem(p,EnumHand.MAIN_HAND);require(EmeraldProgression.stored(p)==126,"Physical emerald counts toward full set");
  Player full=new Player(w);EmeraldProgression.store(full,1);for(int i=0;i<36;i++)full.inventory.setInventorySlotContents(i,new ItemStack(Items.APPLE,64));require(!EmeraldProgression.withdraw(full,0)&&EmeraldProgression.stored(full)==1,"Full inventory retains flag");
  EmeraldMenu.Collection menu=new EmeraldMenu.Collection(clone);require(!menu.enchantItem(p,1)&&menu.enchantItem(clone,1)&&!menu.enchantItem(clone,1),"Server owner/duplicate checks");
  BlockPos pos=new BlockPos(0,64,0);GoalSignpost normal=(GoalSignpost)Content.BLOCKS.get("signpost"),spin=(GoalSignpost)Content.BLOCKS.get("signpost_spinning"),ready=(GoalSignpost)Content.BLOCKS.get("signpost_player");w.setBlockState(pos,normal.getDefaultState().withProperty(GoalSignpost.FACING,EnumFacing.WEST),3);normal.onEntityCollidedWithBlock(w,pos,w.getBlockState(pos),p);require(w.getBlockState(pos).getBlock()==spin&&w.scheduled==1&&w.delay==60,"Spin timer");spin.onEntityCollidedWithBlock(w,pos,w.getBlockState(pos),p);require(w.scheduled==1,"No timer reset");spin.updateTick(w,pos,w.getBlockState(pos),new Random());require(w.getBlockState(pos).getBlock()==ready&&w.getBlockState(pos).getValue(GoalSignpost.FACING)==EnumFacing.WEST,"Ready/facing");Player user=new Player(w);user.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(EmeraldProgression.KEY));require(ready.onBlockActivated(w,pos,w.getBlockState(pos),user,EnumHand.MAIN_HAND,EnumFacing.UP,0,0,0)&&EmeraldProgression.stored(user)==1,"Signpost transaction");w.setBlockToAir(pos);spin.updateTick(w,pos,spin.getDefaultState(),new Random());require(w.isAirBlock(pos),"No resurrection after break");
  System.out.println("PASS: emerald ordering/key cost/50-ring reward; persistent NBT and death clone; safe withdrawal and menu ownership; signpost 60-tick lifecycle, facing and break guard");
 }
}
