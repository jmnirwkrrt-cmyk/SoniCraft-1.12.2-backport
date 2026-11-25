package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
final class MarbleColumn extends Block {
 static final PropertyEnum<EnumFacing.Axis> AXIS=PropertyEnum.create("axis",EnumFacing.Axis.class);
 static class Shapes {double[][] x,y,z;}
 static final Map<String,Shapes> SHAPES=loadShapes();
 static Map<String,Shapes> loadShapes(){com.google.gson.reflect.TypeToken<Map<String,Shapes>> type=new com.google.gson.reflect.TypeToken<Map<String,Shapes>>(){};try(java.io.InputStream in=MarbleColumn.class.getResourceAsStream("/assets/sonicraft/backport/marble_column_shapes.json")){return new com.google.gson.Gson().fromJson(new java.io.InputStreamReader(in,java.nio.charset.StandardCharsets.UTF_8),type.getType());}catch(java.io.IOException e){throw new IllegalStateException(e);}}
 final String id;final boolean broken;
 MarbleColumn(String id){super(Material.ROCK);this.id=id;broken=id.contains("broken");setDefaultState(blockState.getBaseState().withProperty(AXIS,EnumFacing.Axis.Y));setHardness(1).setResistance(50f/3f);setHarvestLevel("pickaxe",0);setSoundType(SoundType.STONE);setLightOpacity(0);}
 @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,AXIS);}
 @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(AXIS,broken?EnumFacing.Axis.Y:meta==1?EnumFacing.Axis.X:meta==2?EnumFacing.Axis.Z:EnumFacing.Axis.Y);}
 @Override public int getMetaFromState(IBlockState s){return broken?0:s.getValue(AXIS)==EnumFacing.Axis.X?1:s.getValue(AXIS)==EnumFacing.Axis.Z?2:0;}
 @Override public IBlockState getStateForPlacement(World w,BlockPos p,EnumFacing f,float x,float y,float z,int meta,EntityLivingBase e){return getDefaultState().withProperty(AXIS,broken?EnumFacing.Axis.Y:f.getAxis());}
 @Override public IBlockState withRotation(IBlockState s,Rotation r){if(!broken&&(r==Rotation.CLOCKWISE_90||r==Rotation.COUNTERCLOCKWISE_90)&&s.getValue(AXIS)!=EnumFacing.Axis.Y)return s.withProperty(AXIS,s.getValue(AXIS)==EnumFacing.Axis.X?EnumFacing.Axis.Z:EnumFacing.Axis.X);return s;}
 @Override public boolean isOpaqueCube(IBlockState s){return false;}
 @Override public boolean isFullCube(IBlockState s){return false;}
 List<AxisAlignedBB> boxes(IBlockState s){Shapes shape=SHAPES.get(id);double[][] values=s.getValue(AXIS)==EnumFacing.Axis.X?shape.x:s.getValue(AXIS)==EnumFacing.Axis.Z?shape.z:shape.y;List<AxisAlignedBB> out=new ArrayList<>();for(double[] v:values)out.add(new AxisAlignedBB(v[0],v[1],v[2],v[3],v[4],v[5]));return out;}
 @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){AxisAlignedBB box=null;for(AxisAlignedBB b:boxes(s))box=box==null?b:box.union(b);return box;}
 @Override public void addCollisionBoxToList(IBlockState s,World w,BlockPos p,AxisAlignedBB area,List<AxisAlignedBB> out,Entity e,boolean actual){for(AxisAlignedBB b:boxes(s))addCollisionBoxToList(p,area,out,b);}
 @Override public RayTraceResult collisionRayTrace(IBlockState s,World w,BlockPos p,Vec3d start,Vec3d end){RayTraceResult best=null;for(AxisAlignedBB b:boxes(s)){RayTraceResult hit=rayTrace(p,start,end,b);if(hit!=null&&(best==null||start.squareDistanceTo(hit.hitVec)<start.squareDistanceTo(best.hitVec)))best=hit;}return best;}
 @Override public BlockFaceShape getBlockFaceShape(IBlockAccess w,IBlockState s,BlockPos p,EnumFacing face){for(AxisAlignedBB b:boxes(s)){boolean full=face.getAxis()==EnumFacing.Axis.X?b.minY==0&&b.maxY==1&&b.minZ==0&&b.maxZ==1:face.getAxis()==EnumFacing.Axis.Y?b.minX==0&&b.maxX==1&&b.minZ==0&&b.maxZ==1:b.minX==0&&b.maxX==1&&b.minY==0&&b.maxY==1;double v=face==EnumFacing.WEST?b.minX:face==EnumFacing.EAST?b.maxX:face==EnumFacing.DOWN?b.minY:face==EnumFacing.UP?b.maxY:face==EnumFacing.NORTH?b.minZ:b.maxZ;if(full&&v==(face.getAxisDirection()==EnumFacing.AxisDirection.POSITIVE?1:0))return BlockFaceShape.SOLID;}return BlockFaceShape.UNDEFINED;}
 static boolean visible(Block b){return !(b instanceof MarbleColumn)||((MarbleColumn)b).id.equals("marble_zone_pillar")||((MarbleColumn)b).id.equals("marble_zone_pillar_broken");}
 Item drop(){return Item.getItemFromBlock(Content.BLOCKS.get(broken?"marble_zone_pillar_broken":"marble_zone_pillar"));}
 @Override public Item getItemDropped(IBlockState s,Random r,int fortune){return drop();}
 @Override public int damageDropped(IBlockState s){return 0;}
 @Override protected ItemStack getSilkTouchDrop(IBlockState s){return new ItemStack(drop());}
 @Override public ItemStack getPickBlock(IBlockState s,RayTraceResult target,World w,BlockPos p,EntityPlayer e){return new ItemStack(drop());}
 static boolean tagged(IBlockState s){return s.getBlock() instanceof MarbleColumn&&!((MarbleColumn)s.getBlock()).id.equals("marble_zone_pillar_broken");}
 static boolean aligned(IBlockState s,EnumFacing.Axis axis){if(!tagged(s))return false;MarbleColumn b=(MarbleColumn)s.getBlock();return b.broken?axis==EnumFacing.Axis.Z:s.getValue(AXIS)==axis;}
 static IBlockState connected(IBlockState state,IBlockAccess world,BlockPos p){MarbleColumn b=(MarbleColumn)state.getBlock();EnumFacing.Axis axis=state.getValue(AXIS);String suffix;
  if(b.broken)suffix=aligned(world.getBlockState(p.down()),EnumFacing.Axis.Y)?"_broken_connected":"_broken";
  else {EnumFacing upper=axis==EnumFacing.Axis.X?EnumFacing.EAST:axis==EnumFacing.Axis.Y?EnumFacing.UP:EnumFacing.NORTH;IBlockState above=world.getBlockState(p.offset(upper)),below=world.getBlockState(p.offset(upper.getOpposite()));boolean up=aligned(above,axis)||(axis==EnumFacing.Axis.Y&&above.getBlock() instanceof MarbleColumn&&((MarbleColumn)above.getBlock()).id.endsWith("broken_connected")),down=aligned(below,axis);suffix=up?(down?"_middle":"_bottom"):(down?"_top":"");}
  Block target=Content.BLOCKS.get("marble_zone_pillar"+suffix);return target==null?state:target.getDefaultState().withProperty(AXIS,b.broken?EnumFacing.Axis.Y:axis);
 }
 static void refresh(World w,BlockPos p){if(w.isRemote||!w.isBlockLoaded(p)||!(w.getBlockState(p).getBlock() instanceof MarbleColumn))return;IBlockState old=w.getBlockState(p);for(EnumFacing f:EnumFacing.values())if(!w.isBlockLoaded(p.offset(f)))return;IBlockState next=connected(old,w,p);if(next!=old){w.setBlockState(p,next,2);w.notifyNeighborsOfStateChange(p,next.getBlock(),false);}}
 @Override public void onBlockAdded(World w,BlockPos p,IBlockState s){refresh(w,p);}
 @Override public void neighborChanged(IBlockState s,World w,BlockPos p,Block neighbor,BlockPos from){refresh(w,p);}
}
