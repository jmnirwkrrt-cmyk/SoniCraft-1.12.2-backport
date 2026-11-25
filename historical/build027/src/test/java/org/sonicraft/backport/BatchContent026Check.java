package org.sonicraft.backport;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

/** Forge-dependent regression check. Not a substitute for a world/load test. */
public final class BatchContent026Check {
 public static void main(String[] args) {
  if(Content.BLOCK_DEFS.length!=1314)throw new AssertionError("Expected 1314 block IDs");
  Set<String> ids=new HashSet<>();
  for(Content.BlockDef d:Content.BLOCK_DEFS)if(!ids.add(d.id))throw new AssertionError("Duplicate "+d.id);
  for(String id:new String[]{"weather_globe_tv_screen","casino_night_sonic_sign","red_button",
   "night_carnival_wall_lights","large_cpz_factory_pillar_corner","oil",
   "death_egg_outer_shell_wall","smooth_death_egg_outer_shell_wall",
   "night_carnival_switch_grind_rail","big_arms_spawn_trigger"}){
   if(!ids.contains(id))throw new AssertionError("Missing original block "+id);
  }
  int[][] states={{24,24},{24,24},{24,24},{32,32},{40,40}};
  String[] names={"casino_night_miles_sign","red_button","night_carnival_wall_lights",
   "large_cpz_factory_pillar_corner","weather_globe_tv_screen"};
  for(int i=0;i<names.length;i++){
   Content.BlockDef d=null;
   for(Content.BlockDef b:Content.BLOCK_DEFS)if(b.id.equals(names[i])){d=b;break;}
   Block block=LegacyBlocks.create(d);
   int count=block.getBlockState().getValidStates().size();
   if(count!=states[i][0])throw new AssertionError(names[i]+" lost render variants "+count);
   for(int meta=0;meta<4;meta++){
    IBlockState state=block.getStateFromMeta(meta);
    if(block.getMetaFromState(state)!=meta)throw new AssertionError(names[i]+" orientation metadata "+meta);
   }
   if(!block.hasTileEntity(block.getDefaultState()))throw new AssertionError(names[i]+" must persist extra render state in tile NBT");
  }
  if(Content.read("paletted_026.json",String[].class).length!=361)throw new AssertionError("Source NBT inventory incomplete");
  System.out.println("PASS: 1314 registered block definitions, 361 source template palettes, persistent >16-state adapters");
 }
}
