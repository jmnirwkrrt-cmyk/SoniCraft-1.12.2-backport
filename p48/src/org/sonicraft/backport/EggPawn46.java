package org.sonicraft.backport;
import net.minecraft.entity.*;import net.minecraft.entity.ai.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.entity.projectile.EntitySnowball;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.World;
public class EggPawn46 extends GroundBadnik implements IRangedAttackMob {
 private static final Badniks.Definition DEF=make();
 private static Badniks.Definition make(){Badniks.Definition d=new Badniks.Definition();d.id="egg_pawn";d.width=.9f;d.height=1.6f;d.step=.6f;d.health=8;d.speed=.25;d.damage=5;d.follow=16;d.knockback=1;return d;}
 public EggPawn46(World w){super(w);}
 Badniks.Definition definition(){return DEF;}
 protected void initEntityAI(){tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(1,new EntityAIAttackRanged(this,1,30,16));tasks.addTask(5,new EntityAIWander(this,.8));tasks.addTask(6,new EntityAILookIdle(this));targetTasks.addTask(1,new EntityAIHurtByTarget(this,false));targetTasks.addTask(2,new EntityAINearestAttackableTarget<EntityPlayer>(this,EntityPlayer.class,true){public boolean shouldExecute(){return playersHostile()&&super.shouldExecute();}});}
 public void setSwingingArms(boolean b){}
 public void attackEntityWithRangedAttack(EntityLivingBase target,float strength){Bullet bullet=new Bullet(world,this);double x=target.posX-posX,z=target.posZ-posZ,y=target.posY+target.getEyeHeight()-.1-bullet.posY;bullet.shoot(x,y+Math.sqrt(x*x+z*z)*.05,z,1.6f,2);world.spawnEntity(bullet);world.playSound(null,getPosition(),net.minecraft.init.SoundEvents.ENTITY_SNOWBALL_THROW,SoundCategory.HOSTILE,.6f,.7f);}
 public static class Bullet extends EntitySnowball {
  public Bullet(World w){super(w);}public Bullet(World w,EntityLivingBase e){super(w,e);}
  protected void onImpact(RayTraceResult hit){if(world.isRemote)return;if(hit.entityHit!=null&&hit.entityHit!=getThrower())hit.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this,getThrower()),5);world.setEntityState(this,(byte)3);setDead();}
  protected float getGravityVelocity(){return .005f;}
 }
}
