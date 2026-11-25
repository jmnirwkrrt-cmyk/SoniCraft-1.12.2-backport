package org.sonicraft.backport;
import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.entity.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.util.text.TextComponentTranslation;import net.minecraft.world.*;
public final class Blocks46 {
 public static boolean bulkKind(String kind,String prefix){return kind.startsWith(prefix)||kind.equals("source_cutout");}
 public static Block create(Content.BlockDef d){
  if(d.id.equals("studiopolis_popcorn_shop_floor"))return new PopcornFloor(d);
  if(d.id.equals("starpost_sonic_2"))return new Checkpoint(d);
  if(d.id.equals("studiopolis_interior_wall_lamp"))return new Lamp(d);
  if(d.kind.equals("source_cutout")&&d.id.endsWith("lamppost_base"))return new PoleBase(d);
  return Blocks43.create(d);
 }
 static class PopcornFloor extends BulkBlocks.Cube {
  PopcornFloor(Content.BlockDef d){super(d);}
  public void randomDisplayTick(IBlockState s,World w,BlockPos p,java.util.Random r){if(r.nextInt(5)!=0||!w.isAirBlock(p.up()))return;net.minecraft.item.Item item=net.minecraft.item.Item.REGISTRY.getObject(new ResourceLocation("sonicraft","piece_of_popcorn"));if(item!=null)w.spawnParticle(EnumParticleTypes.ITEM_CRACK,p.getX()+.5,p.getY()+1.1,p.getZ()+.5,(r.nextDouble()-.5)*.2,.5+r.nextDouble()*.5,(r.nextDouble()-.5)*.2,net.minecraft.item.Item.getIdFromItem(item));}
 }
 static class PoleBase extends BulkBlocks.Cutout {
  PoleBase(Content.BlockDef d){super(d);}
  public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return new AxisAlignedBB(.25,0,.25,.75,1,.75);}
 }
 static class Lamp extends BulkBlocks.Cutout {Lamp(Content.BlockDef d){super(d);setLightLevel(1);}}
 static class Checkpoint extends StructureFacadeBlocks.HorizontalVariant {
  Checkpoint(Content.BlockDef d){super(d);setLightOpacity(0);}
  public boolean isOpaqueCube(IBlockState s){return false;} public boolean isFullCube(IBlockState s){return false;}
  public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  public BlockFaceShape getBlockFaceShape(IBlockAccess w,IBlockState s,BlockPos p,EnumFacing f){return BlockFaceShape.UNDEFINED;}
  public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return NULL_AABB;}
  public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return new AxisAlignedBB(.2,0,.2,.8,1,.8);}
  public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){
   if(w.isRemote||!(e instanceof EntityPlayer)||((EntityPlayer)e).isSpectator())return;
   EntityPlayer player=(EntityPlayer)e;long key=p.toLong();
   if(player.getEntityData().getBoolean("SC46Checkpoint")&&player.getEntityData().getLong("SC46CheckpointPos")==key&&player.getEntityData().getInteger("SC46CheckpointDim")==w.provider.getDimension())return;
   player.setSpawnChunk(p,true,w.provider.getDimension());player.setSpawnDimension(w.provider.getDimension());player.getEntityData().setBoolean("SC46Checkpoint",true);player.getEntityData().setLong("SC46CheckpointPos",key);player.getEntityData().setInteger("SC46CheckpointDim",w.provider.getDimension());
   w.setBlockState(p,getStateFromMeta((getMetaFromState(s)&3)|8),3);MechanismSounds.play(w,p,"starpost.use");player.sendStatusMessage(new TextComponentTranslation("sonicraft.checkpoint_set"),true);
  }
 }
}
