package org.sonicraft.backport;
import net.minecraft.client.renderer.entity.*;import net.minecraft.util.ResourceLocation;import net.minecraftforge.fml.client.registry.RenderingRegistry;
public final class Render48 {
 public static void register(){Render47.register();
  RenderingRegistry.registerEntityRenderingHandler(Entities48.Eggrobo.class,m->new RenderLiving<Entities48.Eggrobo>(m,new EggroboModel48(),.5f){protected ResourceLocation getEntityTexture(Entities48.Eggrobo e){return new ResourceLocation("sonicraft","textures/entities/eggrobo.png");}});
  RenderingRegistry.registerEntityRenderingHandler(Entities48.Flapper.class,m->new BadnikRenderers.Geo(m));
  RenderingRegistry.registerEntityRenderingHandler(Entities48.BattleFlapper.class,m->new BadnikRenderers.Geo(m));
 }
}
