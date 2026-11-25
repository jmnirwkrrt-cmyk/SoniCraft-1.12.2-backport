package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Optional Forge smoke check; run after real Gradle deps resolve, not an offline substitute. */
public final class BatchContent017Check {
 private static void check(boolean yes,String message){if(!yes)throw new AssertionError(message);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block b=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,b);ForgeRegistries.BLOCKS.register(b);
   if(def.kind.startsWith("facade_"))for(IBlockState state:b.getBlockState().getValidStates()){
    int meta=b.getMetaFromState(state);
    check(meta>=0&&meta<16,"bad 1.12 metadata: "+def.id+" "+state);
    check(state.equals(b.getStateFromMeta(meta)),"metadata roundtrip: "+def.id+" "+state);
   }
   if(def.kind.equals("bulk_door"))check(b instanceof BlockDoor,"door adapter: "+def.id);
  }
  for(Block b:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(b);
  check(Content.BLOCKS.size()>=933,"expected 933 registered blocks");
  for(String name:new String[]{"aquis","buzzer","bomb_badnik","crawl_badnik",
   "egg_flapper","flasher","octus","spikes_badnik","rattlekiller","rexon","shutterbug","iwamodoki"}){
   check(Badniks.entityClass(name)!=null&&Arrays.asList(Badniks.IDS).contains(name),"missing badnik: "+name);
  }
  for(String name:new String[]{"cpz_factory_2","cpz_building_1","mirage_saloon_cactus_1",
   "labyrinth_zone_path_crossroad_1","pointed_southite_up_5"}){
   StructurePort.Template t=StructurePort.get(name);
   check(t!=null&&t.usable(),"NBT palette unsupported: "+name+" "+(t==null?"absent":t.missing));
  }
  System.out.println("PASS: 933 registry blocks, facade metadata, doors, 12 badnik classes, selected NBT templates");
 }
}
