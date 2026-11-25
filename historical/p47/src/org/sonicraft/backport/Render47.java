package org.sonicraft.backport;
import net.minecraft.client.Minecraft;import net.minecraft.client.settings.KeyBinding;import net.minecraft.client.renderer.entity.*;import net.minecraft.client.renderer.block.model.ModelResourceLocation;import net.minecraft.util.ResourceLocation;import net.minecraftforge.client.model.ModelLoader;import net.minecraftforge.client.event.ModelRegistryEvent;import net.minecraftforge.fml.client.registry.*;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.fml.common.gameevent.TickEvent;import net.minecraftforge.fml.relauncher.Side;import org.lwjgl.input.Keyboard;
import software.bernie.geckolib3.model.AnimatedGeoModel;import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;import software.bernie.geckolib3.geo.render.built.GeoModel;
@Mod.EventBusSubscriber(modid="sonicraft",value=Side.CLIENT)
public class Render47 {
 private static final KeyBinding DOWN=new KeyBinding("key.sonicraft.flight_down",Keyboard.KEY_X,"key.categories.movement");
 public static void register(){Render46.register();ClientRegistry.registerKeyBinding(DOWN);RenderingRegistry.registerEntityRenderingHandler(Tornado47.class,m->new GeoEntityRenderer<Tornado47>(m,new PlaneModel()){});}
 @SubscribeEvent public static void model(ModelRegistryEvent e){ModelLoader.setCustomModelResourceLocation(Vehicles47.PLANE,0,new ModelResourceLocation("sonicraft:tornado_item","inventory"));}
 @SubscribeEvent public static void input(TickEvent.ClientTickEvent e){Minecraft mc=Minecraft.getMinecraft();if(e.phase!=TickEvent.Phase.END||mc.player==null||!(mc.player.getRidingEntity() instanceof Tornado47)||mc.isGamePaused())return;float f=0,v=0;if(mc.currentScreen==null){f=(mc.gameSettings.keyBindForward.isKeyDown()?1:0)-(mc.gameSettings.keyBindBack.isKeyDown()?1:0);v=(mc.gameSettings.keyBindJump.isKeyDown()?1:0)-(DOWN.isKeyDown()?1:0);}Vehicles47.NET.sendToServer(new Vehicles47.Control(mc.player.getRidingEntity().getEntityId(),f,v));}
 static class PlaneModel extends AnimatedGeoModel<Tornado47>{GeoModel prepared;
  public GeoModel getModel(ResourceLocation r){GeoModel m=super.getModel(r);if(m!=prepared){GeoCompatibility.prepare(m);prepared=m;}return m;}
  public ResourceLocation getModelLocation(Tornado47 e){return new ResourceLocation("sonicraft","geo/tornado_plane.geo.json");}public ResourceLocation getTextureLocation(Tornado47 e){return new ResourceLocation("sonicraft","textures/entities/tornado.png");}public ResourceLocation getAnimationFileLocation(Tornado47 e){return new ResourceLocation("sonicraft","animations/tornado_plane.animation.json");}
 }
}
