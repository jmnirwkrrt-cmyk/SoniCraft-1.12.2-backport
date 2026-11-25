package org.sonicraft.backport;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Forge-classpath smoke test, deliberately separate from offline syntax validation.
 * Run with Gradle/Forge resolved; no claims about actual client rendering. */
public final class BatchContent020Check {
 private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
 public static void main(String[] ignored){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){
   Block block=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);
   Content.BLOCKS.put(def.id,block);
   ForgeRegistries.BLOCKS.register(block);
   if(def.kind.startsWith("facade_"))for(IBlockState s:block.getBlockState().getValidStates()){
    int meta=block.getMetaFromState(s);
    require(meta>=0&&meta<16,"Invalid metadata "+def.id+" = "+meta);
    require(block.getStateFromMeta(meta).equals(s),"Metadata collision "+def.id+" "+s);
   }
  }
  for(Block doubleSlab:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(doubleSlab);
  require(Content.BLOCKS.size()>=1202,"Unexpected block registry count");
  for(String id:new String[]{"press_garden_letter_block","dark_press_garden_letter_block","dim_press_garden_letter_block"}){
   require(Content.BLOCKS.containsKey(id),"Missing 12-state letter block "+id);
   require(Content.BLOCKS.get(id).getBlockState().getValidStates().size()==16,"Letter block needs full metadata range "+id);
  }
  require(UrbanJigsaw.cities().contains("casino")&&UrbanJigsaw.cities().contains("spring"),"Missing city root");
  for(String city:UrbanJigsaw.cities()){
   int successful=0;
   for(long seed=0;seed<10;seed++){
    MarbleDungeon.Plan plan=UrbanJigsaw.plan(city,new BlockPos(0,100,0),seed);
    if(plan.pieces.size()>1)successful++;
    require(plan.pieces.size()<=UrbanJigsaw.MAX_PIECES,"Excessive city sections");
    for(int i=0;i<plan.pieces.size();i++)for(int j=0;j<i;j++)
     require(!plan.pieces.get(i).intersects(plan.pieces.get(j)),"City AABB overlap "+city);
   }
   require(successful>0,"City sections never join: "+city);
  }
  System.out.println("PASS 0.1.20: 1202 blocks, 12-state letter variants, connected nonoverlapping city plans");
 }
}
