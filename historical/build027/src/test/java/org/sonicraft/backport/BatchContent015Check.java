package org.sonicraft.backport;

import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.*;

/** Forge runtime smoke test (runs AFTER dependencies are resolved and Java is compiled). */
public final class BatchContent015Check {
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block block=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,block);ForgeRegistries.BLOCKS.register(block);
  }
  for(Block block:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(block);
  check(Content.BLOCKS.size()>=519,"expected at least the 519 blocks already ported in 0.1.15");
  Set<String> mobIds=new HashSet<>(Arrays.asList(Badniks.IDS));
  for(String id:new String[]{"crabmeat","caterkiller","spiny","splats","ball_hog","roller","buzz_bomber","batbrain"}){
   check(mobIds.contains(id),"missing badnik registry ID "+id);
   check(Badniks.entityClass(id)!=null,"missing badnik class "+id);
  }
  check(StructurePort.CATALOG.size()==361,"original NBT archive should remain intact");
  for(String name:new String[]{"spring_yard_pillar_blue","spring_yard_pillar_purple","spring_yard_searchlight",
   "ice_cap_icicle_floor","aquatic_ruin_platform_1","level_layout_red_spring","ghz_tree_sapling",
   "press_garden_factory_road_corner","seaside_hill_bobsled_track_road_1"}){
   StructurePort.Template template=StructurePort.get(name);
   check(template!=null&&template.usable(),"incomplete palette: "+name+" "+(template==null?"null":template.missing));
  }
  check(StructurePort.get("marble_ruins_1").spawns.size()==2,"marble ruins Batbrain spawning");
  check(StructurePort.get("marble_cave_1_hallway_1").spawns.size()==4,"cave Batbrain spawning");
  check(StructurePort.get("marble_cave_1_hallway_end").spawns.size()==1,"cave end Batbrain spawning");
  System.out.println("PASS: inherited 0.1.15 blocks, 8 additional badniks, 361 source templates, " +
    "new decorative palettes and 7 Batbrain placements");
 }
}
