package org.sonicraft.backport;
import net.minecraft.entity.*;import net.minecraft.entity.player.*;import net.minecraft.item.ItemStack;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.World;
import software.bernie.geckolib3.core.*;import software.bernie.geckolib3.core.builder.AnimationBuilder;import software.bernie.geckolib3.core.controller.AnimationController;import software.bernie.geckolib3.core.event.predicate.AnimationEvent;import software.bernie.geckolib3.core.manager.*;
public class Tornado47 extends EntityCreature implements IAnimatable {
 private final AnimationFactory factory=new AnimationFactory(this);float throttle,vertical;int lastInput=-1000;private boolean reclaimed;
 public Tornado47(World w){super(w);setSize(2.4f,1.8f);stepHeight=.6f;enablePersistence();}
 protected void initEntityAI(){}protected boolean canDespawn(){return false;}
 protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(80);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.3);getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2);}
 public Entity getControllingPassenger(){return getPassengers().isEmpty()?null:getPassengers().get(0);}
 protected boolean canFitPassenger(Entity e){return getPassengers().isEmpty();}
 public boolean canPassengerSteer(){return !world.isRemote;}public boolean canBeSteered(){return true;}public boolean canBePushed(){return false;}public void applyEntityCollision(Entity e){}
 public double getMountedYOffset(){return .85;}
 public void updatePassenger(Entity e){if(isPassenger(e))e.setPosition(posX,posY+getMountedYOffset()+e.getYOffset(),posZ);}
 public boolean processInteract(EntityPlayer p,EnumHand hand){if(hand!=EnumHand.MAIN_HAND)return false;if(world.isRemote)return true;
  if(p.isSneaking()&&!isBeingRidden()&&onGround){ItemStack stack=new ItemStack(Vehicles47.PLANE);stack.setTagCompound(new NBTTagCompound());stack.getTagCompound().setFloat("PlaneHealth",getHealth());if(!p.inventory.addItemStackToInventory(stack))entityDropItem(stack,0);reclaimed=true;setDead();return true;}
  if(!isBeingRidden())p.startRiding(this);return true;
 }
 public void travel(float strafe,float up,float forward){
  if(world.isRemote)return;
  Entity pilot=getControllingPassenger();boolean riding=pilot instanceof EntityPlayer&&pilot.isEntityAlive();setNoGravity(riding);
  if(riding){boolean fresh=ticksExisted-lastInput<15;rotationYaw=pilot.rotationYaw;rotationYawHead=renderYawOffset=rotationYaw;rotationPitch=0;double speed=fresh?MathHelper.clamp(throttle,-1,1)*.6:0;double yaw=Math.toRadians(rotationYaw);motionX=-Math.sin(yaw)*speed;motionZ=Math.cos(yaw)*speed;motionY=fresh?MathHelper.clamp(vertical,-1,1)*.2:0;fallDistance=0;pilot.fallDistance=0;}
  else{motionX*=.85;motionZ*=.85;motionY=Math.max(-.5,motionY-.04);}
  // Do not force-load terrain ahead of a moving vehicle.
  BlockPos next=new BlockPos(posX+motionX,posY+motionY,posZ+motionZ);if(!world.isAreaLoaded(next.add(-2,0,-2),next.add(2,2,2))){motionX=motionY=motionZ=0;return;}
  move(MoverType.SELF,motionX,motionY,motionZ);if(onGround)motionY=0;velocityChanged=true;
 }
 public void fall(float distance,float multiplier){fallDistance=0;}
 public void onDeath(DamageSource cause){super.onDeath(cause);removePassengers();if(!world.isRemote&&!reclaimed&&world.getGameRules().getBoolean("doMobLoot")){reclaimed=true;entityDropItem(new ItemStack(net.minecraft.init.Items.IRON_INGOT,4),0);}}
 private <E extends IAnimatable> PlayState animation(AnimationEvent<E> e){e.getController().setAnimation(new AnimationBuilder().addAnimation(isBeingRidden()?"fly":"idle",true));return PlayState.CONTINUE;}
 public void registerControllers(AnimationData d){d.addAnimationController(new AnimationController<Tornado47>(this,"flight",4,this::animation));}public AnimationFactory getFactory(){return factory;}
}
