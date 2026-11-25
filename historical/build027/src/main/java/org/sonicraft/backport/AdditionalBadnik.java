package org.sonicraft.backport;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIAttackRanged;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.EnumDifficulty;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/** Source models and attributes; movement/AI adapted to the 1.12.2 entity engine.
 * Newly introduced mobs keep stable registry names independently of later behavior upgrades. */
public final class AdditionalBadnik {
 private AdditionalBadnik(){}
 public static class Crabmeat extends Ranged {public Crabmeat(World w){super(w);}}
 public static class Spiny extends Ranged {public Spiny(World w){super(w);}}
 public static class BallHog extends Ranged {public BallHog(World w){super(w);}}
 public static class Caterkiller extends GroundBadnik {public Caterkiller(World w){super(w);}}
 public static class Splats extends GroundBadnik {public Splats(World w){super(w);}}
 public static class Roller extends GroundBadnik {public Roller(World w){super(w);}}

 /** Vanilla projectile substitute for source mechanical projectiles; no terrain destruction. */
 public static abstract class Ranged extends GroundBadnik implements net.minecraft.entity.IRangedAttackMob {
  Ranged(World w){super(w);}
  @Override protected void initEntityAI(){super.initEntityAI();tasks.addTask(3,new EntityAIAttackRanged(this,1.0,40,12f));}
  @Override public void attackEntityWithRangedAttack(EntityLivingBase target,float distanceFactor){
   if(world.isRemote||!target.isEntityAlive())return;
   EntityTippedArrow arrow=new EntityTippedArrow(world,this);
   double dx=target.posX-posX,dz=target.posZ-posZ;
   double dy=target.getEntityBoundingBox().minY+target.height*.6-arrow.posY;
   arrow.shoot(dx,dy+MathHelper.sqrt(dx*dx+dz*dz)*.13,dz,1.2f,4f);
   arrow.setDamage(this instanceof BallHog?3:2);
   world.spawnEntity(arrow);
  }
  @Override public void setSwingingArms(boolean swinging){}
  @Override public void registerControllers(AnimationData data){
   data.addAnimationController(new AnimationController<GroundBadnik>(this,"movement",5,this::animated));
  }
  private <T extends IAnimatable> PlayState animated(AnimationEvent<T> e){
   String animation="idle";
   if(getAttackTarget()!=null&&getAttackTarget().isEntityAlive()){
    double d=getDistanceSq(getAttackTarget());
    if(d<144)animation=this instanceof BallHog?"attack":"shoot";
    else if(e.isMoving())animation="walk";
   }else if(e.isMoving())animation="walk";
   e.getController().setAnimation(new AnimationBuilder().addAnimation(animation,true));
   return PlayState.CONTINUE;
  }
 }

 /** Hovering, target-following adaptation for source Batbrain and Buzz Bomber. */
 public static abstract class Flying extends GroundBadnik {
  Flying(World w){super(w);setNoGravity(true);}
  @Override protected void initEntityAI(){
   targetTasks.addTask(1,new EntityAINearestAttackableTarget<EntityPlayer>(this,EntityPlayer.class,10,false,false,null){
    @Override public boolean shouldExecute(){return world.getDifficulty()!=EnumDifficulty.PEACEFUL&&super.shouldExecute();}});
   targetTasks.addTask(2,new EntityAINearestAttackableTarget<LegacyCharacter.Sonic>(this,LegacyCharacter.Sonic.class,10,false,false,null));
   targetTasks.addTask(3,new EntityAIHurtByTarget(this,false));
  }
  @Override public void onLivingUpdate(){
   setNoGravity(true);super.onLivingUpdate();
   if(world.isRemote||isDead)return;
   EntityLivingBase target=getAttackTarget();
   if(target!=null&&target.isEntityAlive()&&getDistanceSq(target)<256){
    double dx=target.posX-posX,dy=target.posY+target.height*.8-posY,dz=target.posZ-posZ;
    double len=Math.sqrt(dx*dx+dy*dy+dz*dz);
    if(len>.01){motionX=motionX*.75+dx/len*.055;motionY=motionY*.75+dy/len*.052;motionZ=motionZ*.75+dz/len*.055;}
    if(this instanceof BuzzBomber&&len<15&&ticksExisted%55==0){
     EntityTippedArrow shot=new EntityTippedArrow(world,this);
     shot.shoot(dx,dy+Math.sqrt(dx*dx+dz*dz)*.1,dz,1.0f,7f);
     shot.setDamage(2);world.spawnEntity(shot);
    }
    if(getEntityBoundingBox().grow(.6).intersects(target.getEntityBoundingBox())&&ticksExisted%24==0)attackEntityAsMob(target);
   }else{
    motionX*=.78;motionZ*=.78;motionY=motionY*.7+Math.sin((ticksExisted+getEntityId())*.07)*.006;
   }
   velocityChanged=true;
  }
  @Override public void registerControllers(AnimationData data){
   data.addAnimationController(new AnimationController<GroundBadnik>(this,"movement",4,this::animated));
  }
  private <T extends IAnimatable> PlayState animated(AnimationEvent<T> e){
   String a=this instanceof Batbrain?(getAttackTarget()!=null?"fly":"hang"):(getAttackTarget()!=null?"aggro":"idle");
   e.getController().setAnimation(new AnimationBuilder().addAnimation(a,true));
   return PlayState.CONTINUE;
  }
 }
 public static class Batbrain extends Flying {public Batbrain(World w){super(w);}}
 public static class BuzzBomber extends Flying {public BuzzBomber(World w){super(w);}}
}
