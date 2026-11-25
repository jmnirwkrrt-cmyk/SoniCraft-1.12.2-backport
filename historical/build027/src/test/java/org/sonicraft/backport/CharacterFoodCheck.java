package org.sonicraft.backport;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.pathfinding.*;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
/** Checks scheduling and path commands, including a closer empty-handed player. */
public final class CharacterFoodCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static class Navigator extends PathNavigateGround {
  int moves,clears;EntityPlayer target;
  Navigator(LegacyCharacter e){super(e,e.world);}
  @Override public boolean tryMoveToEntityLiving(net.minecraft.entity.Entity e,double speed){moves++;target=(EntityPlayer)e;return true;}
  @Override public void clearPath(){clears++;}
 }
 static ItemStack treat(String id){return new ItemStack(id.equals("rouge")?Items.DIAMOND:FoodContent.favorite(id),1,id.equals("shadow")?3:0);}
 public static void main(String[] args)throws Exception {
  Bootstrap.register();FoodContentCheck.TestWorld w=new FoodContentCheck.TestWorld();
  FoodContentCheck.Player p=new FoodContentCheck.Player(w),closer=new FoodContentCheck.Player(w);w.playerEntities.add(closer);w.playerEntities.add(p);closer.setPosition(1,64,0);p.setPosition(7,64,0);
  for(String id:new String[]{"sonic","tails","knuckles","amy_rose","shadow","silver","vector","rouge"}){
   Characters.Definition def=null;for(Characters.Definition d:Characters.DEFINITIONS)if(d.id.equals(id))def=d;
   LegacyCharacter hero=Characters.entityClass(def).getConstructor(World.class).newInstance(w);hero.setPosition(0,64,0);hero.onGround=true;
   // The production navigator is replaced only at the pathfinding boundary; the real scheduler runs.
   Navigator nav=new Navigator(hero);java.lang.reflect.Field field=net.minecraft.entity.EntityLiving.class.getDeclaredField("navigator");field.setAccessible(true);field.set(hero,nav);
   CharacterFoodAI ai=null;for(EntityAITasks.EntityAITaskEntry e:hero.tasks.taskEntries)if(e.action instanceof CharacterFoodAI)ai=(CharacterFoodAI)e.action;
   require(ai!=null,"Missing AI "+id);
   for(boolean creative:new boolean[]{false,true})for(EnumHand hand:EnumHand.values()){
    p.capabilities.isCreativeMode=creative;p.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);p.setHeldItem(EnumHand.OFF_HAND,ItemStack.EMPTY);p.setHeldItem(hand,treat(id));
    require(ai.shouldExecute(),"Starts "+id+creative+hand);int before=nav.moves;
    // Isolate movement goals so TestWorld need not emulate chunk-based wandering.
    EntityAITasks scheduler=new EntityAITasks(w.profiler);scheduler.addTask(3,ai);for(int tick=0;tick<5;tick++)scheduler.onUpdateTasks();
    require(nav.moves>before&&nav.target==p,"Scheduled path request "+id);
    p.setHeldItem(hand,ItemStack.EMPTY);require(!ai.shouldContinueExecuting(),"Stops without food "+id);scheduler.onUpdateTasks();
   }
   p.setHeldItem(EnumHand.MAIN_HAND,treat(id));p.spectator=true;require(!ai.shouldExecute(),"Ignore spectator");p.spectator=false;
   p.setPosition(11,64,0);require(!ai.shouldExecute(),"Range");p.setPosition(7,64,0);
   if(id.equals("shadow")){p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(Items.DYE,1,0));require(!ai.shouldExecute(),"Ink is not cocoa");}
  }
  require(CharacterRenderers.tailSpin(true,5,.25f)!=CharacterRenderers.tailSpin(true,5,.5f),"Tail moves between frames");require(CharacterRenderers.tailSpin(false,9,.5f)==0,"Tail reset on ground");
  System.out.println("PASS: 8 character attraction goals; real scheduler requests paths; survival/creative/both hands/closer empty-handed player/range/spectator/cocoa metadata; interpolated tail spin and reset");
 }
}
