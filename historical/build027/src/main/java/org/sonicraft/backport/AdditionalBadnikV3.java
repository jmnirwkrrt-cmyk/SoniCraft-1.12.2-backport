package org.sonicraft.backport;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
/** Original entity assets; basic 1.12 combat and navigation adapters. */
final class AdditionalBadnikV3 {
 private AdditionalBadnikV3(){}
 private static void register(GroundBadnik e,AnimationData data){data.addAnimationController(new AnimationController<GroundBadnik>(e,"movement",5,event->animate(e,event)));}
 private static <T extends IAnimatable> PlayState animate(GroundBadnik e,AnimationEvent<T> event){
  String[] states=TRACKS.get(e.definition().id);String anim=e.getAttackTarget()!=null&&e.getAttackTarget().isEntityAlive()?states[1]:event.isMoving()?states[2]:states[0];
  event.getController().setAnimation(new AnimationBuilder().addAnimation(anim,true));return PlayState.CONTINUE;
 }
 private static final java.util.Map<String,String[]> TRACKS=new java.util.HashMap<>();
 static {
  TRACKS.put("aquis",new String[]{"idle","attack","idle"});
  TRACKS.put("buzzer",new String[]{"idle","attack","idle"});
  TRACKS.put("bomb_badnik",new String[]{"idle","primed","walk"});
  TRACKS.put("crawl_badnik",new String[]{"idle","attack","walk"});
  TRACKS.put("egg_flapper",new String[]{"idle","idle","idle"});
  TRACKS.put("flasher",new String[]{"idle","attack","idle"});
  TRACKS.put("octus",new String[]{"idle","aggro","walk"});
  TRACKS.put("spikes_badnik",new String[]{"idle","idle","walk"});
  TRACKS.put("rattlekiller",new String[]{"idle","attack","slither"});
  TRACKS.put("rexon",new String[]{"idle","shoot","idle"});
  TRACKS.put("shutterbug",new String[]{"idle","flash","idle"});
  TRACKS.put("iwamodoki",new String[]{"idle","awake","idle"});
 }
 public static final class Aquis extends AdditionalBadnik.Flying {
  public Aquis(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Buzzer extends AdditionalBadnik.Flying {
  public Buzzer(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class BombBadnik extends GroundBadnik {
  public BombBadnik(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class CrawlBadnik extends GroundBadnik {
  public CrawlBadnik(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class EggFlapper extends AdditionalBadnik.Flying {
  public EggFlapper(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Flasher extends AdditionalBadnik.Ranged {
  public Flasher(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Octus extends AdditionalBadnik.Flying {
  public Octus(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class SpikesBadnik extends GroundBadnik {
  public SpikesBadnik(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Rattlekiller extends GroundBadnik {
  public Rattlekiller(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Rexon extends AdditionalBadnik.Ranged {
  public Rexon(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Shutterbug extends AdditionalBadnik.Flying {
  public Shutterbug(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
 public static final class Iwamodoki extends GroundBadnik {
  public Iwamodoki(World w){super(w);}
  @Override public void registerControllers(AnimationData d){register(this,d);}
 }
}
