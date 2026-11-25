package org.sonicraft.backport;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Requires a real Forge test classpath; this is NOT an offline substitute for compilation. */
public final class BatchContent018Check {
 private static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block b=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,b);
   ForgeRegistries.BLOCKS.register(b);
   if(def.kind.startsWith("facade_"))for(IBlockState state:b.getBlockState().getValidStates()){
    int meta=b.getMetaFromState(state);
    check(meta>=0&&meta<16,"metadata out of range: "+def.id+" "+state);
    check(state.equals(b.getStateFromMeta(meta)),"metadata mismatch: "+def.id+" "+state);
   }
   if(def.kind.equals("bulk_door"))check(b instanceof BlockDoor,"not a door: "+def.id);
  }
  for(Block b:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(b);
  check(Content.BLOCKS.size()>=1100,"expected 1100 blocks");
  for(String id:new String[]{"green_hill_totem","mirage_saloon_bird_totem_1","studiopolis_lamppost",
    "night_carnival_lamppost","crisis_city_road","popcorn_shop","scrap_brain_factory_building_1"}){
   StructurePort.Template t=StructurePort.get(id);
   check(t!=null&&t.usable(),"source structure incomplete: "+id+" "+(t==null?"not found":t.missing));
  }
  check(Content.BLOCKS.get("scrap_brain_iron_door") instanceof BlockDoor,"Scrap Brain door cannot open");
  check(Content.BLOCKS.get("night_carnival_door") instanceof BlockDoor,"Night Carnival door cannot open");
  System.out.println("PASS 0.1.18: 1100 registered blocks, facade metadata, door adapters and architecture templates");
 }
}
