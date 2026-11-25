package org.sonicraft.backport;
import java.util.*;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.*;
@Mod.EventBusSubscriber(modid=SonicraftLegacy.ID)
public final class Characters {
 public static final Definition[] DEFINITIONS=Content.read("characters.json",Definition[].class);
 public static final Map<String,SoundEvent> SOUNDS=new LinkedHashMap<>();
 public static class Definition {
  public String id,javaClass,geo,texture;public String[] clips;public float width,height;public double health,speed,damage,armor;public boolean enemy;public Map<String,String> sounds;
  public boolean hasClip(String s){return Arrays.asList(clips).contains(s);}
 }
 public static Definition forClass(Class<?> cls){for(Definition d:DEFINITIONS)if(d.javaClass.equals(cls.getSimpleName()))return d;throw new IllegalArgumentException(cls.getName());}
 @SuppressWarnings("unchecked") public static Class<? extends LegacyCharacter> entityClass(Definition d){try{return (Class<? extends LegacyCharacter>)Class.forName(LegacyCharacter.class.getName()+"$"+d.javaClass);}catch(ClassNotFoundException ex){throw new IllegalStateException(ex);}}
 @SubscribeEvent public static void registerEntities(RegistryEvent.Register<EntityEntry> e){int id=0;for(Definition d:DEFINITIONS)e.getRegistry().register(EntityEntryBuilder.<LegacyCharacter>create().entity(entityClass(d)).id(new ResourceLocation(SonicraftLegacy.ID,d.id),id++).name(SonicraftLegacy.ID+"."+d.id).tracker(80,3,true).egg(d.id.hashCode()&0xffffff,0xeeeeee).build());}
 @SubscribeEvent public static void registerSounds(RegistryEvent.Register<SoundEvent> e){for(Definition d:DEFINITIONS)for(String id:d.sounds.values())if(!SOUNDS.containsKey(id)){ResourceLocation key=new ResourceLocation(SonicraftLegacy.ID,id);SoundEvent sound=e.getRegistry().containsKey(key)?e.getRegistry().getValue(key):new SoundEvent(key).setRegistryName(key);SOUNDS.put(id,sound);if(!e.getRegistry().containsKey(key))e.getRegistry().register(sound);}}
 public static void addSpawns(){
  if(!SonicraftLegacy.config.getBoolean("naturalSpawns","characters",true,"Rare heroes in SoniCraft biomes; enemies and Eggman via eggs/summon only."))return;
  Map<String,String[]> zones=new HashMap<>();
  zones.put("sonic",new String[]{"green_hill_zone","emerald_hill_zone"});zones.put("tails",new String[]{"green_hill_zone","seaside_hill_zone"});zones.put("knuckles",new String[]{"angel_island_jungle"});zones.put("amy_rose",new String[]{"emerald_hill_zone"});zones.put("rouge",new String[]{"marble_zone"});zones.put("shadow",new String[]{"hill_top_zone"});zones.put("silver",new String[]{"press_garden_zone"});zones.put("big_the_cat",new String[]{"mushroom_hill_zone"});zones.put("espio",new String[]{"aquatic_ruin_zone"});zones.put("charmy",new String[]{"emerald_hill_zone"});zones.put("vector",new String[]{"aquatic_ruin_zone"});zones.put("omega",new String[]{"hill_top_zone"});zones.put("blaze_the_cat",new String[]{"mirage_saloon_zone"});zones.put("cream",new String[]{"green_hill_zone"});
  for(Definition d:DEFINITIONS)if(zones.containsKey(d.id)){int weight=SonicraftLegacy.config.getInt(d.id,"characterWeights",1,0,20,"Spawn weight; 0 disables. Restart required.");if(weight>0)for(String id:zones.get(d.id))EntityRegistry.addSpawn(entityClass(d),weight,1,1,EnumCreatureType.CREATURE,Content.BIOMES.get(id));}
 }
}
