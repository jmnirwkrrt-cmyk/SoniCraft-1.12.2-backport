package org.sonicraft.backport;

import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** To be run with the 1.12.2 Forge runtime; not a substitute for a world-play test. */
public final class BatchContent023Check {
 private static void ok(boolean value,String what){if(!value)throw new AssertionError(what);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block block=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,block);
   ForgeRegistries.BLOCKS.register(block);
  }
  for(Block slab:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(slab);
  ok(Content.BLOCKS.size()>=1217,"Incomplete SoniCraft block registry");
  for(String id:new String[]{"ice_cap_trampoline","bumper_sonic_3","studiopolis_spring_catapult","cpz_factory_conveyor_belt","dash_ring","sandopolis_quicksand","launch_base_blockade_trapdoor"}){
   Block b=Content.BLOCKS.get(id);
   ok(b!=null,"Missing motion block "+id);
   for(int meta=0;meta<(id.equals("cpz_factory_conveyor_belt")?8:id.equals("dash_ring")?6:id.equals("launch_base_blockade_trapdoor")?16:id.equals("ice_cap_trampoline")||id.equals("sandopolis_quicksand")?1:2);meta++){
    ok(b.getMetaFromState(b.getStateFromMeta(meta))==meta,"Metadata mismatch "+id+" meta="+meta);
   }
  }
  StructurePort.Template ice=StructurePort.get("ice_cap_pit");
  StructurePort.Template carnival=StructurePort.get("carnival_night_building_1");
  ok(ice!=null&&ice.usable(),"Ice Cap pit missing original blocks");
  ok(carnival!=null&&carnival.usable(),"Carnival building missing original blocks");
  for(String structure:new String[]{"crisis_city_skyscraper_1","sandopolis_tower","sandopolis_playground_1"}){
   ok(StructurePort.get(structure).usable(),"Missing motion block in "+structure);
  }
  BlockPos pos=new BlockPos(120,70,200);
  for(Rotation rotation:Rotation.values()){
   int rw=StructurePort.rotatedWidth(ice,rotation),rd=StructurePort.rotatedDepth(ice,rotation);
   for(StructurePort.Cell c:ice.blocks){
    BlockPos p=StructurePort.at(ice,pos,c,rotation);
    ok(p.getX()>=pos.getX()&&p.getX()<pos.getX()+rw,"Rotated template x out of bounds");
    ok(p.getZ()>=pos.getZ()&&p.getZ()<pos.getZ()+rd,"Rotated template z out of bounds");
   }
  }
  System.out.println("PASS 0.1.23 motion blocks, metadata and structure rotation bounds");
 }
}
