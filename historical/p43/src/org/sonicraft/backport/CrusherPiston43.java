package org.sonicraft.backport;
import java.util.UUID;import net.minecraft.entity.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.world.*;
import software.bernie.geckolib3.core.*;import software.bernie.geckolib3.core.builder.AnimationBuilder;import software.bernie.geckolib3.core.controller.AnimationController;import software.bernie.geckolib3.core.event.predicate.AnimationEvent;import software.bernie.geckolib3.core.manager.*;
public final class CrusherPiston43 extends EntityLiving implements IAnimatable {
 UUID parentId;int index;double ax,ay,az;private int missing;private final AnimationFactory factory=new AnimationFactory(this);
 public CrusherPiston43(World w){super(w);setSize(1.5f,4.5f);setNoGravity(true);setNoAI(true);enablePersistence();}
 void bind(EggCrusherBoss parent,int i,double x,double y,double z){parentId=parent.getUniqueID();index=i;ax=x;ay=y;az=z;setLocationAndAngles(x,y,z,90,0);}
 protected void initEntityAI(){}protected boolean canDespawn(){return false;}
 EggCrusherBoss parent(){if(!(world instanceof WorldServer)||parentId==null)return null;Entity e=((WorldServer)world).getEntityFromUuid(parentId);return e instanceof EggCrusherBoss?(EggCrusherBoss)e:null;}
 public boolean attackEntityFrom(DamageSource src,float amount){EggCrusherBoss p=parent();return p!=null&&p.isEntityAlive()&&p.attackEntityFrom(src,amount);}
 public boolean canBePushed(){return false;}public void applyEntityCollision(Entity e){}
 public void onLivingUpdate(){super.onLivingUpdate();setNoGravity(true);motionX=motionY=motionZ=0;if(world.isRemote)return;
  EggCrusherBoss p=parent();if(p==null){if(++missing>200&&world.isAreaLoaded(getPosition().add(-6,0,-6),getPosition().add(6,8,6)))setDead();return;}missing=0;if(!p.isEntityAlive()){setDead();return;}
  // Alternate diagonal pairs: lift, pause, then a quick downward stroke.
  int phase=(int)((world.getTotalWorldTime()+((index==0||index==3)?0:70))%140);
  double lift=phase<30?phase/30.0*3.4:phase<65?3.4:phase<75?3.4*(75-phase)/10.0:0;
  double before=posY;setPosition(ax,ay+lift,az);rotationYaw=rotationYawHead=renderYawOffset=90;fallDistance=0;
  if(posY<before&&ticksExisted%5==0)for(EntityLivingBase e:world.getEntitiesWithinAABB(EntityLivingBase.class,new AxisAlignedBB(ax-.75,posY-.2,az-.75,ax+.75,before+.2,az+.75)))
   if(e instanceof EntityPlayer||e instanceof LegacyCharacter)e.attackEntityFrom(DamageSource.causeMobDamage(p),12);
 }
 public void writeEntityToNBT(NBTTagCompound n){super.writeEntityToNBT(n);if(parentId!=null)n.setUniqueId("Controller",parentId);n.setInteger("PistonIndex",index);n.setDouble("AnchorX",ax);n.setDouble("AnchorY",ay);n.setDouble("AnchorZ",az);}
 public void readEntityFromNBT(NBTTagCompound n){super.readEntityFromNBT(n);parentId=n.hasUniqueId("Controller")?n.getUniqueId("Controller"):null;index=n.getInteger("PistonIndex");ax=n.getDouble("AnchorX");ay=n.getDouble("AnchorY");az=n.getDouble("AnchorZ");}
 private <T extends IAnimatable> PlayState idle(AnimationEvent<T> e){e.getController().setAnimation(new AnimationBuilder().addAnimation("idle",true));return PlayState.CONTINUE;}
 public void registerControllers(AnimationData d){d.addAnimationController(new AnimationController<CrusherPiston43>(this,"piston",0,this::idle));}public AnimationFactory getFactory(){return factory;}
}
