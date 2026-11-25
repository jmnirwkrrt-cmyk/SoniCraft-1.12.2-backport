package org.sonicraft.backport;
import com.google.gson.Gson;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.block.Block;
public final class Content {
 public static <T>T read(String file,Class<T> type){try(InputStream in=Content.class.getResourceAsStream("/assets/sonicraft/backport/"+file)){if(in==null)throw new IllegalStateException(file);return new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),type);}catch(IOException e){throw new IllegalStateException(e);}}
 public static class BlockDef {public String id,kind,material;public int flammability,fireSpread;public boolean tool,zeroLight;public float hardness,resistance;}
 public static class BiomeDef {public String id,top,filler,climate;public boolean industrial;public float temperature,rain,height,variation;public int grass,foliage,water,sky;}
 public static final BlockDef[] BLOCK_DEFS=read("blocks.json",BlockDef[].class);
 public static final BiomeDef[] BIOME_DEFS=read("biomes.json",BiomeDef[].class);
 public static final Map<String,Block> BLOCKS=new LinkedHashMap<>();
 public static final Map<String,ZoneBiome> BIOMES=new LinkedHashMap<>();
}
