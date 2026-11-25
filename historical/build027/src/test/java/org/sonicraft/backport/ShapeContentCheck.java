package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class ShapeContentCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static class World extends FoodContentCheck.TestWorld {
  boolean obstructed;
  @Override public boolean checkNoEntityCollision(AxisAlignedBB box){return !obstructed;}
  @Override public boolean mayPlace(Block b,BlockPos p,boolean skip,EnumFacing side,net.minecraft.entity.Entity e){return !obstructed&&isAirBlock(p);}
 }
 public static void main(String[] args){
  Bootstrap.register();int slabs=0,walls=0;
  for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);ForgeRegistries.BLOCKS.register(b);ForgeRegistries.ITEMS.register(ShapeBlocks.item(b).setRegistryName(b.getRegistryName()));}
  for(Block b:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(b);
  World w=new World();FoodContentCheck.Player player=new FoodContentCheck.Player(w);BlockPos p=new BlockPos(0,64,0);
  for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=Content.BLOCKS.get(d.id);
   if(b instanceof ShapeBlocks.Slab){slabs++;ShapeBlocks.Slab s=(ShapeBlocks.Slab)b;ItemSlab item=(ItemSlab)Item.getItemFromBlock(s);
    require(s.full.isDouble()&&!s.isDouble(),"Pair "+d.id);require(s.getBoundingBox(s.getDefaultState(),w,p).maxY==.5&&s.getBoundingBox(s.getStateFromMeta(8),w,p).minY==.5&&s.full.getBoundingBox(s.full.getDefaultState(),w,p).maxY==1,"Shape "+d.id);
    require(s.quantityDropped(new Random())==1&&s.full.quantityDropped(new Random())==2&&s.full.getItemDropped(s.full.getDefaultState(),new Random(),0)==item,"Two single drops "+d.id);require(s.full.getItem(w,p,s.full.getDefaultState()).getItem()==item,"Pick double "+d.id);
    require(s.getStateForPlacement(w,p,EnumFacing.DOWN,0,.1f,0,0,player).getValue(BlockSlab.HALF)==BlockSlab.EnumBlockHalf.TOP,"Placement "+d.id);
    for(boolean top:new boolean[]{false,true}){w.blocks.clear();w.blocks.put(p,s.getDefaultState().withProperty(BlockSlab.HALF,top?BlockSlab.EnumBlockHalf.TOP:BlockSlab.EnumBlockHalf.BOTTOM));player.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(item,3));EnumActionResult result=item.onItemUse(player,w,p,EnumHand.MAIN_HAND,top?EnumFacing.DOWN:EnumFacing.UP,.5f,.5f,.5f);require(result==EnumActionResult.SUCCESS&&w.getBlockState(p).getBlock()==s.full&&player.getHeldItemMainhand().getCount()==2,"Merge "+d.id);}
    w.blocks.clear();w.blocks.put(p,s.getDefaultState());player.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(item,3));w.obstructed=true;item.onItemUse(player,w,p,EnumHand.MAIN_HAND,EnumFacing.UP,.5f,.5f,.5f);require(w.getBlockState(p).getBlock()==s&&player.getHeldItemMainhand().getCount()==3,"Collision guard "+d.id);w.obstructed=false;
    require(s.getMetaFromState(s.getStateFromMeta(8))==8&&s.full.getMetaFromState(s.full.getDefaultState())==0,"Metadata "+d.id);
   }else if(b instanceof ShapeBlocks.Wall){walls++;w.blocks.clear();w.blocks.put(p,b.getDefaultState());w.blocks.put(p.north(),b.getDefaultState());w.blocks.put(p.south(),Blocks.STONE.getDefaultState());IBlockState actual=b.getActualState(b.getDefaultState(),w,p);require(actual.getValue(BlockWall.NORTH)&&actual.getValue(BlockWall.SOUTH)&&!actual.getValue(BlockWall.EAST)&&!actual.getValue(BlockWall.WEST),"Wall connections "+d.id);require(b.getCollisionBoundingBox(actual,w,p).maxY==1.5,"Wall collision "+d.id);NonNullList<ItemStack> list=NonNullList.create();b.getSubBlocks(CreativeCatalog.BLOCKS,list);require(list.size()==1&&list.get(0).getMetadata()==0,"Single variant "+d.id);}
  }
  require(slabs==56&&walls==16,"Families");System.out.println("PASS: 56 slab pairs, placement/merging/consumption/collision guard/drops/metadata; 16 walls with connections and collision");
 }
}
