package org.sonicraft.backport;
import java.util.*;
import com.google.common.base.Predicate;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class BadnikContentCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static class World extends FoodContentCheck.TestWorld {
  EnumDifficulty difficulty=EnumDifficulty.NORMAL;final List<Entity> entities=new ArrayList<>(),spawnedEntities=new ArrayList<>();
  @Override public EnumDifficulty getDifficulty(){return difficulty;}
  @Override public boolean spawnEntity(Entity e){spawnedEntities.add(e);return true;}
  @Override public void setEntityState(Entity e,byte state){}
  @Override public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> cls,AxisAlignedBB bounds,Predicate<? super T> filter){List<T> out=new ArrayList<>();for(Entity e:entities)if(cls.isInstance(e)&&e.getEntityBoundingBox().intersects(bounds)){T cast=cls.cast(e);if(filter==null||filter.apply(cast))out.add(cast);}return out;}
 }
 static class Player extends FoodContentCheck.Player {float damage;Player(World w){super(w);}@Override public boolean attackEntityFrom(DamageSource s,float amount){damage+=amount;return true;}}
 static class Navigator extends PathNavigateGround {
  int paths;Navigator(GroundBadnik e){super(e,e.world);}
  @Override public Path getPathToEntityLiving(Entity e){return new Path(new PathPoint[]{new PathPoint(1,64,0)});}
  @Override public boolean setPath(Path p,double speed){paths++;return true;}
  @Override public boolean tryMoveToEntityLiving(Entity e,double speed){paths++;return true;}
  @Override public boolean noPath(){return false;}
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();Badniks.sounds(new net.minecraftforge.event.RegistryEvent.Register<SoundEvent>(new ResourceLocation("minecraft","soundevents"),ForgeRegistries.SOUND_EVENTS));Characters.registerSounds(new net.minecraftforge.event.RegistryEvent.Register<SoundEvent>(new ResourceLocation("minecraft","soundevents"),ForgeRegistries.SOUND_EVENTS));require(Badniks.SOUNDS.get("badnik.hurt")==Characters.SOUNDS.get("badnik.hurt"),"Shared Metal Sonic sound without duplicate registration");ForgeRegistries.ITEMS.register(Badniks.SCRAP);World w=new World();Badniks.ensureRule(w);require(Badniks.peacefulBadniks(w),"Source gamerule default true");
  for(Badniks.Definition d:Badniks.DEFINITIONS){GroundBadnik robot=(GroundBadnik)Badniks.entityClass(d.id).getConstructor(net.minecraft.world.World.class).newInstance(w);require(robot.getMaxHealth()==8&&Math.abs(robot.width-d.width)<.001&&Math.abs(robot.stepHeight-d.step)<.001,"Source size/stats "+d.id);require(robot.isCreatureType(EnumCreatureType.CREATURE,true),"Creature spawn cap "+d.id);
   robot.setPosition(0,64,0);Player p=new Player(w);p.setPosition(.5,64,0);w.playerEntities.clear();w.playerEntities.add(p);w.entities.clear();w.entities.add(p);LegacyCharacter.Sonic sonic=new LegacyCharacter.Sonic(w);sonic.setPosition(2,64,0);w.entities.add(sonic);
   EntityAIBase primary=null;for(EntityAITasks.EntityAITaskEntry entry:robot.targetTasks.taskEntries)if(entry.priority==1)primary=entry.action;require(primary!=null,"Primary target goal");boolean selected=false;for(int i=0;i<120;i++)if(primary.shouldExecute()){selected=true;break;}require(selected,"Primary target can execute "+d.id);primary.startExecuting();require(robot.getAttackTarget()==(robot instanceof GroundBadnik.Burrobot?p:sonic),"Source primary target "+d.id);primary.resetTask();

   Navigator nav=new Navigator(robot);java.lang.reflect.Field f=EntityLiving.class.getDeclaredField("navigator");f.setAccessible(true);f.set(robot,nav);robot.setAttackTarget(p);EntityAIAttackMelee melee=null;for(EntityAITasks.EntityAITaskEntry e:robot.tasks.taskEntries)if(e.action instanceof EntityAIAttackMelee)melee=(EntityAIAttackMelee)e.action;require(melee!=null&&melee.shouldExecute(),"Melee starts "+d.id);melee.startExecuting();for(int i=0;i<5;i++)melee.updateTask();require(nav.paths>0&&p.damage==5,"Navigation and five damage "+d.id);
   for(DamageSource s:new DamageSource[]{DamageSource.FALL,DamageSource.DROWN,DamageSource.LIGHTNING_BOLT})require(!robot.attackEntityFrom(s,10),"Source immunity "+d.id);
   if(robot instanceof GroundBadnik.Rhinobot)require(!robot.attackEntityFrom(DamageSource.LAVA,10),"Rhino fireproof");
   robot.setCustomNameTag("test-"+d.id);NBTTagCompound tag=new NBTTagCompound();robot.writeToNBT(tag);GroundBadnik copy=(GroundBadnik)Badniks.entityClass(d.id).getConstructor(net.minecraft.world.World.class).newInstance(w);copy.readFromNBT(tag);require(copy.getCustomNameTag().equals(robot.getCustomNameTag()),"NBT roundtrip "+d.id);
   w.difficulty=EnumDifficulty.PEACEFUL;require(!robot.playersHostile(),"Peaceful no proactive player attack");robot.peacefulTick();require(!robot.isDead,"Peaceful persistence");w.getGameRules().setOrCreateGameRule("peacefulBadniks","false");robot.peacefulTick();require(robot.isDead,"Peaceful removal switch");w.difficulty=EnumDifficulty.NORMAL;w.getGameRules().setOrCreateGameRule("peacefulBadniks","true");
  }
  Set<Class<?>> released=new HashSet<>();boolean foundScrap=false;
  for(int i=0;i<60;i++){World test=new World();GroundBadnik robot=new GroundBadnik.Motobug(test);robot.setPosition(0,64,0);robot.setHealth(0);robot.onDeath(DamageSource.GENERIC);int animals=0,scrap=0;for(Entity e:test.spawnedEntities){if(e instanceof FreedAnimal){animals++;released.add(e.getClass());}if(e instanceof EntityItem){require(((EntityItem)e).getItem().getItem()==Badniks.SCRAP,"Correct scrap");scrap+=((EntityItem)e).getItem().getCount();}}require(robot.isDead&&animals==1&&scrap<=2,"Exactly one freed animal and source loot");foundScrap|=scrap>0;int count=test.spawnedEntities.size();robot.onDeath(DamageSource.GENERIC);robot.releaseContents();require(test.spawnedEntities.size()==count,"No duplicate release");}
  require(released.size()==2&&foundScrap,"Both source animal outcomes and scrap");World noLoot=new World();noLoot.getGameRules().setOrCreateGameRule("doMobLoot","false");GroundBadnik robot=new GroundBadnik.Motobug(noLoot);robot.releaseContents();require(noLoot.spawnedEntities.size()==1&&noLoot.spawnedEntities.get(0) instanceof FreedAnimal,"doMobLoot suppresses scrap but not animal");
  for(String id:new String[]{"flicky","pocky"}){FreedAnimal animal=(FreedAnimal)Badniks.entityClass(id).getConstructor(net.minecraft.world.World.class).newInstance(w);require(animal.getMaxHealth()==8&&!animal.canDespawn()&&animal.tasks.taskEntries.size()==4&&!animal.attackEntityFrom(DamageSource.FALL,10),"Animal behavior "+id);NBTTagCompound tag=new NBTTagCompound();animal.writeToNBT(tag);animal.readFromNBT(tag);}
  require(Badniks.heroTargets("sonic",new GroundBadnik.Motobug(w))&&!Badniks.heroTargets("eggman",new GroundBadnik.Motobug(w))&&!Badniks.heroTargets("blaze_the_cat",new GroundBadnik.Burrobot(w)),"Source hero target lists");
  FlickyModel112 flicky=new FlickyModel112();flicky.setRotationAngles(0,1,0,0,0,.0625f,null);float start=flicky.left_wing.rotateAngleY;flicky.setRotationAngles(.8f,1,0,0,0,.0625f,null);require(Math.abs(flicky.left_wing.rotateAngleY-start)>.1,"Flicky source wing animation");flicky.setRotationAngles(0,0,0,0,0,.0625f,null);require(flicky.left_wing.rotateAngleY==0&&flicky.root_rotate.rotateAngleX==0,"No pose accumulation");new PockyModel112();new RhinobotModel112();
  System.out.println("PASS: 3 badnik source priorities, real melee/path requests, damage/immunities, peaceful rules, NBT and creature caps; 60 death cycles with animals/scrap/no duplication; 2 persistent animals; source hero targets and Flicky wing/reset animation");
 }
}
