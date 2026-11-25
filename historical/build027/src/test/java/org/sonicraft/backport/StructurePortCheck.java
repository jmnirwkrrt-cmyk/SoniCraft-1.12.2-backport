package org.sonicraft.backport;
import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.Rotation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
/** Run after Gradle testClasses using `./gradlew structurePortCheck` */
public final class StructurePortCheck {
 static void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){Block block=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);Content.BLOCKS.put(def.id,block);ForgeRegistries.BLOCKS.register(block);}
  for(Block full:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(full);
  require(StructurePort.CATALOG.size()==361,"Original structure archive count");
  StructurePort.Template ruins=StructurePort.get("marble_ruins_1");
  require(ruins.usable()&&ruins.width==11&&ruins.height==16&&ruins.depth==11&&ruins.blocks.size()==1936,"Marble ruins geometry and palette");
  require(ruins.blockEntities==1&&ruins.entities==2,"Original chest + entities accounted for");
  for(String name:new String[]{"marble_garden_pillar_1","marble_garden_pillar_2","green_hill_mountain_small_1","green_hill_neo_palm_tree","emerald_hill_neo_palm_tree","angel_island_neo_palm_tree","giant_mushroom_1","spring_yard_platform"})
   require(StructurePort.get(name).usable(),"Ported source palette "+name);
  StructurePort.Template cave=StructurePort.get("marble_cave_1");
  require(cave.missing.isEmpty()&&cave.width==19&&cave.height==29&&cave.depth==19,"Marble cave palette + geometry mapped");
  require(cave.deferred.size()>0&&cave.markers>700&&cave.entities==3,"Dungeon controllers/jigsaw/entities still explicitly incomplete");
  for(String name:new String[]{"marble_cave","marble_cave_1_hallway_1","marble_cave_1_hallway_2","marble_cave_1_hallway_3","marble_cave_1_hallway_4","marble_cave_1_hallway_end","marble_cave_1_hallway_turn","marble_cave_1_spawner","marble_cave_1_tunnel_1","marble_cave_1_tunnel_2"})
   require(StructurePort.get(name).missing.isEmpty(),"All ported marble cave palette blocks: "+name+" "+StructurePort.get(name).missing);
  require(StructurePort.get("marble_zone_lava").width==19,"Raw lava feature archived without dangerous surface generation");
  require(StructurePort.rotatedWidth(ruins,Rotation.CLOCKWISE_90)==11,"Rotated bounds");
  require(StructurePort.get("not_a_structure")==null,"Reject unknown template name");
  System.out.println("PASS: 361 original NBTs catalogued; 11 marble cave templates palette-mapped; deferred mechanics, jigsaw and entities reported");
 }
}
