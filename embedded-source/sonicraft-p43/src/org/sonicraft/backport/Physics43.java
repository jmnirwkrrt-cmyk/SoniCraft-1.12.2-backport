package org.sonicraft.backport;
import net.minecraft.entity.*;import net.minecraft.entity.player.*;import net.minecraft.block.state.IBlockState;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.World;import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.event.entity.living.LivingEvent;
@Mod.EventBusSubscriber(modid="sonicraft")
public final class Physics43 {
 public static void cannon(World w,BlockPos p,IBlockState s,Entity e){
  if(!P34.actor(w,e))return;long now=w.getTotalWorldTime();NBTTagCompound n=e.getEntityData();
  if(n.hasKey("sc34CannonPos")&&n.getLong("sc34CannonPos")==p.toLong()&&n.getLong("sc34CannonEnd")>now)return;
  EnumFacing f=P34.dir(s,false);
  if(!n.hasKey("SC43FlightEnd"))n.setBoolean("SC43GravityBefore",e.hasNoGravity());
  n.setLong("sc34CannonPos",p.toLong());n.setLong("sc34CannonEnd",now+4);n.setLong("SC43FlightEnd",now+(f.getAxis()==EnumFacing.Axis.Y?10:40));n.setInteger("SC43FlightDim",e.dimension);
  e.setNoGravity(true);e.motionX=f.getFrontOffsetX()*2;e.motionY=f.getFrontOffsetY()*1.8;e.motionZ=f.getFrontOffsetZ()*2;e.fallDistance=0;e.velocityChanged=true;e.isAirBorne=true;
  if(e instanceof EntityPlayerMP)((EntityPlayerMP)e).connection.sendPacket(new SPacketEntityVelocity(e));
 }
 @SubscribeEvent public static void flight(LivingEvent.LivingUpdateEvent event){EntityLivingBase e=event.getEntityLiving();NBTTagCompound n=e.getEntityData();if(!n.hasKey("SC43FlightEnd"))return;
  long now=e.world.getTotalWorldTime(),end=n.getLong("SC43FlightEnd");e.fallDistance=0;
  if(e.world.isRemote)return;
  if(now>=end||end>now+40||e.dimension!=n.getInteger("SC43FlightDim")||!e.isEntityAlive()||e.isRiding()){
   e.setNoGravity(n.getBoolean("SC43GravityBefore"));n.removeTag("SC43FlightEnd");n.removeTag("SC43GravityBefore");n.removeTag("SC43FlightDim");
  }else e.setNoGravity(true);
 }
}
