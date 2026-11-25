package org.sonicraft.backport;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
public final class LandmarkCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static class World extends FoodContentCheck.TestWorld {
  boolean loaded=true,foreign=false,hole=false;
  @Override protected boolean isChunkLoaded(int x,int z,boolean empty){return loaded;}
  @Override public Biome getBiome(BlockPos p){return foreign?Biomes.DESERT:Content.BIOMES.get("green_hill_zone");}
  @Override public IBlockState getBlockState(BlockPos p){return blocks.containsKey(p)?blocks.get(p):p.getY()==63?Content.BLOCKS.get("ghz_grass").getDefaultState():p.getY()<63&&!hole?Content.BLOCKS.get("ghz_dirt").getDefaultState():Blocks.AIR.getDefaultState();}
 }
 public static void main(String[] args){Bootstrap.register();for(Content.BlockDef d:Content.BLOCK_DEFS)Content.BLOCKS.put(d.id,LegacyBlocks.create(d));for(Content.BiomeDef d:Content.BIOME_DEFS)Content.BIOMES.put(d.id,new ZoneBiome(d));BlockPos p=new BlockPos(20,64,20);World w=new World();require(GreenHillLandmark.place(w,p)&&w.blocks.size()==403,"Exact 403 source cells");
  for(Object[] c:GreenHillLandmark.TEMPLATE.cells){BlockPos pos=p.add(-7,-6,-7).add(((Number)c[0]).intValue(),((Number)c[1]).intValue(),((Number)c[2]).intValue());require(w.getBlockState(pos).getBlock()==Content.BLOCKS.get("southite"),"Source position");}
  World foreign=new World();foreign.foreign=true;require(!GreenHillLandmark.place(foreign,p)&&foreign.blocks.isEmpty(),"Biome boundary");World unloaded=new World();unloaded.loaded=false;require(!GreenHillLandmark.place(unloaded,p)&&unloaded.blocks.isEmpty(),"Unloaded chunk");World hole=new World();hole.hole=true;require(!GreenHillLandmark.place(hole,p)&&hole.blocks.isEmpty(),"Unsupported base");World blocked=new World();Object[] c=GreenHillLandmark.TEMPLATE.cells[0];BlockPos obstacle=p.add(-7,-6,-7).add(((Number)c[0]).intValue(),((Number)c[1]).intValue(),((Number)c[2]).intValue());blocked.blocks.put(obstacle,Blocks.CHEST.getDefaultState());require(!GreenHillLandmark.place(blocked,p)&&blocked.blocks.size()==1,"Atomic obstacle rejection");require(!GreenHillLandmark.place(new World(),new BlockPos(20,250,20)),"World height guard");
  System.out.println("PASS: exact 403-cell GHZ source landmark; biome/support/loaded-chunk/obstruction/height preflight");
 }
}
