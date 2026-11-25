package org.sonicraft.backport;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.*;
@Mod.EventBusSubscriber(modid=SonicraftLegacy.ID)
public final class Badniks {
 static final Map<String,java.util.List<String>> HERO_TARGETS=Content.read("hero_badnik_targets.json",Map.class);
 static boolean heroTargets(String hero,GroundBadnik target){return HERO_TARGETS.containsKey(hero)&&HERO_TARGETS.get(hero).contains(target.definition().id);}
 static final Definition[] DEFINITIONS=Content.read("badniks.json",Definition[].class);
 static final String[] IDS={"motobug","rhinobot","burrobot","flicky","pocky","crabmeat","caterkiller","spiny","splats","ball_hog","roller","buzz_bomber","batbrain","jaws_badnik","woodrow","grabber","grounder","coconuts","bumpalo","aquis","buzzer","bomb_badnik","crawl_badnik","egg_flapper","flasher","octus","spikes_badnik","rattlekiller","rexon","shutterbug","iwamodoki","newtron","tubinaut","gohla","egg_pawn_bunny"};
 static final Item SCRAP=new Item().setRegistryName("sonicraft","badnik_scrap").setUnlocalizedName("sonicraft.badnik_scrap").setCreativeTab(CreativeCatalog.MISC);
 static final Map<String,SoundEvent> SOUNDS=new LinkedHashMap<>();
 static {for(String id:new String[]{"badnik.hurt","monitor.destroy","animal.tweet"}){ResourceLocation key=new ResourceLocation("sonicraft",id);SOUNDS.put(id,new SoundEvent(key).setRegistryName(key));}}
 static class Spawn {String biomes;int weight,min,max;}
 static class Definition {String id,javaClass,geo;float width,height,step;double speed,health,damage,armor,follow,knockback;Spawn[] spawns;}
 static Definition definition(Class<?> cls){for(Definition d:DEFINITIONS)if(d.javaClass.equals(cls.getSimpleName()))return d;throw new IllegalArgumentException(cls.getName());}
 @SuppressWarnings("unchecked") static Class<? extends EntityCreature> entityClass(String id){switch(id){case "motobug":return GroundBadnik.Motobug.class;case "rhinobot":return GroundBadnik.Rhinobot.class;case "burrobot":return GroundBadnik.Burrobot.class;case "crabmeat":return AdditionalBadnik.Crabmeat.class;case "caterkiller":return AdditionalBadnik.Caterkiller.class;case "spiny":return AdditionalBadnik.Spiny.class;case "splats":return AdditionalBadnik.Splats.class;case "ball_hog":return AdditionalBadnik.BallHog.class;case "roller":return AdditionalBadnik.Roller.class;case "buzz_bomber":return AdditionalBadnik.BuzzBomber.class;case "batbrain":return AdditionalBadnik.Batbrain.class;case "flicky":return FreedAnimal.Flicky.class;case "pocky":return FreedAnimal.Pocky.class;case "jaws_badnik":return AdditionalBadnikV2.JawsBadnik.class;case "woodrow":return AdditionalBadnikV2.Woodrow.class;case "grabber":return AdditionalBadnikV2.Grabber.class;case "grounder":return AdditionalBadnikV2.Grounder.class;case "coconuts":return AdditionalBadnikV2.Coconuts.class;case "bumpalo":return AdditionalBadnikV2.Bumpalo.class;case "aquis":return AdditionalBadnikV3.Aquis.class;case "buzzer":return AdditionalBadnikV3.Buzzer.class;case "bomb_badnik":return AdditionalBadnikV3.BombBadnik.class;case "crawl_badnik":return AdditionalBadnikV3.CrawlBadnik.class;case "egg_flapper":return AdditionalBadnikV3.EggFlapper.class;case "flasher":return AdditionalBadnikV3.Flasher.class;case "octus":return AdditionalBadnikV3.Octus.class;case "spikes_badnik":return AdditionalBadnikV3.SpikesBadnik.class;case "rattlekiller":return AdditionalBadnikV3.Rattlekiller.class;case "rexon":return AdditionalBadnikV3.Rexon.class;case "shutterbug":return AdditionalBadnikV3.Shutterbug.class;case "iwamodoki":return AdditionalBadnikV3.Iwamodoki.class;case "newtron":return AdditionalBadnikV4.Newtron.class;case "tubinaut":return AdditionalBadnikV4.Tubinaut.class;case "gohla":return AdditionalBadnikV4.Gohla.class;case "egg_pawn_bunny":return AdditionalBadnikV4.EggPawnBunny.class;default:throw new IllegalArgumentException(id);}}
 @SubscribeEvent public static void entities(RegistryEvent.Register<EntityEntry> e){int n=100;for(String id:IDS)e.getRegistry().register(EntityEntryBuilder.<EntityCreature>create().entity(entityClass(id)).id(new ResourceLocation("sonicraft",id),n++).name("sonicraft."+id).tracker(64,3,true).egg(id.hashCode()&0xffffff,0xeeeeee).build());}
 @SubscribeEvent public static void items(RegistryEvent.Register<Item> e){e.getRegistry().register(SCRAP);}
 @SubscribeEvent public static void sounds(RegistryEvent.Register<SoundEvent> e){for(Map.Entry<String,SoundEvent> entry:SOUNDS.entrySet()){SoundEvent sound=entry.getValue();if(e.getRegistry().containsKey(sound.getRegistryName()))entry.setValue(e.getRegistry().getValue(sound.getRegistryName()));else e.getRegistry().register(sound);}}
 @SubscribeEvent public static void worldLoad(WorldEvent.Load e){ensureRule(e.getWorld());}
 static void ensureRule(World w){if(!w.getGameRules().hasRule("peacefulBadniks"))w.getGameRules().addGameRule("peacefulBadniks","true",net.minecraft.world.GameRules.ValueType.BOOLEAN_VALUE);}
 static boolean peacefulBadniks(World w){return !w.getGameRules().hasRule("peacefulBadniks")||w.getGameRules().getBoolean("peacefulBadniks");}
 static void addSpawns(){if(!SonicraftLegacy.config.getBoolean("naturalSpawns","badniks",true,"Source ground badnik spawns in available SoniCraft biomes; restart required."))return;for(Definition d:DEFINITIONS)for(Spawn s:d.spawns){String id=s.biomes.replace("sonicraft:","");if(Content.BIOMES.containsKey(id))EntityRegistry.addSpawn(entityClass(d.id),s.weight,s.min,s.max,EnumCreatureType.CREATURE,Content.BIOMES.get(id));}}
}
