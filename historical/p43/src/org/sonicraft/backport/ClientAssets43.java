package org.sonicraft.backport;
import net.minecraft.item.Item;import net.minecraft.util.ResourceLocation;import net.minecraft.client.renderer.block.model.ModelResourceLocation;import net.minecraftforge.client.event.ModelRegistryEvent;import net.minecraftforge.client.model.ModelLoader;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.fml.relauncher.Side;
@Mod.EventBusSubscriber(modid="sonicraft",value=Side.CLIENT)
public final class ClientAssets43 {
 @SubscribeEvent public static void models(ModelRegistryEvent e){ModelLoader.setCustomModelResourceLocation(Item.REGISTRY.getObject(new ResourceLocation("sonicraft:red_star_ring")),0,new ModelResourceLocation("sonicraft:red_star_ring","inventory"));}
}
