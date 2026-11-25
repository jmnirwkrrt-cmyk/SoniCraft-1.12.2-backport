package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class FloraCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static class World extends FoodContentCheck.TestWorld {
  ZoneBiome zone;boolean loaded=true;
  @Override public Biome getBiome(BlockPos p){return zone==null?Biomes.PLAINS:zone;}
  @Override protected boolean isChunkLoaded(int x,int z,boolean empty){return loaded;}
  @Override public BlockPos getHeight(BlockPos p){return new BlockPos(p.getX(),64,p.getZ());}
  @Override public IBlockState getBlockState(BlockPos p){return blocks.containsKey(p)?blocks.get(p):p.getY()==63&&zone!=null?zone.topBlock:Blocks.AIR.getDefaultState();}
  @Override public boolean setBlockState(BlockPos p,IBlockState state,int flags){IBlockState old=getBlockState(p);super.setBlockState(p,state,flags);if(old.getBlock()!=state.getBlock())old.getBlock().breakBlock(this,p,old);return true;}
 }
 public static void main(String[] args){
  Bootstrap.register();for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);ForgeRegistries.BLOCKS.register(b);ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));}
  require(ZoneFlora.PATCHES.length==5,"Five source patches");Set<String> seen=new HashSet<>();
  for(String biome:new String[]{"green_hill_zone","emerald_hill_zone","hill_top_zone"}){
   World w=new World();for(Content.BiomeDef d:Content.BIOME_DEFS)if(d.id.equals(biome))w.zone=new ZoneBiome(d);BlockPos p=new BlockPos(0,64,0);
   for(ZoneFlora.Patch patch:ZoneFlora.PATCHES)if(patch.biome.equals(biome)){
    for(ZoneFlora.Choice c:patch.choices){
     w.blocks.clear();require(ZoneFlora.place(w,p,biome,c),"Correct soil "+c.id);require(!ZoneFlora.place(w,p,biome,c),"No overwrite");
     if(Content.BLOCKS.get(c.id) instanceof TallZoneFlower){
      TallZoneFlower b=(TallZoneFlower)Content.BLOCKS.get(c.id);require(!TallZoneFlower.upper(w.getBlockState(p))&&TallZoneFlower.upper(w.getBlockState(p.up())),"Stem below flower");
      require(b.canBlockStay(w,p,w.getBlockState(p))&&b.canBlockStay(w,p.up(),w.getBlockState(p.up())),"Both halves survive");
      require(b.getItemDropped(w.getBlockState(p.up()),new Random(),0)==Items.AIR,"Upper half cannot duplicate drops");
      w.setBlockToAir(p);require(w.isAirBlock(p.up()),"Lower break removes upper");
      require(b.place(w,p),"Replant");w.setBlockToAir(p.up());require(w.isAirBlock(p),"Upper break removes lower");
      w.blocks.clear();w.blocks.put(p.up(),Blocks.STONE.getDefaultState());require(!ZoneFlora.place(w,p,biome,c)&&w.isAirBlock(p),"Obstructed top leaves no partial plant");
      w.blocks.clear();require(!b.place(w,new BlockPos(0,255,0)),"World height guard");
      // Real ItemBlock placement checks and onBlockPlacedBy path.
      require(b.canPlaceBlockAt(w,p),"Player placement");w.setBlockState(p,b.getDefaultState(),2);b.onBlockPlacedBy(w,p,b.getDefaultState(),null,ItemStack.EMPTY);require(TallZoneFlower.upper(w.getBlockState(p.up())),"Player creates upper half");
      FoodContentCheck.Player player=new FoodContentCheck.Player(w);player.capabilities.isCreativeMode=true;int before=w.spawned;b.onBlockHarvested(w,p.up(),w.getBlockState(p.up()),player);w.setBlockToAir(p.up());require(w.isAirBlock(p)&&w.spawned==before,"Creative removes pair without drops");
     }
     w.blocks.clear();w.blocks.put(p.down(),Blocks.STONE.getDefaultState());require(!ZoneFlora.place(w,p,biome,c),"Reject wrong soil");
     w.blocks.clear();w.loaded=false;require(!ZoneFlora.place(w,p,biome,c),"No unloaded writes");w.loaded=true;
     ZoneBiome saved=w.zone;w.zone=null;require(!ZoneFlora.place(w,p,biome,c),"No spill into vanilla biome");w.zone=saved;
    }
    if(patch.choices.length>1){int[] counts=new int[patch.choices.length+1];Random r=new Random(42);for(int i=0;i<160000;i++){ZoneFlora.Choice c=ZoneFlora.choose(patch,r);counts[c==null?patch.choices.length:Arrays.asList(patch.choices).indexOf(c)]++;}for(int i=0;i<counts.length;i++){double expected=Math.pow(.5,Math.min(i+1,patch.choices.length));require(Math.abs(counts[i]/160000.0-expected)<.006,"Ordered selector/no_op "+i);}}
   }
   for(int seed=0;seed<128;seed++){w.blocks.clear();ZoneFlora.decorate(w,new Random(seed),BlockPos.ORIGIN,biome);for(IBlockState s:w.blocks.values())seen.add(s.getBlock().getRegistryName().getResourcePath());}
  }
  require(seen.size()==11,"All eleven plants generated: "+seen);
  System.out.println("PASS: 11 plants/5 source patches; selector probabilities/no_op; soil/biome/height/obstruction guards; GHZ stem and flower halves, metadata, player placement and pair removal");
 }
}
