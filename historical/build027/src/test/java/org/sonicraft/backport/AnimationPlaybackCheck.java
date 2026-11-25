package org.sonicraft.backport;
import java.util.*;
import com.google.gson.*;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.*;
import software.bernie.geckolib3.core.builder.*;
import software.bernie.geckolib3.core.controller.*;
import software.bernie.geckolib3.core.event.predicate.*;
import software.bernie.geckolib3.core.manager.*;
import software.bernie.geckolib3.core.processor.*;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.geo.render.built.*;
import software.bernie.geckolib3.util.json.JsonAnimationUtils;
import software.bernie.geckolib3.molang.MolangRegistrar;
import software.bernie.shadowed.eliotlash.molang.MolangParser;
/** Executes GeckoLib 3's real controller and interpolator, without OpenGL. */
public final class AnimationPlaybackCheck {
 static class Actor implements IAnimatable {
  final AnimationFactory factory=new AnimationFactory(this); final Model model; String clip;
  Actor(Model m,String c){model=m;clip=c;}
  public AnimationFactory getFactory(){return factory;}
  public void registerControllers(AnimationData d){d.addAnimationController(new AnimationController<Actor>(this,"test",3,e->{e.getController().setAnimation(new AnimationBuilder().addAnimation(clip,true));return PlayState.CONTINUE;}));}
 }
 static class Model implements IAnimatableModel<Actor> {
  final AnimationProcessor<Actor> processor=new AnimationProcessor<>(this); final Map<String,Animation> animations=new LinkedHashMap<>();
  public AnimationProcessor getAnimationProcessor(){return processor;}
  public Animation getAnimation(String name,IAnimatable a){return animations.get(name);}
  public void setLivingAnimations(Actor a,Integer id,AnimationEvent e){}
  public void setMolangQueries(IAnimatable a,double t){}
  void bone(GeoBone b){processor.registerModelRenderer(b);for(GeoBone child:b.childBones)bone(child);}
 }
 public static void main(String[] args)throws Exception {
  net.minecraft.init.Bootstrap.register();
  MolangParser parser=new MolangParser();MolangRegistrar.registerVars(parser);
  AnimationController.addModelFetcher((AnimationController.ModelFetcher<Actor>)(a)->a instanceof Actor?((Actor)a).model:null);
  int count=0;
  List<String> geos=new ArrayList<>();for(Characters.Definition d:Characters.DEFINITIONS)if(d.geo!=null)geos.add(d.geo);for(Badniks.Definition d:Badniks.DEFINITIONS)if(d.geo!=null)geos.add(d.geo);
  for(String geoId:geos){
   Model model=new Model();GeoModel geo=new GeoBuilder().constructGeoModel(RawGeometryTree.parseHierarchy(Converter.fromJsonString(CharacterSmokeCheck.resource("geo/"+geoId+".geo.json")),new ResourceLocation("sonicraft",geoId)));
   for(GeoBone b:geo.topLevelBones)model.bone(b);
   JsonObject json=new JsonParser().parse(CharacterSmokeCheck.resource("animations/"+geoId+".animation.json")).getAsJsonObject();
   for(Map.Entry<String,JsonElement> entry:JsonAnimationUtils.getAnimations(json))model.animations.put(entry.getKey(),JsonAnimationUtils.deserializeJsonToAnimation(entry,parser));
   for(String clip:model.animations.keySet()){
    Actor actor=new Actor(model,clip);
    try{for(int t=0;t<160;t++){AnimationEvent<Actor> event=new AnimationEvent<>(actor,0,0,.25f,false,Collections.emptyList());model.processor.tickAnimation(actor,System.identityHashCode(actor),t*.25,event,parser,false);
     for(IBone b:model.processor.getModelRendererList())if(!Float.isFinite(b.getRotationX())||!Float.isFinite(b.getRotationY())||!Float.isFinite(b.getRotationZ())||!Float.isFinite(b.getScaleX()))throw new AssertionError("Nonfinite bone "+b.getName());
    }}catch(Throwable e){throw new AssertionError(geoId+" / "+clip,e);}count++;
   }
  }
  System.out.println("PASS: "+count+" clips executed through GeckoLib controller and interpolation for 160 frames each");
 }
}
