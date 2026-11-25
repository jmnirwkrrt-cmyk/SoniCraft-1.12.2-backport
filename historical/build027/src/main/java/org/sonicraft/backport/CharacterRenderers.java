package org.sonicraft.backport;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
public final class CharacterRenderers {
 public static void register(){for(Characters.Definition d:Characters.DEFINITIONS){if(d.geo!=null)RenderingRegistry.registerEntityRenderingHandler(Characters.entityClass(d),manager->new GeoRenderer(manager));else RenderingRegistry.registerEntityRenderingHandler(Characters.entityClass(d),manager->new VanillaRenderer(manager,d.id.equals("cream")?new CreamModel112():d.id.equals("eggman")?new EggmanModel112():new BlazeModel112()));}}
 static ResourceLocation texture(LegacyCharacter e){return new ResourceLocation("sonicraft","textures/entities/"+e.definition().texture+".png");}
 static void look(software.bernie.geckolib3.core.processor.IBone head,float pitch,float yaw){head.setRotationX(pitch*((float)Math.PI/180));head.setRotationY(yaw*((float)Math.PI/180));}
 static float tailSpin(boolean flying,float age,float partial){return flying?(float)((age+partial)%(Math.PI*2)):0;}
 static class Model extends AnimatedGeoModel<LegacyCharacter>{
  private software.bernie.geckolib3.geo.render.built.GeoModel prepared;
  @Override public software.bernie.geckolib3.geo.render.built.GeoModel getModel(ResourceLocation location){
   software.bernie.geckolib3.geo.render.built.GeoModel model=super.getModel(location);
   if(model!=prepared){GeoCompatibility.prepare(model);prepared=model;}return model;
  }
  @Override public void setLivingAnimations(LegacyCharacter e,Integer uniqueID,software.bernie.geckolib3.core.event.predicate.AnimationEvent event){
   super.setLivingAnimations(e,uniqueID,event);
   if(event!=null){
    java.util.List<software.bernie.geckolib3.model.provider.data.EntityModelData> data=event.getExtraDataOfType(software.bernie.geckolib3.model.provider.data.EntityModelData.class);
    software.bernie.geckolib3.core.processor.IBone head=getAnimationProcessor().getBone("head");
    if(head!=null&&!data.isEmpty()){look(head,data.get(0).headPitch,data.get(0).netHeadYaw);}
   }
   if(e instanceof LegacyCharacter.Tails){
    software.bernie.geckolib3.core.processor.IBone tail=getAnimationProcessor().getBone("tailroot");
    if(tail!=null)tail.setRotationZ(tailSpin(e.controlledFlight(),e.ticksExisted,event==null?0:event.getPartialTick()));
   }
  }
  @Override public ResourceLocation getModelLocation(LegacyCharacter e){return new ResourceLocation("sonicraft","geo/"+e.definition().geo+".geo.json");}
  @Override public ResourceLocation getTextureLocation(LegacyCharacter e){return texture(e);}
  @Override public ResourceLocation getAnimationFileLocation(LegacyCharacter e){return new ResourceLocation("sonicraft","animations/"+e.definition().geo+".animation.json");}
 }
 static class GeoRenderer extends GeoEntityRenderer<LegacyCharacter>{GeoRenderer(RenderManager m){super(m,new Model());shadowSize=.45f;}}
 static class VanillaRenderer extends RenderLiving<LegacyCharacter>{VanillaRenderer(RenderManager m,ModelBase model){super(m,model,.45f);}@Override protected ResourceLocation getEntityTexture(LegacyCharacter e){return texture(e);}}
}
