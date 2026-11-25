package org.sonicraft.backport;
import net.minecraft.client.renderer.entity.*;import net.minecraft.util.ResourceLocation;import net.minecraftforge.fml.client.registry.RenderingRegistry;import software.bernie.geckolib3.model.AnimatedGeoModel;import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;import software.bernie.geckolib3.geo.render.built.GeoModel;
public final class Render43 {
 public static void register(){
  RenderingRegistry.registerEntityRenderingHandler(SceneEggman43.class,m->new CharacterRenderers.VanillaRenderer(m,new EggmanModel112()));
  RenderingRegistry.registerEntityRenderingHandler(CrusherPiston43.class,m->new GeoEntityRenderer<CrusherPiston43>(m,new Model()){});
  RenderingRegistry.registerEntityRenderingHandler(EggCrusherBoss.class,m->new Render<EggCrusherBoss>(m){protected ResourceLocation getEntityTexture(EggCrusherBoss e){return null;}public void doRender(EggCrusherBoss e,double x,double y,double z,float yaw,float pt){}});
 }
 static class Model extends AnimatedGeoModel<CrusherPiston43>{GeoModel prepared;
  public GeoModel getModel(ResourceLocation r){GeoModel g=super.getModel(r);if(g!=prepared){GeoCompatibility.prepare(g);prepared=g;}return g;}
  public ResourceLocation getModelLocation(CrusherPiston43 e){return new ResourceLocation("sonicraft","geo/egg_crusher.geo.json");}
  public ResourceLocation getTextureLocation(CrusherPiston43 e){return new ResourceLocation("sonicraft","textures/entities/egg_crusher.png");}
  public ResourceLocation getAnimationFileLocation(CrusherPiston43 e){return new ResourceLocation("sonicraft","animations/egg_crusher.animation.json");}
 }
}
