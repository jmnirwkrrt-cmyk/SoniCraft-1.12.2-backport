package org.sonicraft.backport;
import java.util.*;
import net.minecraft.util.*;
/** Sound IDs are extracted from the original biome effects.ambient_sound fields. */
public final class BiomeMusic {
 public static final Map<String,String> TRACKS=Content.read("biome_music.json",Map.class);
 public static final Map<String,SoundEvent> SOUNDS=new LinkedHashMap<>();
 static{for(String name:TRACKS.values())SOUNDS.put(name,new SoundEvent(new ResourceLocation("sonicraft",name)).setRegistryName("sonicraft",name));}
 static String track(String biome){return TRACKS.get(biome);}
}
