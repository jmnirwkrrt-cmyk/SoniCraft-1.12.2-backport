package org.sonicraft.backport;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.network.datasync.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.*;
public class LegacyCharacter extends EntityCreature implements IAnimatable {
 private final AnimationFactory factory=new AnimationFactory(this);
 public LegacyCharacter(World world){super(world);Characters.Definition d=definition();setSize(d.width,d.height);setHealth(getMaxHealth());enablePersistence();if(getNavigator() instanceof PathNavigateGround)((PathNavigateGround)getNavigator()).setCanSwim(true);FoodContent.addCharacterGoals(this);}
 public Characters.Definition definition(){return Characters.forClass(getClass());}
 @Override protected void applyEntityAttributes(){super.applyEntityAttributes();Characters.Definition d=definition();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(d.health);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(d.speed);getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16);getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(d.armor);getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(d.damage);}
 @Override protected void initEntityAI(){
  tasks.addTask(0,new EntityAISwimming(this));tasks.addTask(2,new EntityAIAttackMelee(this,1.0,true));tasks.addTask(5,new EntityAIWanderAvoidWater(this,.65));tasks.addTask(6,new EntityAIWatchClosest(this,EntityPlayer.class,8));tasks.addTask(7,new EntityAILookIdle(this));targetTasks.addTask(1,new EntityAIHurtByTarget(this,false));
  if(Badniks.HERO_TARGETS.containsKey(definition().id))targetTasks.addTask(3,new EntityAINearestAttackableTarget<GroundBadnik>(this,GroundBadnik.class,10,false,false,other->other!=null&&Badniks.heroTargets(definition().id,other)));
  if(!definition().id.equals("eggman"))targetTasks.addTask(2,new EntityAINearestAttackableTarget<LegacyCharacter>(this,LegacyCharacter.class,10,true,false,other->other!=null&&!other.definition().id.equals("eggman")&&other.definition().enemy!=definition().enemy));
 }
 @Override public boolean processInteract(EntityPlayer player,EnumHand hand){return FoodContent.feed(this,player,hand)||super.processInteract(player,hand);}
 @Override public boolean attackEntityAsMob(Entity target){swingArm(EnumHand.MAIN_HAND);boolean hit=target.attackEntityFrom(DamageSource.causeMobDamage(this),(float)getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());if(hit){applyEnchantments(this,target);if(definition().id.equals("amy_rose")){if(!world.isRemote)world.playSound(null,posX,posY,posZ,FoodContent.SOUNDS.get("pikopikohammer.attack"),SoundCategory.NEUTRAL,1,1);if(target instanceof EntityLivingBase)((EntityLivingBase)target).knockBack(this,.6f,MathHelper.sin(rotationYaw*.017453292f),-MathHelper.cos(rotationYaw*.017453292f));}}return hit;}
 @Override public boolean getCanSpawnHere(){return world.getLightFromNeighbors(getPosition())>8&&world.getBlockState(getPosition().down()).isNormalCube()&&super.getCanSpawnHere();}
 @Override protected boolean canDespawn(){return false;}
 private SoundEvent voice(String type){String id=definition().sounds.get(type);return id==null?null:Characters.SOUNDS.get(id);}
 @Override protected SoundEvent getAmbientSound(){return voice("ambient");}
 @Override protected SoundEvent getHurtSound(DamageSource source){return voice("hurt");}
 @Override protected SoundEvent getDeathSound(){return voice("death");}
 @Override public int getTalkInterval(){return 240;}
 private <E extends IAnimatable> PlayState animation(AnimationEvent<E> event){Characters.Definition d=definition();if(d.geo==null)return PlayState.STOP;String clip="idle";if(controlledFlight()&&d.hasClip("fly"))clip="fly";else if(isSwingInProgress&&d.hasClip("attack"))clip="attack";else if(isInWater()&&d.hasClip("swim"))clip="swim";else if(!onGround&&d.hasClip("roll"))clip="roll";else if(!onGround&&d.hasClip("fly"))clip="fly";else if(event.isMoving())clip=d.hasClip("run")&&getAttackTarget()!=null?"run":"walk";if(!d.hasClip(clip))clip="idle";event.getController().setAnimation(new AnimationBuilder().addAnimation(clip,true));return PlayState.CONTINUE;}
 @Override public void registerControllers(AnimationData data){data.addAnimationController(new AnimationController<LegacyCharacter>(this,"main",3,this::animation));}
 @Override public AnimationFactory getFactory(){return factory;}
 protected boolean controlledFlight(){return false;}
 public static final class Sonic extends LegacyCharacter {public Sonic(World w){super(w);}}
 public static final class Tails extends LegacyCharacter {
  private static final DataParameter<Byte> FLIGHT_PHASE=EntityDataManager.createKey(Tails.class,DataSerializers.BYTE);
  final TailsFlightAI flight;
  public Tails(World w){super(w);flight=new TailsFlightAI(this);tasks.addTask(1,flight);}
  @Override protected void entityInit(){super.entityInit();dataManager.register(FLIGHT_PHASE,(byte)0);}
  void setFlightPhase(int phase){dataManager.set(FLIGHT_PHASE,(byte)phase);}
  @Override protected boolean controlledFlight(){return dataManager.get(FLIGHT_PHASE)!=0;}
  @Override public void onLivingUpdate(){flight.tick();super.onLivingUpdate();}
  @Override public void travel(float strafe,float vertical,float forward){
   if(!controlledFlight()){super.travel(strafe,vertical,forward);return;}
   move(MoverType.SELF,motionX,motionY,motionZ);
   motionX*=.8;motionY*=.8;motionZ*=.8;
   prevLimbSwingAmount=limbSwingAmount;limbSwingAmount*=.8f;
  }
  @Override public void fall(float distance,float multiplier){if(!flight.protectsFall())super.fall(distance,multiplier);}
  @Override public void writeEntityToNBT(NBTTagCompound tag){super.writeEntityToNBT(tag);flight.write(tag);}
  @Override public void readEntityFromNBT(NBTTagCompound tag){super.readEntityFromNBT(tag);flight.read(tag);}
 }
 public static final class Knuckles extends LegacyCharacter {public Knuckles(World w){super(w);}}
 public static final class AmyRose extends LegacyCharacter {public AmyRose(World w){super(w);}
  @Override protected void dropFewItems(boolean recentlyHit,int looting){super.dropFewItems(recentlyHit,looting);dropItem(FoodContent.ITEMS.get("piko_piko_hammer"),1);}
 }
 public static final class Rouge extends LegacyCharacter {public Rouge(World w){super(w);}}
 public static final class Shadow extends LegacyCharacter {public Shadow(World w){super(w);}}
 public static final class Silver extends LegacyCharacter {public Silver(World w){super(w);}}
 public static final class BigTheCat extends LegacyCharacter {public BigTheCat(World w){super(w);}}
 public static final class Espio extends LegacyCharacter {public Espio(World w){super(w);}}
 public static final class Charmy extends LegacyCharacter {public Charmy(World w){super(w);}}
 public static final class Vector extends LegacyCharacter {public Vector(World w){super(w);}}
 public static final class MetalSonic extends LegacyCharacter {public MetalSonic(World w){super(w);}}
 public static final class Omega extends LegacyCharacter {public Omega(World w){super(w);}}
 public static final class Infinite extends LegacyCharacter {public Infinite(World w){super(w);}}
 public static final class BlazeTheCat extends LegacyCharacter {public BlazeTheCat(World w){super(w);}}
 public static final class Cream extends LegacyCharacter {public Cream(World w){super(w);}}
 public static final class Eggman extends LegacyCharacter {public Eggman(World w){super(w);}}
}
