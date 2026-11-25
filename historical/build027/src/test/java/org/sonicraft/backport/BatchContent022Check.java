package org.sonicraft.backport;

import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Forge runtime test only. Must not be represented as executed before Gradle succeeds. */
public final class BatchContent022Check {
 private static void ok(boolean condition,String message){if(!condition)throw new AssertionError(message);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block b=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,b);
   ForgeRegistries.BLOCKS.register(b);
  }
  for(Block slab:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(slab);
  ok(Content.BLOCKS.size()>=1210,"Registry lost 0.1.22 blocks");
  ok(SourceComplexes.complexes().size()==13,"Missing industrial city root");
  ok(SourceComplexes.POOLS.size()==48,"Original source pool data incomplete");
  ok(Content.BLOCKS.get("press_garden_conveyor_belt") instanceof StructureFacadeBlocks.Conveyor,"Conveyor is decorative instead of moving entities");
  StructurePort.Template factory=StructurePort.get("scrap_brain_factory");
  ok(factory!=null,"Missing Scrap Brain source factory");
  ok(factory.deferred.containsKey("DISABLED_BOSS_TRIGGER:egg_crusher"),"Disabled Egg Crusher trigger is not reported");
  for(String id:new String[]{"scrap","press","night","studiopolis","starlight","oilocean"}){
   ok(SourceComplexes.complexes().contains(id),"Missing city "+id);
   ok(SourceComplexes.audit(id).contains("root="),"Broken audit for "+id);
  }
  for(String city:new String[]{"casino","spring","chemical","tailslab","labyrinth","hydrocity","hidden"}){
   MarbleDungeon.Plan p=SourceComplexes.plan(city,new BlockPos(0,100,0),72);
   ok(p.pieces.size()>=2,"Disconnected existing complex "+city);
  }
  if(factory.usable()){
   MarbleDungeon.Plan scrap=SourceComplexes.plan("scrap",new BlockPos(0,120,0),100);
   ok(!scrap.pieces.isEmpty(),"Missing Scrap Brain root piece");
  }
  System.out.println("PASS 0.1.22 source pool audit, seven adapted blocks and conveyor logic registration");
 }
}
