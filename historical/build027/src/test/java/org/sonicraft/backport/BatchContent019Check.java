package org.sonicraft.backport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
/** Compile/run with the Forge 1.12.2 test classpath, not with a plain JDK. */
public final class BatchContent019Check {
 private static void check(boolean condition,String description){if(!condition)throw new AssertionError(description);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block block=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,block);ForgeRegistries.BLOCKS.register(block);
   if(def.kind.startsWith("facade_"))for(IBlockState state:block.getBlockState().getValidStates()){
    int metadata=block.getMetaFromState(state);
    check(metadata>=0&&metadata<16,"Invalid metadata: "+def.id+" "+state);
    check(block.getStateFromMeta(metadata).equals(state),"Metadata state not bijective: "+def.id+" "+state);
   }
  }
  for(Block block:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(block);
  check(Content.BLOCKS.size()>=1185,"Expected 1185 blocks");
  for(String id:new String[]{"chilly_blossom_pressure_plate","death_egg_floor_pressure_plate","neo_palm_pressure_plate","studiopolis_interior_floor_pressure_plate"})
   check(Content.BLOCKS.get(id) instanceof BlockPressurePlate,"Nonfunctional pressure plate: "+id);
  for(String name:new String[]{"big_the_cat_house_green_hill","green_hill_mountain","ehz_loop","seaside_hill_orca_island"}){
   StructurePort.Template template=StructurePort.get(name);
   check(template!=null && template.usable(),"Unusable NBT structure: "+name+" "+(template==null?"absent":template.missing));
  }
  for(String id:new String[]{"newtron","tubinaut","gohla","egg_pawn_bunny"}){
   check(Badniks.entityClass(id)!=null,"Missing badnik type: "+id);
  }
  System.out.println("PASS 0.1.19: 1185 blocks, redstone plates, metadata, 4 new palette-complete templates and 4 badnik types");
 }
}
