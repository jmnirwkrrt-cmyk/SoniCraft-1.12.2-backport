package org.sonicraft.backport;
import java.util.*;import net.minecraft.entity.*;import net.minecraft.world.*;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.core.IAnimatable;import software.bernie.geckolib3.core.PlayState;import software.bernie.geckolib3.core.builder.AnimationBuilder;import software.bernie.geckolib3.core.controller.AnimationController;import software.bernie.geckolib3.core.event.predicate.AnimationEvent;import software.bernie.geckolib3.core.manager.AnimationData;
/** One persistent encounter controller, with four separately tracked shaft pistons. */
public final class EggCrusherBoss extends GroundBadnik {
 public EggCrusherBoss(World w){super(w);enablePersistence();setSize(.2f,.2f);setNoAI(true);setNoGravity(true);setInvisible(true);experienceValue=40;}
 protected void initEntityAI(){}protected boolean canDespawn(){return false;}
 public void onLivingUpdate(){super.onLivingUpdate();if(world.isRemote||!isEntityAlive())return;
  if(!getEntityData().hasKey("sc34ArenaY"))P34.markArena(this);P34.holdArena(this);setNoGravity(true);setInvisible(true);
  if(ticksExisted%40!=0||!world.isAreaLoaded(getPosition().add(-5,0,-5),getPosition().add(5,8,5)))return;
  List<CrusherPiston43> all=world.getEntitiesWithinAABB(CrusherPiston43.class,getEntityBoundingBox().grow(9));
  for(int i=0;i<4;i++){CrusherPiston43 found=null;for(CrusherPiston43 part:all)if(getUniqueID().equals(part.parentId)&&part.index==i){if(found==null)found=part;else part.setDead();}if(found!=null)continue;
   double x=posX+((i&1)==0?-2:2),z=posZ+((i&2)==0?-2:2),y=posY;
   // Pin each piston under its actual shaft, when this is a generated arena.
   for(int dy=0;dy<=8;dy++){BlockPos shaft=new BlockPos(x,posY+dy,z);if(world.getBlockState(shaft).getBlock()==Content.BLOCKS.get("egg_crusher_shaft_frame")){y=shaft.getY()-4;break;}}
   CrusherPiston43 part=new CrusherPiston43(world);part.bind(this,i,x,y,z);world.spawnEntity(part);
  }
 }
 public boolean canBeCollidedWith(){return false;}
 void releaseContents(){if(!world.isRemote&&world.getGameRules().getBoolean("doMobLoot")){entityDropItem(new ItemStack(Badniks.SCRAP,6),.2f);entityDropItem(new ItemStack(EmeraldProgression.KEY),.2f);}}
 private <T extends IAnimatable> PlayState idle(AnimationEvent<T> e){e.getController().setAnimation(new AnimationBuilder().addAnimation("idle",true));return PlayState.CONTINUE;}
 public void registerControllers(AnimationData d){d.addAnimationController(new AnimationController<EggCrusherBoss>(this,"boss_idle",5,this::idle));}
}
