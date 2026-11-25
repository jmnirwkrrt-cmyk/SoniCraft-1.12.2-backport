package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** For use after Gradle resolves Forge; NOT a substitute for a full client/server game test. */
public final class BatchContent016Check {
 private static void check(boolean pass,String message){if(!pass)throw new AssertionError(message);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block b=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,b);ForgeRegistries.BLOCKS.register(b);
   // New property adapters must round-trip through 4-bit 1.12 metadata.
   if(def.kind.startsWith("facade_"))for(IBlockState state:b.getBlockState().getValidStates()){
    int meta=b.getMetaFromState(state);
    check(meta>=0&&meta<16,"metadata overflow: "+def.id);
    check(state.equals(b.getStateFromMeta(meta)),"state not persistent: "+def.id+" "+state);
   }
  }
  for(Block b:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(b);
  check(Content.BLOCKS.size()>=698,"expected 698 registered main blocks");
  for(String id:new String[]{"jaws_badnik","woodrow","grabber","grounder","coconuts","bumpalo"}){
   check(Badniks.entityClass(id)!=null,"missing mob: "+id);
   check(Arrays.asList(Badniks.IDS).contains(id),"unregistered mob: "+id);
  }
  for(String name:new String[]{"spring_yard_funnel","spring_yard_bumper_pit",
   "aquatic_ruin_big_platform_1","cpz_tower_1","small_chilly_blossom_tree_1_woodrow_1"}){
   StructurePort.Template t=StructurePort.get(name);
   check(t!=null&&t.usable(),"NBT palette cannot be placed: "+name+" "+(t==null?"missing":t.missing));
  }
  check(StructurePort.get("small_chilly_blossom_tree_1_woodrow_1").spawns.size()==1,
   "woodrow not restored from NBT");
  System.out.println("PASS: 698 blocks with 1.12 metadata, 6 new badniks, adapted worldgen templates and Woodrow NBT");
 }
}
