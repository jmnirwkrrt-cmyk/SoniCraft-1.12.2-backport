package org.sonicraft.backport;
import java.util.Set;import net.minecraft.util.ResourceLocation;import net.minecraft.entity.EnumCreatureType;import net.minecraft.world.biome.Biome;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.event.RegistryEvent;import net.minecraftforge.fml.common.registry.*;import net.minecraftforge.event.world.WorldEvent;
@Mod.EventBusSubscriber(modid="sonicraft")
public class Entities46 {
 @SubscribeEvent public static void register(RegistryEvent.Register<EntityEntry> e){
  e.getRegistry().register(EntityEntryBuilder.create().entity(EggPawn46.class).id(new ResourceLocation("sonicraft","egg_pawn"),463).name("sonicraft.egg_pawn").tracker(80,3,true).egg(0xb91c27,0xe2d4a8).build());
  e.getRegistry().register(EntityEntryBuilder.create().entity(EggPawn46.Bullet.class).id(new ResourceLocation("sonicraft","egg_pawn_bullet"),464).name("sonicraft.egg_pawn_bullet").tracker(80,1,true).build());
 }
 @SubscribeEvent public static void load(WorldEvent.Load e){Biome b=Content.BIOMES.get("test_zone");if(b!=null)EntityRegistry.addSpawn(EggPawn46.class,8,1,1,EnumCreatureType.MONSTER,b);}
 public static boolean safe(Set<?> s,Object v){return "sonicraft:egg_pawn".equals(v)||Entities43.safe(s,v);}
}
