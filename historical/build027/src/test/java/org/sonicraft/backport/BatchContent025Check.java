package org.sonicraft.backport;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;

/** Classpath-dependent smoke check; Gradle needs Forge and GeckoLib dependencies. */
public final class BatchContent025Check {
 public static void main(String[] args) {
  if(Content.BLOCK_DEFS.length<1289)throw new AssertionError("Lost original 0.1.25 block definitions");
  Set<String> ids=new HashSet<>();
  for(Content.BlockDef d:Content.BLOCK_DEFS){
   if(!ids.add(d.id))throw new AssertionError("Duplicate block id: "+d.id);
  }
  for(String id:new String[]{"night_carnival_fence","press_garden_fence", "purple_oil_ocean_iron_bars",
   "yellow_oil_ocean_iron_bars","crisis_city_handrail","oil_ocean_railing",
   "locked_studiopolis_door","press_garden_factory_door_locked","death_egg_arena_door",
   "studiopolis_camera", "press_garden_wood_chipper"}){
    if(!ids.contains(id))throw new AssertionError("Missing definition "+id);
  }
  // Block state serialization round trip for saved metadata. This is the bound
  // that prevents 24- and 40-variant source states from being silently truncated.
  for(Content.BlockDef d:Content.BLOCK_DEFS){
   if(!d.id.equals("studiopolis_camera")&&!d.id.equals("press_garden_wood_chipper"))continue;
   Block b=LegacyBlocks.create(d);int count=d.id.equals("studiopolis_camera")?12:16;
   for(int m=0;m<count;m++){
    IBlockState state=b.getStateFromMeta(m);
    if(b.getMetaFromState(state)!=m)throw new AssertionError(d.id+" metadata " +m);
   }
  }
  for(Content.BlockDef d:Content.BLOCK_DEFS){
   if(!d.kind.equals("bulk_door_locked"))continue;
   Block b=LegacyBlocks.create(d);
   if(!(b instanceof BlockDoor))throw new AssertionError("Not a two-block door: "+d.id);
   for(int m=0;m<16;m++)if(b.getStateFromMeta(m)==null)throw new AssertionError("Invalid door metadata "+m);
  }
  System.out.println("PASS: 0.1.25 block registry, metadata-safe facades, vanilla two-part doors");
 }
}
