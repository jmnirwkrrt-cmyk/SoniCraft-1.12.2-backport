package org.sonicraft.backport;

import java.util.*;
import com.mojang.authlib.GameProfile;
import com.google.gson.*;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.inventory.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.*;
import net.minecraft.stats.StatBase;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class FoodContentCheck {
 static void require(boolean v,String msg){if(!v)throw new AssertionError(msg);}
 static class TestWorld extends CharacterSmokeCheck.TestWorld {
  final Map<BlockPos,IBlockState> blocks=new HashMap<>();int sounds,spawned;
  @Override public BlockPos getSpawnPoint(){return new BlockPos(0,64,0);}
  @Override public IBlockState getBlockState(BlockPos p){return blocks.getOrDefault(p,Blocks.AIR.getDefaultState());}
  @Override public boolean setBlockState(BlockPos p,IBlockState state,int flags){blocks.put(p.toImmutable(),state);return true;}
  @Override public void playSound(EntityPlayer p,double x,double y,double z,SoundEvent sound,SoundCategory category,float volume,float pitch){sounds++;}
  @Override public boolean spawnEntity(Entity e){spawned++;return true;}
  @Override public int getLightFromNeighbors(BlockPos p){return 15;}
 }
 static class Player extends EntityPlayer {
  boolean spectator;
  Player(World w){super(w,new GameProfile(new UUID(1,2),"ContentTest"));}
  @Override public boolean isSpectator(){return spectator;}
  @Override public boolean isCreative(){return capabilities.isCreativeMode;}
  @Override public void addStat(StatBase stat,int amount){}
 }
 static int rings(Player p,Item ring){int count=0;for(ItemStack s:p.inventory.mainInventory)if(s.getItem()==ring)count+=s.getCount();return count;}
 public static void main(String[] args)throws Exception {
  Bootstrap.register();
  for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);ForgeRegistries.BLOCKS.register(b);ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));}
  for(Item i:FoodContent.ITEMS.values())ForgeRegistries.ITEMS.register(i);
  TestWorld w=new TestWorld();Player p=new Player(w);
  for(String model:new String[]{"custom/piko_piko_hammer","custom/sonic_ring","block/gold_ring","block/mint_plant","item/piko_piko_hammer"})net.minecraft.client.renderer.block.model.ModelBlock.deserialize(CharacterSmokeCheck.resource("models/"+model+".json"));
  for(FoodContent.FoodDef d:FoodContent.FOODS){ItemFood f=(ItemFood)FoodContent.ITEMS.get(d.id);ItemStack stack=new ItemStack(f,2);require(f.getHealAmount(stack)==d.hunger&&f.getSaturationModifier(stack)==d.saturation&&f.getItemStackLimit()==d.stack,"Food values "+d.id);p.getFoodStats().setFoodLevel(0);p.getFoodStats().setFoodSaturationLevel(0);f.onItemUseFinish(stack,w,p);require(p.getFoodStats().getFoodLevel()==d.hunger&&stack.getCount()==1,"Food consumption "+d.id);}
  for(String id:new String[]{"sonic","tails","knuckles","amy_rose","cream","shadow","silver"}){
   Characters.Definition def=null;for(Characters.Definition d:Characters.DEFINITIONS)if(d.id.equals(id))def=d;
   LegacyCharacter hero=Characters.entityClass(def).getConstructor(World.class).newInstance(w);hero.setHealth(hero.getMaxHealth()-8);Item treat=FoodContent.favorite(id);
   p.capabilities.isCreativeMode=false;p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(treat,3,id.equals("shadow")?3:0));require(hero.processInteract(p,EnumHand.MAIN_HAND),"Interaction "+id);require(hero.getHealth()==hero.getMaxHealth()-4&&p.getHeldItemMainhand().getCount()==2,"Heal and consume "+id);
   p.setHeldItem(EnumHand.OFF_HAND,new ItemStack(treat,2,id.equals("shadow")?3:0));hero.processInteract(p,EnumHand.OFF_HAND);require(hero.getHealth()==hero.getMaxHealth()&&p.getHeldItemOffhand().getCount()==1,"Offhand/clamp "+id);
   p.capabilities.isCreativeMode=true;hero.processInteract(p,EnumHand.MAIN_HAND);require(p.getHeldItemMainhand().getCount()==2,"Creative does not consume "+id);
   p.capabilities.isCreativeMode=false;p.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(Items.STICK));require(!FoodContent.feed(hero,p,EnumHand.MAIN_HAND),"Wrong item "+id);
   boolean temptation=hero.tasks.taskEntries.stream().anyMatch(e->e.action instanceof CharacterFoodAI);require(temptation!=id.equals("cream"),"Source temptation mapping "+id);
  }
  FoodContent.Hammer hammer=(FoodContent.Hammer)FoodContent.ITEMS.get("piko_piko_hammer");Item ring=Item.getItemFromBlock(Content.BLOCKS.get("gold_ring"));require(ring!=Items.AIR,"Block-item mapping");
  require(hammer.getMaxDamage()==10000&&hammer.getItemEnchantability()==19,"Hammer durability/enchantment");require(hammer.getIsRepairable(new ItemStack(hammer),new ItemStack(ring)),"Hammer ring repair");
  require(hammer.getAttributeModifiers(EntityEquipmentSlot.MAINHAND,new ItemStack(hammer)).get(SharedMonsterAttributes.ATTACK_SPEED.getName()).iterator().next().getAmount()==-3,"Hammer attack speed");
  LegacyCharacter target=new LegacyCharacter.Knuckles(w);ItemStack weapon=new ItemStack(hammer);hammer.hitEntity(weapon,target,p);require(weapon.getItemDamage()==1,"Hammer wear");
  Block block=Content.BLOCKS.get("gold_ring");BlockPos pos=new BlockPos(0,64,0);w.blocks.put(pos,block.getDefaultState());p.inventory.clear();p.capabilities.isCreativeMode=false;
  block.onEntityCollidedWithBlock(w,pos,block.getDefaultState(),p);block.onEntityCollidedWithBlock(w,pos,block.getDefaultState(),p);require(rings(p,ring)==1&&w.isAirBlock(pos),"Single collection/no duplication");
  p.capabilities.isCreativeMode=true;w.blocks.put(pos,block.getDefaultState());block.onEntityCollidedWithBlock(w,pos,block.getDefaultState(),p);require(rings(p,ring)==1&&w.isAirBlock(pos),"Creative removes but grants no ring");
  p.spectator=true;w.blocks.put(pos,block.getDefaultState());block.onEntityCollidedWithBlock(w,pos,block.getDefaultState(),p);require(!w.isAirBlock(pos),"Spectator leaves ring");p.spectator=false;
  require(block.getCollisionBoundingBox(block.getDefaultState(),w,pos)==Block.NULL_AABB,"Ring collision");
  BlockBush mint=(BlockBush)Content.BLOCKS.get("mint_plant");w.blocks.put(pos.down(),Content.BLOCKS.get("ghz_grass").getDefaultState());require(mint.canBlockStay(w,pos,mint.getDefaultState()),"Mint supports SoniCraft ground");w.blocks.put(pos.down(),Blocks.AIR.getDefaultState());require(!mint.canBlockStay(w,pos,mint.getDefaultState()),"Mint cannot float");
  int recipes=0;
  for(String id:new String[]{"chili_dog_recipe_beef","chili_dog_recipe_pork","gold_nugget_to_ring","mint_candy_recipe","popcorn_bucket_recipe"}){
   JsonObject j=new JsonParser().parse(CharacterSmokeCheck.resource("recipes/"+id+".json")).getAsJsonObject();IRecipe recipe=j.get("type").getAsString().endsWith("shaped")?ShapedRecipes.deserialize(j):ShapelessRecipes.deserialize(j);
   InventoryCrafting grid=new InventoryCrafting(new Container(){@Override public boolean canInteractWith(EntityPlayer player){return true;}},3,3);
   if(j.has("pattern")){int y=0;JsonObject keys=j.getAsJsonObject("key");for(JsonElement row:j.getAsJsonArray("pattern")){String text=row.getAsString();for(int x=0;x<text.length();x++){char c=text.charAt(x);if(c!=' ')grid.setInventorySlotContents(y*3+x,new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(keys.getAsJsonObject(String.valueOf(c)).get("item").getAsString()))));}y++;}}
   else{int slot=0;for(JsonElement e:j.getAsJsonArray("ingredients"))grid.setInventorySlotContents(slot++,new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(e.getAsJsonObject().get("item").getAsString()))));}
   require(recipe.matches(grid,w),"Craft matches "+id);ItemStack result=recipe.getCraftingResult(grid);require(!result.isEmpty()&&result.getCount()==j.getAsJsonObject("result").get("count").getAsInt(),"Craft output "+id);recipes++;
  }
  System.out.println("PASS: 20 food consumption/value checks; 7 hero interactions, offhand/creative/wrong item; hammer durability/repair/speed; ring collection/duplication/creative/spectator; mint support; "+recipes+" original crafting recipes");
 }
}
