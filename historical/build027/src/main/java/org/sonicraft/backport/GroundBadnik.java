package org.sonicraft.backport;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import software.bernie.geckolib3.core.*;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.*;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
public abstract class GroundBadnik extends EntityCreature implements IAnimatable {
 private final AnimationFactory factory=new AnimationFactory(this);private boolean released;
 public GroundBadnik(World w){super(w);Badniks.Definition d=definition();setSize(d.width,d.height);stepHeight=d.step;experienceValue=0;isImmuneToFire=this instanceof Rhinobot;if(this instanceof Burrobot)enablePersistence();}
 @Override public boolean isCreatureType(EnumCreatureType type,boolean forSpawnCount){return type==EnumCreatureType.CREATURE;}
 Badniks.Definition definition(){return Badniks.definition(getClass());}
 @Override protected void applyEntityAttributes(){super.applyEntityAttributes();Badniks.Definition d=definition();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(d.health);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(d.speed);getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(d.armor);getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(d.follow);getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(d.knockback);getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(d.damage);}
 boolean playersHostile(){return world.getDifficulty()!=EnumDifficulty.PEACEFUL;}
 @Override protected void initEntityAI(){
  targetTasks.addTask(this instanceof Burrobot?2:1,new EntityAINearestAttackableTarget<LegacyCharacter.Sonic>(this,LegacyCharacter.Sonic.class,10,false,false,null));
  targetTasks.addTask(this instanceof Burrobot?1:2,new EntityAINearestAttackableTarget<EntityPlayer>(this,EntityPlayer.class,10,false,false,null){@Override public boolean shouldExecute(){return playersHostile()&&super.shouldExecute();}});
  targetTasks.addTask(3,new EntityAIHurtByTarget(this,false){@Override public boolean shouldExecute(){return (GroundBadnik.this instanceof Burrobot||!(getAttackTarget() instanceof EntityPlayer))&&super.shouldExecute();}});
  tasks.addTask(4,new EntityAIAttackMelee(this,this instanceof Burrobot?1.2:1,false){@Override protected double getAttackReachSqr(EntityLivingBase target){return width*width+target.width;}});
  if(this instanceof Burrobot)tasks.addTask(5,new EntityAIWander(this,.8,20){@Override protected Vec3d getPosition(){return new Vec3d(posX+(rand.nextFloat()*2-1)*16,posY+(rand.nextFloat()*2-1)*16,posZ+(rand.nextFloat()*2-1)*16);}});else tasks.addTask(5,new EntityAIWander(this,1));
  tasks.addTask(6,new EntityAILookIdle(this));if(this instanceof Rhinobot)tasks.addTask(7,new EntityAISwimming(this));
 }
 @Override public boolean attackEntityAsMob(Entity target){return target.attackEntityFrom(DamageSource.causeMobDamage(this),(float)getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());}
 @Override protected SoundEvent getHurtSound(DamageSource source){return Badniks.SOUNDS.get("badnik.hurt");}
 @Override public boolean attackEntityFrom(DamageSource s,float amount){if(s==DamageSource.FALL||s==DamageSource.DROWN||s==DamageSource.LIGHTNING_BOLT||(this instanceof Rhinobot&&s.isFireDamage()))return false;return super.attackEntityFrom(s,amount);}
 void peacefulTick(){if(!world.isRemote&&world.getDifficulty()==EnumDifficulty.PEACEFUL){if(!Badniks.peacefulBadniks(world))setDead();}}
 @Override public void onLivingUpdate(){peacefulTick();if(!isDead)super.onLivingUpdate();}
 @Override public boolean getCanSpawnHere(){return (world.getDifficulty()!=EnumDifficulty.PEACEFUL||Badniks.peacefulBadniks(world))&&world.getBlockState(new BlockPos(this).down()).isTopSolid()&&world.checkNoEntityCollision(getEntityBoundingBox())&&world.getCollisionBoxes(this,getEntityBoundingBox()).isEmpty()&&!world.containsAnyLiquid(getEntityBoundingBox());}
 @Override public void onDeath(DamageSource cause){if(released)return;super.onDeath(cause);if(dead)releaseContents();}
 void releaseContents(){if(world.isRemote||released)return;released=true;world.playSound(null,posX,posY,posZ,Badniks.SOUNDS.get("monitor.destroy"),SoundCategory.NEUTRAL,1,1);if(world instanceof WorldServer)((WorldServer)world).spawnParticle(EnumParticleTypes.EXPLOSION_NORMAL,posX,posY+height/2,posZ,1,.01,.01,.01,0);
  if(world.getGameRules().getBoolean("doMobLoot")){int amount=rand.nextInt(3);if(amount>0)entityDropItem(new net.minecraft.item.ItemStack(Badniks.SCRAP,amount),0);}
  EntityCreature animal=rand.nextBoolean()?new FreedAnimal.Flicky(world):new FreedAnimal.Pocky(world);animal.setLocationAndAngles(posX,posY,posZ,rand.nextFloat()*360,0);world.spawnEntity(animal);setDead();
 }
 @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);tag.setBoolean("BadnikReleased",released);}
 @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);released=tag.getBoolean("BadnikReleased");}
 private <E extends IAnimatable> PlayState movement(AnimationEvent<E> event){event.getController().setAnimation(new AnimationBuilder().addAnimation(event.isMoving()?"walk":"idle",true));return PlayState.CONTINUE;}
 @Override public void registerControllers(AnimationData data){data.addAnimationController(new AnimationController<GroundBadnik>(this,"movement",4,this::movement));}
 @Override public AnimationFactory getFactory(){return factory;}
 public static class Motobug extends GroundBadnik {public Motobug(World w){super(w);}}
 public static class Rhinobot extends GroundBadnik {public Rhinobot(World w){super(w);}}
 public static class Burrobot extends GroundBadnik {public Burrobot(World w){super(w);}}
}
