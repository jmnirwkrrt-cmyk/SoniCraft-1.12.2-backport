package org.sonicraft.backport;
import net.minecraft.block.*;import net.minecraft.block.state.IBlockState;import net.minecraft.init.Blocks;import net.minecraft.util.Rotation;import net.minecraft.util.math.BlockPos;import net.minecraft.world.*;import net.minecraft.world.biome.Biome;
public final class Placement43 {
 public static boolean canPlace(World w,BlockPos at,StructurePort.Template t,Rotation r,boolean natural){
  if(t==null||!t.usable()||w.isRemote||at.getY()<1||at.getY()+t.height>=255||t.blocks.size()>70000)return false;
  if(!w.isAreaLoaded(at,at.add(StructurePort.rotatedWidth(t,r)-1,t.height-1,StructurePort.rotatedDepth(t,r)-1)))return false;
  for(StructurePort.Cell cell:t.blocks){BlockPos p=StructurePort.at(t,at,cell,r);IBlockState s=w.getBlockState(p);Block b=s.getBlock();
   if(b==Blocks.BEDROCK||w.getTileEntity(p)!=null)return false;if(!natural)continue;
   Biome biome=w.getBiome(p);Block top=null,fill=null;if(biome instanceof ZoneBiome){Content.BiomeDef d=((ZoneBiome)biome).definition;top=Content.BLOCKS.get(d.top);fill=Content.BLOCKS.get(d.filler);}
   if(s.getMaterial().isLiquid()){
    boolean oil=t.name.startsWith("oil_ocean_")&&b==Content.BLOCKS.get("oil");
    boolean water=(t.name.startsWith("green_hill_lake")||t.name.startsWith("aquatic_ruin_")||t.name.startsWith("seaside_hill_lake")||t.name.startsWith("hydrocity_"))&&(b==Blocks.WATER||b==Blocks.FLOWING_WATER);
    boolean lava=t.name.startsWith("hill_top_lava_lake")&&(b==Blocks.LAVA||b==Blocks.FLOWING_LAVA);if(!oil&&!water&&!lava)return false;continue;
   }
   if(w.isAirBlock(p)||!s.getMaterial().isSolid()||s.getMaterial().isReplaceable()||b.isLeaves(s,w,p)||b instanceof BlockLog||b==top||b==fill)continue;
   if(b==Blocks.STONE||b==Blocks.DIRT||b==Blocks.GRASS||b==Blocks.GRAVEL||b==Blocks.SAND||b==Blocks.COAL_ORE||b==Blocks.IRON_ORE||b==Blocks.GOLD_ORE||b==Blocks.REDSTONE_ORE||b==Blocks.LIT_REDSTONE_ORE||b==Blocks.LAPIS_ORE||b==Blocks.DIAMOND_ORE||b==Blocks.EMERALD_ORE)continue;
   if(b.getRegistryName()!=null&&b.getRegistryName().getResourceDomain().equals("sonicraft")){
    String id=b.getRegistryName().getResourcePath();if(id.equals("chaostone")||id.equals("marble_zone_grass")||id.equals("marble_zone_stone")||id.equals("marble_zone_cave_wall")||id.endsWith("_ore")||id.equals("chilly_blossom_log")||id.equals("hillpine_log")||id.equals("green_hill_palm_log"))continue;
   }
   return false;
  }return true;
 }
}
