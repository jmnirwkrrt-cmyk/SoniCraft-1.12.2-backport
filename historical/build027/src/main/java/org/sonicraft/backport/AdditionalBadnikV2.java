package org.sonicraft.backport;

import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/** Further six source-backed badniks. Source textures/geometry/animation names are retained;
 * 1.12 combat, flight and ranged attacks remain simplified adaptations. */
final class AdditionalBadnikV2 {
 private AdditionalBadnikV2(){}
 private static void register(GroundBadnik e,AnimationData data){
  data.addAnimationController(new AnimationController<GroundBadnik>(e,"movement",5,event->animate(e,event)));
 }
 private static <T extends IAnimatable> PlayState animate(GroundBadnik e,AnimationEvent<T> event){
  boolean target=e.getAttackTarget()!=null&&e.getAttackTarget().isEntityAlive();
  String id=e.definition().id;
  String a;
  switch(id){
   case "jaws_badnik":a=target?"aggro":"idle";break;
   case "woodrow":a=target?"peck":"idle";break;
   case "grabber":a=target?"attack":"idle";break;
   case "grounder":a="idle";break;
   case "coconuts":a=target?"throw":event.isMoving()?"walk":"idle";break;
   case "bumpalo":a=target?"aggro":event.isMoving()?"walk":"idle";break;
   default:a="idle";
  }
  event.getController().setAnimation(new AnimationBuilder().addAnimation(a,true));return PlayState.CONTINUE;
 }
 public static final class JawsBadnik extends AdditionalBadnik.Flying {
  public JawsBadnik(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Woodrow extends AdditionalBadnik.Ranged {
  public Woodrow(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Grabber extends AdditionalBadnik.Flying {
  public Grabber(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Grounder extends GroundBadnik {
  public Grounder(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Coconuts extends AdditionalBadnik.Ranged {
  public Coconuts(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Bumpalo extends GroundBadnik {
  public Bumpalo(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
}
