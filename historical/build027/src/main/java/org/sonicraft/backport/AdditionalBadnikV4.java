package org.sonicraft.backport;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
/** Four more ported robot entities. The source assets are original; AI is deliberately adapted. */
final class AdditionalBadnikV4 {
 private AdditionalBadnikV4(){}
 private static <T extends IAnimatable> PlayState animate(GroundBadnik e,AnimationEvent<T> ev){
  String id=e.definition().id;String anim="idle";
  boolean attack=e.getAttackTarget()!=null&&e.getAttackTarget().isEntityAlive();
  if(id.equals("newtron"))anim=attack?"shoot":ev.isMoving()?"walk":"idle";
  if(id.equals("tubinaut"))anim=attack?"aggro":"idle";
  if(id.equals("egg_pawn_bunny"))anim=ev.isMoving()?"fly":"idle";
  ev.getController().setAnimation(new AnimationBuilder().addAnimation(anim,true));return PlayState.CONTINUE;
 }
 private static void register(GroundBadnik e,AnimationData d){d.addAnimationController(new AnimationController<GroundBadnik>(e,"movement",5,event->animate(e,event)));}
 public static final class Newtron extends AdditionalBadnik.Ranged {public Newtron(World w){super(w);} @Override public void registerControllers(AnimationData d){register(this,d);}}
 public static final class Tubinaut extends AdditionalBadnik.Flying {public Tubinaut(World w){super(w);} @Override public void registerControllers(AnimationData d){register(this,d);}}
 public static final class Gohla extends GroundBadnik {public Gohla(World w){super(w);} @Override public void registerControllers(AnimationData d){register(this,d);}}
 public static final class EggPawnBunny extends AdditionalBadnik.Flying {public EggPawnBunny(World w){super(w);} @Override public void registerControllers(AnimationData d){register(this,d);}}
}
