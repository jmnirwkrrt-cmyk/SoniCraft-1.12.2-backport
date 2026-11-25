package org.sonicraft.backport;

import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Real Forge-classpath smoke test to run AFTER a successful Gradle compilation.
 * Not executable offline without Minecraft 1.12.2/Forge dependencies. */
public final class BatchContent024Check {
 private static void require(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block b=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,b);ForgeRegistries.BLOCKS.register(b);
  }
  for(Block slab:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(slab);
  require(Content.BLOCKS.size()>=1252,"Lost imported blocks from 0.1.24");
  String[] ids={"blue_sphere","red_sphere","yellow_sphere","bumper_sphere",
    "special_stage_up_block","special_stage_down_block","special_stage_reverse_block",
    "bouncy_checkpoint","bouncy_checkpoint_top","industrial_electric_block",
    "death_egg_electric_block","pink_special_stage_border_block"};
  for(String id:ids){
   Block b=Content.BLOCKS.get(id);require(b!=null,"Missing "+id);
   int expected=id.equals("pink_special_stage_border_block")?5:
    id.equals("bouncy_checkpoint")||id.equals("bouncy_checkpoint_top")?3:
    id.endsWith("electric_block")?4:
    id.equals("blue_sphere")||id.equals("special_stage_reverse_block")||id.equals("special_stage_up_block")||id.equals("special_stage_down_block")?2:1;
   for(int m=0;m<expected;m++)require(b.getMetaFromState(b.getStateFromMeta(m))==m,
    "Corrupt 1.12 metadata: "+id+" meta="+m);
  }
  for(String s:new String[]{"blue_spheres_1","blue_spheres_2","blue_spheres_3",
   "blue_spheres_4","blue_spheres_5","special_stage_dungeon_tunnel_1",
   "special_stage_dungeon_tunnel_2","special_stage_goal_trap_1"}){
   StructurePort.Template t=StructurePort.get(s);
   require(t!=null&&t.usable(),"Source stage missing IDs: "+s);
  }
  System.out.println("PASS SoniCraft 0.1.24 registry, stage metadata, original stage NBT palette");
 }
}
