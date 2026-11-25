package org.sonicraft.backport;
import java.util.*;import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.entity.*;import net.minecraft.entity.player.*;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.*;
public final class Teleport44 extends BulkBlocks.Cutout {
 public Teleport44(Content.BlockDef d){super(d);setLightOpacity(0);}
 public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return NULL_AABB;}
 public boolean isPassable(IBlockAccess w,BlockPos p){return true;}
 public void onEntityCollidedWithBlock(World w,BlockPos at,IBlockState state,Entity e){
  if(w.isRemote||!(e instanceof EntityPlayerMP)||e.isRiding()||e.isBeingRidden())return;
  long now=w.getTotalWorldTime(),last=e.getEntityData().getLong("SC44TeleportUntil");if(last>now&&last<=now+20)return;
  // Original AABB.ofSize(64,64,64): 32 blocks in each direction, not a radius of 64.
  List<TeleportTarget44> targets=w.getEntitiesWithinAABB(TeleportTarget44.class,new AxisAlignedBB(at.getX()-32,at.getY()-32,at.getZ()-32,at.getX()+32,at.getY()+32,at.getZ()+32));
  targets.sort(Comparator.comparingDouble(t->t.getDistanceSq(at.getX(),at.getY(),at.getZ())));
  if(targets.isEmpty())return;TeleportTarget44 t=targets.get(0);
  // Do not force-load chunks or place a player inside new obstructions.
  for(int[] d:new int[][]{{0,0},{1,0},{-1,0},{0,1},{0,-1}}){
   double x=t.posX+d[0],y=t.posY,z=t.posZ+d[1];BlockPos p=new BlockPos(x,y,z);
   if(!w.isAreaLoaded(p.add(-1,0,-1),p.add(1,2,1)))continue;
   AxisAlignedBB box=e.getEntityBoundingBox().offset(x-e.posX,y-e.posY,z-e.posZ);
   if(!w.getWorldBorder().contains(box)||!w.getCollisionBoxes(e,box).isEmpty()||w.containsAnyLiquid(box))continue;
   e.getEntityData().setLong("SC44TeleportUntil",now+20);e.motionX=e.motionY=e.motionZ=0;e.fallDistance=0;
   ((EntityPlayerMP)e).connection.setPlayerLocation(x,y,z,e.rotationYaw,e.rotationPitch);e.velocityChanged=true;
   ((EntityPlayerMP)e).connection.sendPacket(new net.minecraft.network.play.server.SPacketEntityVelocity(e));return;
  }
 }
 public void randomDisplayTick(IBlockState s,World w,BlockPos p,Random r){if(r.nextInt(3)==0)w.spawnParticle(EnumParticleTypes.PORTAL,p.getX()+r.nextDouble(),p.getY()+.5,p.getZ()+r.nextDouble(),0,.1,0);}
}
