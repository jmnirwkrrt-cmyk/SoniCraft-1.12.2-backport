package org.sonicraft.backport;
import java.util.*;
import java.nio.file.*;
import java.io.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class BulkContentCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args)throws Exception {
  Bootstrap.register();int count=0;FoodContentCheck.TestWorld w=new FoodContentCheck.TestWorld();BlockPos p=new BlockPos(0,64,0);
  for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id).setCreativeTab(CreativeCatalog.forId(d.id));Content.BLOCKS.put(d.id,b);ForgeRegistries.BLOCKS.register(b);ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()).setCreativeTab(CreativeCatalog.forId(d.id)));if(!d.kind.startsWith("bulk_"))continue;count++;
   require(Math.abs(b.getBlockHardness(b.getDefaultState(),w,p)-d.hardness)<.001,"Hardness "+d.id);require(Math.abs(b.getExplosionResistance(null)-d.resistance)<.001,"Resistance "+d.id);require(b.getFlammability(w,p,EnumFacing.UP)==d.flammability,"Flammability "+d.id);require(b.getItemDropped(b.getDefaultState(),new Random(),0)==Item.getItemFromBlock(b),"Self drop "+d.id);
  }
  require(count==274,"Batch size");
  Block stairs=Content.BLOCKS.get("chaostone_stairs");IBlockState east=stairs.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.EAST),north=stairs.getDefaultState().withProperty(BlockStairs.FACING,EnumFacing.NORTH);w.blocks.put(p,east);w.blocks.put(p.east(),north);require(stairs.getActualState(east,w,p).getValue(BlockStairs.SHAPE)!=BlockStairs.EnumShape.STRAIGHT,"Stair corner connection");
  Block fence=Content.BLOCKS.get("green_hill_palm_fence");w.blocks.clear();w.blocks.put(p,fence.getDefaultState());w.blocks.put(p.north(),fence.getDefaultState());require(fence.getActualState(fence.getDefaultState(),w,p).getValue(BlockFence.NORTH),"Fence connection");
  Block gate=Content.BLOCKS.get("green_hill_palm_fence_gate");w.blocks.clear();w.blocks.put(p,gate.getDefaultState());FoodContentCheck.Player player=new FoodContentCheck.Player(w);require(gate.onBlockActivated(w,p,w.getBlockState(p),player,EnumHand.MAIN_HAND,EnumFacing.NORTH,0,0,0)&&w.getBlockState(p).getValue(BlockFenceGate.OPEN),"Working gate");
  require(CreativeCatalog.forId("chili_dog")==CreativeCatalog.MISC&&CreativeCatalog.forId("chaostone_stairs")==CreativeCatalog.BLOCKS,"Original tabs");NonNullList<ItemStack> eggs=NonNullList.create();CreativeCatalog.CHARACTERS.displayAllRelevantItems(eggs);int eggCount=0;for(ItemStack s:eggs)if(s.getItem()==Items.SPAWN_EGG)eggCount++;require(eggCount==22,"22 character and mob eggs in own tab");
  Path resources=Paths.get(BulkContentCheck.class.getResource("/assets/sonicraft/backport/blocks.json").toURI()).getParent().getParent();int models=0;
  try(java.util.stream.Stream<Path> stream=Files.walk(resources.resolve("models"))){for(Path file:(Iterable<Path>)stream.filter(f->f.toString().endsWith(".json"))::iterator){try(Reader reader=Files.newBufferedReader(file)){require(net.minecraft.client.renderer.block.model.ModelBlock.deserialize(reader)!=null,"Model parse "+file);models++;}}}
  for(Item item:FoodContent.ITEMS.values())ForgeRegistries.ITEMS.register(item);ForgeRegistries.ITEMS.register(MobiusPortal.IGNITER);
  net.minecraftforge.common.crafting.CraftingHelper.init();int recipes=0;
  try(java.util.stream.Stream<Path> stream=Files.list(resources.resolve("recipes"))){for(Path file:(Iterable<Path>)stream.filter(f->f.toString().endsWith(".json"))::iterator){com.google.gson.JsonObject json=new com.google.gson.JsonParser().parse(new String(Files.readAllBytes(file),java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();net.minecraft.item.crafting.IRecipe recipe=net.minecraftforge.common.crafting.CraftingHelper.getRecipe(json,new net.minecraftforge.common.crafting.JsonContext("sonicraft"));require(!recipe.getRecipeOutput().isEmpty(),"Recipe output "+file);for(net.minecraft.item.crafting.Ingredient ingredient:recipe.getIngredients())if(ingredient!=net.minecraft.item.crafting.Ingredient.EMPTY)require(ingredient.getMatchingStacks().length>0,"Recipe ingredient "+file);recipes++;}}
  System.out.println("PASS: 274 blocks source properties/drops, stair corners, fence connections, working gates, original creative groups and 22 eggs; "+models+" models and "+recipes+" recipes parsed by Minecraft/Forge 1.12");
 }
}
