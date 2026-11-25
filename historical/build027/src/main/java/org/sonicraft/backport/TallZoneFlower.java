package org.sonicraft.backport;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

/** Two source halves; metadata 0 remains the lower half for existing IDs. */
public final class TallZoneFlower extends LegacyBlocks.ZonePlant {
 public static final PropertyEnum<BlockDoublePlant.EnumBlockHalf> HALF=BlockDoublePlant.HALF;
 TallZoneFlower(){setDefaultState(blockState.getBaseState().withProperty(HALF,BlockDoublePlant.EnumBlockHalf.LOWER));}
 static boolean upper(IBlockState s){return s.getValue(HALF)==BlockDoublePlant.EnumBlockHalf.UPPER;}
 @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HALF);}
 @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(HALF,(meta&8)!=0?BlockDoublePlant.EnumBlockHalf.UPPER:BlockDoublePlant.EnumBlockHalf.LOWER);}
 @Override public int getMetaFromState(IBlockState s){return upper(s)?8:0;}
 @Override public EnumOffsetType getOffsetType(){return EnumOffsetType.XZ;}
 @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return super.getBoundingBox(s,w,p).offset(s.getOffset(w,p));}
 @Override public boolean canPlaceBlockAt(World w,BlockPos p){return p.getY()>0&&p.getY()<255&&w.isBlockLoaded(p.up())&&w.isAirBlock(p)&&w.isAirBlock(p.up())&&canSustainBush(w.getBlockState(p.down()));}
 @Override public boolean canBlockStay(World w,BlockPos p,IBlockState s){
  if(upper(s)){IBlockState below=w.getBlockState(p.down());return below.getBlock()==this&&!upper(below);}
  IBlockState above=w.getBlockState(p.up());return canSustainBush(w.getBlockState(p.down()))&&above.getBlock()==this&&upper(above);
 }
 boolean place(World w,BlockPos p){
  if(!canPlaceBlockAt(w,p))return false;
  if(!w.setBlockState(p,getDefaultState(),2))return false;
  if(!w.setBlockState(p.up(),getStateFromMeta(8),2)){w.setBlockToAir(p);return false;}
  return true;
 }
 @Override public void onBlockPlacedBy(World w,BlockPos p,IBlockState s,EntityLivingBase e,ItemStack stack){if(!w.isRemote)w.setBlockState(p.up(),getStateFromMeta(8),2);}
 @Override public Item getItemDropped(IBlockState s,Random r,int fortune){return upper(s)?net.minecraft.init.Items.AIR:Item.getItemFromBlock(this);}
 @Override public int damageDropped(IBlockState s){return 0;}
 @Override public void onBlockHarvested(World w,BlockPos p,IBlockState s,EntityPlayer player){
  if(player.capabilities.isCreativeMode&&upper(s)&&w.getBlockState(p.down()).getBlock()==this)w.setBlockToAir(p.down());
  super.onBlockHarvested(w,p,s,player);
 }
 @Override public void breakBlock(World w,BlockPos p,IBlockState s){
  BlockPos other=upper(s)?p.down():p.up();IBlockState pair=w.getBlockState(other);
  if(pair.getBlock()==this&&upper(pair)!=upper(s)){
   if(upper(s)&&!w.isRemote)dropBlockAsItem(w,other,pair,0);
   w.setBlockToAir(other);
  }
  super.breakBlock(w,p,s);
 }
 @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return 100;}
 @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return 60;}
}
