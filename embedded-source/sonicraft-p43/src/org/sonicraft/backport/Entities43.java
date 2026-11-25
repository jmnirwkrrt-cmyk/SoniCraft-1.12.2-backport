package org.sonicraft.backport;
import java.util.Set;import net.minecraft.util.ResourceLocation;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.event.RegistryEvent;import net.minecraftforge.fml.common.registry.*;
@Mod.EventBusSubscriber(modid="sonicraft")
public final class Entities43 {
 @SubscribeEvent public static void register(RegistryEvent.Register<EntityEntry> e){
  e.getRegistry().register(EntityEntryBuilder.create().entity(SceneEggman43.class).id(new ResourceLocation("sonicraft","scrap_brain_eggman"),460).name("sonicraft.scrap_brain_eggman").tracker(96,3,false).build());
  e.getRegistry().register(EntityEntryBuilder.create().entity(CrusherPiston43.class).id(new ResourceLocation("sonicraft","egg_crusher_piston"),461).name("sonicraft.egg_crusher_piston").tracker(96,1,true).build());
 }
 public static boolean safe(Set<?> set,Object value){return set.contains(value)||"sonicraft:scrap_brain_eggman".equals(value);}
}
