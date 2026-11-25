package org.sonicraft.backport;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Run with Forge/Gradle: ./gradlew batch027Check. Not an in-world graphics test. */
public final class BatchContent027Check {
 public static void main(String[] args){
  String[] newer={
   "minecraft:azalea_leaves","minecraft:bamboo_wall_sign","minecraft:barrel",
   "minecraft:big_dripleaf","minecraft:big_dripleaf_stem","minecraft:birch_wall_sign",
   "minecraft:brown_bed","minecraft:campfire","minecraft:honey_block",
   "minecraft:lantern","minecraft:sculk_vein","minecraft:smooth_basalt",
   "minecraft:soul_wall_torch","minecraft:spruce_log","minecraft:stone_brick_wall",
   "minecraft:white_bed","minecraft:cobweb"
  };
  for(String original:newer){
   String old=StructurePort.legacyVanillaBlock(original);
   if(old.equals(original))throw new AssertionError("Unmapped modern vanilla block: "+original);
   Block block=ForgeRegistries.BLOCKS.getValue(new ResourceLocation(old));
   if(block==null||block==Blocks.AIR)throw new AssertionError("Unregistered 1.12 fallback: "+original+"->"+old);
  }
  // All templates containing newer vanilla blocks must now have an adapter.
  String[] affected={"big_the_cat_house_green_hill","casino_night_city_building_2",
   "chao_garden","death_egg_magnet","cpz_factory_1","cpz_factory_2",
   "tails_lab","mean_bean_machine","pink_bot_store","star_light_city_center"};
  for(String name:affected){
   StructurePort.Template t=StructurePort.get(name);
   if(t==null)throw new AssertionError("Missing original structure: "+name);
   for(String bad:t.missing.keySet())if(bad.startsWith("minecraft:azalea_leaves")||
    bad.startsWith("minecraft:barrel")||bad.startsWith("minecraft:big_dripleaf")||
    bad.startsWith("minecraft:lantern")||bad.startsWith("minecraft:spruce_log")||
    bad.startsWith("minecraft:campfire")||bad.startsWith("minecraft:stone_brick_wall"))
    throw new AssertionError(name+" still blocked by "+bad);
  }
  if(!"minecraft:web".equals(StructurePort.legacyVanillaBlock("minecraft:cobweb")))
   throw new AssertionError("Old cobweb registry ID");
  System.out.println("PASS: 17 remaps and 10 original affected source templates. Rendering and save tests remain needed.");
 }
}
