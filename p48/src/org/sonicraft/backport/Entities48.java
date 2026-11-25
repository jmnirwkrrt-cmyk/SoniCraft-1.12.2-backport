package org.sonicraft.backport;
import java.util.Set;
import net.minecraft.entity.*;import net.minecraft.world.World;import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.event.RegistryEvent;import net.minecraftforge.fml.common.registry.*;
@Mod.EventBusSubscriber(modid="sonicraft")
public final class Entities48 {
 @SubscribeEvent public static void register(RegistryEvent.Register<EntityEntry> e){
  e.getRegistry().register(EntityEntryBuilder.create().entity(Eggrobo.class).id(new ResourceLocation("sonicraft","eggrobo"),466).name("sonicraft.eggrobo").tracker(96,3,true).egg(0xffcd42,0x777777).build());
  e.getRegistry().register(EntityEntryBuilder.create().entity(BattleFlapper.class).id(new ResourceLocation("sonicraft","battle_flapper"),467).name("sonicraft.battle_flapper").tracker(96,3,true).egg(0x46276b,0xdd3333).build());
  e.getRegistry().register(EntityEntryBuilder.create().entity(Flapper.class).id(new ResourceLocation("sonicraft","flapper"),468).name("sonicraft.flapper").tracker(96,3,true).egg(0xcc3333,0xffffff).build());
 }
 public static Class<? extends EntityCreature> type(String id){switch(id){case "egg_pawn":return EggPawn46.class;case "eggrobo":return Eggrobo.class;case "battle_flapper":return BattleFlapper.class;case "flapper":return Flapper.class;default:return null;}}
 public static boolean safe(Set<?> s,Object v){return "sonicraft:eggrobo".equals(v)||"sonicraft:battle_flapper".equals(v)||"sonicraft:flapper".equals(v)||Vehicles47.safe(s,v);}
 public static class Eggrobo extends EggPawn46 {public Eggrobo(World w){super(w);}Badniks.Definition definition(){return Badniks.definition(Eggrobo.class);}}
 public static class Flapper extends AdditionalBadnik.Flying {
  public Flapper(World w){super(w);}Badniks.Definition definition(){return Badniks.definition(getClass());}
  public void onLivingUpdate(){super.onLivingUpdate();EntityLivingBase t=getAttackTarget();if(!world.isRemote&&t!=null&&t.isEntityAlive()&&getDistanceSq(t)<256&&canEntityBeSeen(t)&&ticksExisted%40==0){EggPawn46.Bullet b=new EggPawn46.Bullet(world,this);double x=t.posX-posX,z=t.posZ-posZ,y=t.posY+t.getEyeHeight()-b.posY;b.shoot(x,y+Math.sqrt(x*x+z*z)*.05,z,1.6f,3);world.spawnEntity(b);}}
  public void registerControllers(software.bernie.geckolib3.core.manager.AnimationData d){d.addAnimationController(new software.bernie.geckolib3.core.controller.AnimationController<Flapper>(this,"movement",4,e->{e.getController().setAnimation(new software.bernie.geckolib3.core.builder.AnimationBuilder().addAnimation("idle",true));return software.bernie.geckolib3.core.PlayState.CONTINUE;}));}
 }
 public static class BattleFlapper extends Flapper {public BattleFlapper(World w){super(w);}}
}
