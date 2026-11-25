package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.inventory.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import com.google.gson.*;
public final class MobiusPortalCheck {
 static class PlacementWorld extends FoodContentCheck.TestWorld {
  final net.minecraft.world.border.WorldBorder border=new net.minecraft.world.border.WorldBorder();
  @Override public int getHeight(int x,int z){return 64;}
  @Override public int getActualHeight(){return 256;}
  @Override public net.minecraft.world.border.WorldBorder getWorldBorder(){return border;}
 }
 static class TerrainWorld extends net.minecraft.world.World {
  TerrainWorld(){super(new net.minecraft.world.storage.SaveHandlerMP(),new net.minecraft.world.storage.WorldInfo(new net.minecraft.world.WorldSettings(234,net.minecraft.world.GameType.SURVIVAL,false,false,net.minecraft.world.WorldType.DEFAULT),"mobius-test"),new MobiusWorld(),new net.minecraft.profiler.Profiler(),false);provider.setWorld(this);}
  @Override protected net.minecraft.world.chunk.IChunkProvider createChunkProvider(){return null;}
  @Override protected boolean isChunkLoaded(int x,int z,boolean allowEmpty){return false;}
 }
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static void frame(FoodContentCheck.TestWorld w,BlockPos base,EnumFacing dir,int width,int height){
  for(int x=-1;x<=width;x++)for(int y=-1;y<=height;y++)if(x==-1||x==width||y==-1||y==height)w.setBlockState(base.offset(dir,x).up(y),Blocks.GOLD_BLOCK.getDefaultState(),2);
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();
  for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);ForgeRegistries.BLOCKS.register(b);ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));}
  ForgeRegistries.BLOCKS.register(MobiusPortal.BLOCK);ForgeRegistries.ITEMS.register(MobiusPortal.IGNITER);
  for(EnumFacing dir:new EnumFacing[]{EnumFacing.EAST,EnumFacing.SOUTH})for(int width:new int[]{2,4,21}){
   FoodContentCheck.TestWorld w=new FoodContentCheck.TestWorld();BlockPos base=new BlockPos(0,64,0);frame(w,base,dir,width,3);
   require(MobiusPortal.BLOCK.trySpawnPortal(w,base),"Gold frame ignition "+dir+width);
   for(int x=0;x<width;x++)for(int y=0;y<3;y++)require(w.getBlockState(base.offset(dir,x).up(y)).getBlock()==MobiusPortal.BLOCK,"Filled interior");
   require(w.getBlockState(base).getValue(MobiusPortal.AXIS)==dir.getAxis(),"Axis");
   w.setBlockToAir(base.offset(dir,-1));MobiusPortal.BLOCK.neighborChanged(w.getBlockState(base),w,base,Blocks.AIR,base.offset(dir,-1));require(w.isAirBlock(base),"Broken frame collapses");
  }
  for(int size:new int[]{1,22}){FoodContentCheck.TestWorld w=new FoodContentCheck.TestWorld();BlockPos b=new BlockPos(0,64,0);frame(w,b,EnumFacing.EAST,size,3);require(!MobiusPortal.BLOCK.trySpawnPortal(w,b),"Invalid width");}
  FoodContentCheck.TestWorld w=new FoodContentCheck.TestWorld();BlockPos b=new BlockPos(0,64,0);frame(w,b,EnumFacing.EAST,2,3);w.setBlockState(b.up(),Blocks.STONE.getDefaultState(),2);require(!MobiusPortal.BLOCK.trySpawnPortal(w,b),"Blocked interior");
  for(Content.BiomeDef d:Content.BIOME_DEFS){ZoneBiome biome=new ZoneBiome(d);biome.setRegistryName("sonicraft",d.id);Content.BIOMES.put(d.id,biome);ForgeRegistries.BIOMES.register(biome);}
  MobiusWorld.Provider a=new MobiusWorld.Provider(234),copy=new MobiusWorld.Provider(234),different=new MobiusWorld.Provider(235);Set<Biome> found=new HashSet<>();boolean changed=false;
  for(int z=-8192;z<=8192;z+=512)for(int x=-8192;x<=8192;x+=512){Biome[] aa=a.getBiomes(null,x,z,32,32),bb=copy.getBiomes(null,x,z,32,32),cc=different.getBiomes(null,x,z,32,32);require(Arrays.equals(aa,bb),"Stable seed");changed|=!Arrays.equals(aa,cc);found.addAll(Arrays.asList(aa));}
  require(changed&&found.size()==16,"Sixteen Mobius zones and seed variation");for(Biome biome:found)require(Arrays.asList(MobiusWorld.ZONES).contains(biome.getRegistryName().getResourcePath()),"No Angel Island/vanilla biomes");
  require(!a.areBiomesViable(0,0,100,Collections.emptyList()),"Viability");require(a.findBiomePosition(0,0,100,new ArrayList<>(found),new Random(1))!=null,"Spawn search");
  PlacementWorld placement=new PlacementWorld();BlockPos created=MobiusTeleporter.buildPortal(placement,new BlockPos(4,70,5));require(created!=null,"Exit created");BlockPos exit=MobiusTeleporter.exit(placement,created);require(exit!=null&&placement.isAirBlock(exit)&&placement.isAirBlock(exit.up())&&placement.getBlockState(exit.down()).isFullCube(),"Safe exit feet/head/floor");
  placement.setBlockState(exit,Blocks.STONE.getDefaultState(),2);require(MobiusTeleporter.exit(placement,created)!=null&&!MobiusTeleporter.exit(placement,created).equals(exit),"Blocked exit uses opposite side");
  PlacementWorld obstructed=new PlacementWorld();obstructed.setBlockState(new BlockPos(4,65,5),Blocks.STONE.getDefaultState(),2);require(MobiusTeleporter.buildPortal(obstructed,new BlockPos(4,70,5))==null&&obstructed.getBlockState(new BlockPos(4,65,5)).getBlock()==Blocks.STONE,"Refuse obstructed destination without carving");
  MobiusWorld.TYPE=net.minecraft.world.DimensionType.register("Mobius Test","_mobius_test",11230,MobiusWorld.class,false);
  TerrainWorld terrain=new TerrainWorld();net.minecraft.world.gen.IChunkGenerator generator=terrain.provider.createChunkGenerator();
  for(int cx:new int[]{-2,0,3}){net.minecraft.world.chunk.Chunk chunk=generator.generateChunk(cx,0);require(chunk.getHeightValue(8,8)>0,"Nonempty terrain");for(byte bid:chunk.getBiomeArray())require(found.contains(Biome.getBiome(bid&255)),"Generated chunk biome");}
  JsonObject json=new JsonParser().parse(CharacterSmokeCheck.resource("recipes/flint_and_ring_recipe.json")).getAsJsonObject();IRecipe recipe=ShapedRecipes.deserialize(json);
  InventoryCrafting grid=new InventoryCrafting(new Container(){public boolean canInteractWith(EntityPlayer p){return true;}},2,2);grid.setInventorySlotContents(0,new ItemStack(Content.BLOCKS.get("gold_ring")));grid.setInventorySlotContents(3,new ItemStack(Items.FLINT));require(recipe.matches(grid,w)&&recipe.getCraftingResult(grid).getItem()==MobiusPortal.IGNITER,"Original igniter recipe");
  System.out.println("PASS: both portal axes, 2/4/21-wide gold frames, invalid sizes/obstructions/breakage, 16 deterministic Mobius biomes, spawn search, 3 real terrain chunks, safe exit/fallback/no carving, original igniter recipe");
 }
}
