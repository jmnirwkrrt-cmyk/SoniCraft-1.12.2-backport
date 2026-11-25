package org.sonicraft.backport;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
public abstract class FreedAnimal extends EntityCreature {
 public FreedAnimal(World w){super(w);setSize(.2f,.4f);stepHeight=.6f;experienceValue=0;enablePersistence();}
 @Override public boolean isCreatureType(EnumCreatureType type,boolean forSpawnCount){return type==EnumCreatureType.CREATURE;}
 @Override protected void applyEntityAttributes(){super.applyEntityAttributes();getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8);getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.2);getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16);getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3);}
 @Override protected void initEntityAI(){tasks.addTask(1,new EntityAIWander(this,1));tasks.addTask(2,new EntityAILookIdle(this));tasks.addTask(3,new EntityAISwimming(this));tasks.addTask(4,new EntityAILeapAtTarget(this,.5f));}
 @Override protected boolean canDespawn(){return false;}
 @Override public boolean attackEntityFrom(DamageSource s,float amount){return s!=DamageSource.FALL&&super.attackEntityFrom(s,amount);}
 @Override protected SoundEvent getHurtSound(DamageSource s){return Badniks.SOUNDS.get("animal.tweet");}
 @Override protected SoundEvent getDeathSound(){return Badniks.SOUNDS.get("animal.tweet");}
 public static class Flicky extends FreedAnimal {public Flicky(World w){super(w);}}
 public static class Pocky extends FreedAnimal {public Pocky(World w){super(w);}}
}
