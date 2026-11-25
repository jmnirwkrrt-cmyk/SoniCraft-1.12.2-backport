package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class ColumnContentCheck {
 static void require(boolean v,String m){if(!v)throw new AssertionError(m);}
 static class World extends FoodContentCheck.TestWorld {
  int updates;boolean loaded=true;
  @Override protected boolean isChunkLoaded(int x,int z,boolean empty){return loaded;}
  @Override public boolean setBlockState(BlockPos p,IBlockState s,int flags){IBlockState old=getBlockState(p);if(old==s)return false;if(++updates>2000)throw new AssertionError("Unbounded neighbor recursion");blocks.put(p.toImmutable(),s);if(old.getBlock()!=s.getBlock())s.getBlock().onBlockAdded(this,p,s);if((flags&1)!=0)notifyNeighborsOfStateChange(p,s.getBlock(),false);return true;}
  @Override public void notifyNeighborsOfStateChange(BlockPos p,Block block,boolean observers){for(EnumFacing side:EnumFacing.values()){BlockPos neighbor=p.offset(side);IBlockState state=getBlockState(neighbor);state.getBlock().neighborChanged(state,this,neighbor,block,p);}}
 }
 static String id(World w,BlockPos p){return w.getBlockState(p).getBlock().getRegistryName().getResourcePath();}
 public static void main(String[] args){Bootstrap.register();for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);ForgeRegistries.BLOCKS.register(b);ForgeRegistries.ITEMS.register(ShapeBlocks.item(b).setRegistryName(b.getRegistryName()));}
  BlockPos p=new BlockPos(0,64,0);MarbleColumn basic=(MarbleColumn)Content.BLOCKS.get("marble_zone_pillar"),broken=(MarbleColumn)Content.BLOCKS.get("marble_zone_pillar_broken");
  for(EnumFacing.Axis axis:EnumFacing.Axis.values()){World w=new World();EnumFacing direction=axis==EnumFacing.Axis.X?EnumFacing.EAST:axis==EnumFacing.Axis.Z?EnumFacing.NORTH:EnumFacing.UP;IBlockState s=basic.getDefaultState().withProperty(MarbleColumn.AXIS,axis);w.setBlockState(p,s,3);require(id(w,p).equals("marble_zone_pillar"),"Isolated "+axis);w.setBlockState(p.offset(direction),s,3);w.setBlockState(p.offset(direction,2),s,3);require(id(w,p).endsWith("_bottom")&&id(w,p.offset(direction)).endsWith("_middle")&&id(w,p.offset(direction,2)).endsWith("_top"),"Connected triple "+axis);w.setBlockState(p.offset(direction),Blocks.AIR.getDefaultState(),3);require(id(w,p).equals("marble_zone_pillar")&&id(w,p.offset(direction,2)).equals("marble_zone_pillar"),"Split column "+axis);require(basic.getStateFromMeta(basic.getMetaFromState(s))==s,"Axis persistence");}
  World w=new World();w.setBlockState(p,broken.getDefaultState(),3);require(id(w,p).equals("marble_zone_pillar_broken"),"Detached cap");w.setBlockState(p.down(),basic.getDefaultState(),3);require(id(w,p).equals("marble_zone_pillar_broken_connected")&&id(w,p.down()).endsWith("_bottom"),"Late support updates both blocks");w.setBlockState(p.down(),Blocks.AIR.getDefaultState(),3);require(id(w,p).equals("marble_zone_pillar_broken"),"Removing support restores broken base");
  w=new World();w.setBlockState(p,basic.getDefaultState(),3);w.setBlockState(p.up(),basic.getDefaultState().withProperty(MarbleColumn.AXIS,EnumFacing.Axis.X),3);require(id(w,p).equals("marble_zone_pillar"),"Perpendicular axes do not join");
  require(basic.withRotation(basic.getStateFromMeta(1),Rotation.CLOCKWISE_90).getValue(MarbleColumn.AXIS)==EnumFacing.Axis.Z,"Structure rotation");
  int count=0;for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=Content.BLOCKS.get(d.id);if(!(b instanceof MarbleColumn))continue;count++;MarbleColumn c=(MarbleColumn)b;Item expected=Item.getItemFromBlock(c.broken?broken:basic);require(c.getItemDropped(c.getDefaultState(),new Random(),0)==expected&&c.getSilkTouchDrop(c.getDefaultState()).getItem()==expected&&c.getPickBlock(c.getDefaultState(),null,w,p,null).getItem()==expected,"Normalized drop/pick/silk "+d.id);for(IBlockState state:c.getBlockState().getValidStates()){require(c.getExplosionResistance(null)==10,"Source resistance");for(AxisAlignedBB box:c.boxes(state))require(box.minX>=0&&box.maxX<=1&&box.minY>=0&&box.maxY<=1&&box.minZ>=0&&box.maxZ<=1,"Source shape bounds");}}
  w=new World();IBlockState state=basic.getDefaultState();List<AxisAlignedBB> collisions=new ArrayList<>();basic.addCollisionBoxToList(state,w,p,new AxisAlignedBB(.01,64.4,.01,.1,64.6,.1),collisions,null,false);require(collisions.isEmpty(),"Empty shaft corner is not solid");require(basic.collisionRayTrace(state,w,p,new Vec3d(.05,64.5,-1),new Vec3d(.05,64.5,2))==null,"Ray misses empty corner");require(basic.collisionRayTrace(state,w,p,new Vec3d(.5,64.5,-1),new Vec3d(.5,64.5,2))!=null,"Ray hits shaft");
  require(count==6&&ZoneStructures.PILLARS.length==12,"Complete Marble column set");System.out.println("PASS: 6 source column states, three-axis joining/splitting, broken cap transitions, rotation/NBT metadata, drops/pick/silk, compound collisions and ray hits; 12 structure variants");
 }
}
