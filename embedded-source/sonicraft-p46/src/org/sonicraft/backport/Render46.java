package org.sonicraft.backport;
import net.minecraft.client.renderer.entity.*;import net.minecraft.client.Minecraft;import net.minecraft.util.ResourceLocation;import net.minecraftforge.fml.client.registry.RenderingRegistry;
public class Render46 {
 public static void register(){Render43.register();RenderingRegistry.registerEntityRenderingHandler(EggPawn46.class,m->new RenderLiving<EggPawn46>(m,new PawnModel46(),.5f){protected ResourceLocation getEntityTexture(EggPawn46 e){return new ResourceLocation("sonicraft","textures/entities/egg_pawn.png");}});RenderingRegistry.registerEntityRenderingHandler(EggPawn46.Bullet.class,m->new RenderSnowball<EggPawn46.Bullet>(m,net.minecraft.init.Items.IRON_NUGGET,Minecraft.getMinecraft().getRenderItem()));}
}
