package org.sonicraft.backport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.relauncher.Side;
/** Apply the eating hand movement around Carry On's own entity rendering. */
@Mod.EventBusSubscriber(modid="sonicraft",value=Side.CLIENT)
public final class CuddleRender42 {
 private static boolean pushed;
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void before(RenderHandEvent event){
  Minecraft mc=Minecraft.getMinecraft();if(mc.player==null)return;ItemStack s=mc.player.getHeldItemMainhand();
  if(!Companions42.carried(s)||!mc.player.isHandActive()||mc.player.getActiveItemStack()!=s)return;
  float t=Companions42.HUG_TICKS-mc.player.getItemInUseCount()+event.getPartialTicks();if(t<0||t>Companions42.HUG_TICKS)return;
  GlStateManager.pushMatrix();pushed=true;
  float approach=Math.min(1,t/6f)*Math.min(1,(Companions42.HUG_TICKS-t)/5f);
  GlStateManager.translate(.10f*approach,.22f*approach+Math.abs((float)Math.cos(t/4f*Math.PI))*.055f*approach,.25f*approach);
  GlStateManager.rotate(approach*10,0,1,0);GlStateManager.rotate(approach*6,1,0,0);
 }
 @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true) public static void after(RenderHandEvent event){if(pushed){GlStateManager.popMatrix();pushed=false;}}
}
