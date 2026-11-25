package org.sonicraft.backport;
import net.minecraft.entity.Entity;import net.minecraft.world.World;import net.minecraft.nbt.NBTTagCompound;
/** Persistent, nonphysical source-template destination marker. */
public final class TeleportTarget44 extends Entity {
 public TeleportTarget44(World w){super(w);setSize(.01f,.01f);setNoGravity(true);setInvisible(true);noClip=true;}
 protected void entityInit(){}
 protected void readEntityFromNBT(NBTTagCompound n){}
 protected void writeEntityToNBT(NBTTagCompound n){}
 public void onUpdate(){super.onUpdate();motionX=motionY=motionZ=0;}
 public boolean canBeCollidedWith(){return false;}
 public boolean isInRangeToRenderDist(double d){return false;}
}
