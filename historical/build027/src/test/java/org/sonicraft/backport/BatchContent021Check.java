package org.sonicraft.backport;

import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** This requires resolved Forge/Gradle and a Java 8 runtime; NOT an offline proof. */
public final class BatchContent021Check {
 private static void ok(boolean yes,String error){if(!yes)throw new AssertionError(error);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block block=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,block);
   ForgeRegistries.BLOCKS.register(block);
  }
  for(Block slab:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(slab);
  ok(Content.BLOCKS.size()>=1202,"Unexpected block registry count");
  ok(SourceComplexes.complexes().size()>=7,"Wrong complex count");
  ok(SourceComplexes.POOLS.size()>=23,"Incomplete original template pools");
  for(String zone:new String[]{"casino","spring","chemical","tailslab","labyrinth","hydrocity","hidden"}){
   int minSections=Integer.MAX_VALUE;
   for(long seed=0;seed<4;seed++){
    MarbleDungeon.Plan plan=SourceComplexes.plan(zone,new BlockPos(0,110,0),seed);
    minSections=Math.min(minSections,plan.pieces.size());
    ok(plan.pieces.size()<=SourceComplexes.MAX_PIECES,"Too many pieces: "+zone);
    int n=0;
    for(int i=0;i<plan.pieces.size();i++){
     MarbleDungeon.Piece p=plan.pieces.get(i);
     n+=p.template.blocks.size();
     for(int j=0;j<i;j++)ok(!p.intersects(plan.pieces.get(j)),"Overlap: "+zone);
    }
    ok(n<=SourceComplexes.MAX_TOTAL_BLOCKS,"Too many blocks: "+zone);
   }
   ok(minSections>=2,"Sections do not connect: "+zone);
  }
  // Source jigsaws with a non-air final_state must never become invisible holes.
  StructurePort.Template road=StructurePort.get("casino_night_city_road_1");
  ok(road!=null&&road.usable(),"Road missing source blocks after final_state mapping");
  ok(!road.blocks.isEmpty(),"Empty road template");
  System.out.println("PASS 0.1.21: seven connected source complexes, bounded sections, baseline structural assertions");
 }
}
