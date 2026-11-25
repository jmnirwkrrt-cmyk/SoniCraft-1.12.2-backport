package org.sonicraft.backport;

import java.util.*;
import com.google.common.collect.Multimap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.*;
import net.minecraft.init.Items;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.items.ItemHandlerHelper;

/** Source-backed food/items milestone. Acquisition through unported shops stays unimplemented. */
public final class FoodContent {
 public static final class FoodDef {public String id,source;public int hunger,stack;public float saturation;}
 public static final FoodDef[] FOODS=Content.read("foods.json",FoodDef[].class);
 public static final Map<String,Item> ITEMS=new LinkedHashMap<>();
 public static final Map<String,SoundEvent> SOUNDS=new LinkedHashMap<>();
 static {
  for(String id:new String[]{"ring.collect","ring.place","pikopikohammer.attack"}){
   ResourceLocation key=new ResourceLocation(SonicraftLegacy.ID,id);SOUNDS.put(id,new SoundEvent(key).setRegistryName(key));
  }
  for(FoodDef d:FOODS){ItemFood food=new Food(d);add(d.id,food);}
  add("piko_piko_hammer",new Hammer());
  add("bubblegum",new Item()); // Attraction item; chewing/projectile chain remains a separate milestone.
 }
 private static void add(String id,Item item){item.setRegistryName(SonicraftLegacy.ID,id).setUnlocalizedName(SonicraftLegacy.ID+"."+id).setCreativeTab(SonicraftLegacy.TAB);ITEMS.put(id,item);}
 static class Food extends ItemFood {
  Food(FoodDef d){super(d.hunger,d.saturation,false);setMaxStackSize(d.stack);}
  @Override public float getDestroySpeed(ItemStack stack,IBlockState state){return 0;}
 }
 static Item favorite(String id){
  switch(id){case "shadow":return Items.DYE;case "silver":return Items.APPLE;case "vector":return ITEMS.get("bubblegum");case "sonic":return ITEMS.get("chili_dog");case "tails":return ITEMS.get("mint_candy");case "knuckles":return ITEMS.get("grapes");case "amy_rose":case "cream":return ITEMS.get("soft_serve_ice_cream");default:return null;}
 }
 static boolean matchesFavorite(String id,ItemStack held){
  if(id.equals("rouge"))return !held.isEmpty()&&(held.getItem()==Items.DIAMOND||held.getItem()==Items.EMERALD);
  Item food=favorite(id);return food!=null&&!held.isEmpty()&&held.getItem()==food&&(!id.equals("shadow")||held.getMetadata()==3);
 }
 static boolean isTempting(String id,EntityPlayer player){return matchesFavorite(id,player.getHeldItemMainhand())||matchesFavorite(id,player.getHeldItemOffhand());}
 static void addCharacterGoals(LegacyCharacter character){
  String id=character.definition().id;Item food=favorite(id);
  // Cream shares Amy's right-click procedure, but has no food TemptGoal in the source.
  if((food!=null&&!id.equals("cream"))||id.equals("rouge"))character.tasks.addTask(3,new CharacterFoodAI(character));
  if(id.equals("amy_rose"))character.tasks.addTask(6,new EntityAIWatchClosest(character,LegacyCharacter.Sonic.class,20));
 }
 static boolean feed(LegacyCharacter character,EntityPlayer player,EnumHand hand){
  String id=character.definition().id;ItemStack held=player.getHeldItem(hand);
  if(!matchesFavorite(id,held)||id.equals("vector")||id.equals("rouge"))return false;
  if(!character.world.isRemote){
   character.heal(4); // The original consumes the treat even at full health.
   character.world.playSound(null,character.posX,character.posY,character.posZ,SoundEvents.ENTITY_GENERIC_EAT,SoundCategory.NEUTRAL,1,1);
   if(!player.capabilities.isCreativeMode)held.shrink(1);
  }
  return true;
 }
 public static final class Hammer extends ItemSword {
  private static final Item.ToolMaterial MATERIAL=EnumHelper.addToolMaterial("SONICRAFT_PIKO",1,10000,4,3,19);
  Hammer(){super(MATERIAL);}
  @Override public Multimap<String,AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot,ItemStack stack){
   Multimap<String,AttributeModifier> attributes=super.getAttributeModifiers(slot,stack);
   if(slot==EntityEquipmentSlot.MAINHAND){attributes.removeAll(SharedMonsterAttributes.ATTACK_SPEED.getName());attributes.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),new AttributeModifier(ATTACK_SPEED_MODIFIER,"Weapon modifier",-3,0));}
   return attributes;
  }
  @Override public boolean getIsRepairable(ItemStack damaged,ItemStack material){return !material.isEmpty()&&material.getItem()==Item.getItemFromBlock(Content.BLOCKS.get("gold_ring"))||super.getIsRepairable(damaged,material);}
  @Override public boolean hitEntity(ItemStack stack,EntityLivingBase target,EntityLivingBase attacker){boolean hit=super.hitEntity(stack,target,attacker);if(!target.world.isRemote)target.world.playSound(null,target.posX,target.posY,target.posZ,SOUNDS.get("pikopikohammer.attack"),SoundCategory.NEUTRAL,1,1);return hit;}
 }
 public static final class Mint extends BlockBush {
  Mint(){super(Material.PLANTS);setHardness(0);setSoundType(SoundType.PLANT);}
  @Override protected boolean canSustainBush(IBlockState state){return state.getMaterial()==Material.GROUND||state.getMaterial()==Material.GRASS;}
  @Override public Block.EnumOffsetType getOffsetType(){return Block.EnumOffsetType.XZ;}
  @Override public int getFlammability(IBlockAccess world,BlockPos pos,EnumFacing face){return 100;}
  @Override public int getFireSpreadSpeed(IBlockAccess world,BlockPos pos,EnumFacing face){return 60;}
 }
 public static final class Ring extends Block {
  private static final AxisAlignedBB BOX=new AxisAlignedBB(.2,.2,.2,.8,.8,.8);
  Ring(){super(Material.IRON);setHardness(0);setLightOpacity(0);setSoundType(new SoundType(1,1,SOUNDS.get("ring.place"),SoundEvents.BLOCK_METAL_STEP,SOUNDS.get("ring.place"),SoundEvents.BLOCK_METAL_HIT,SoundEvents.BLOCK_METAL_FALL));}
  @Override public boolean isOpaqueCube(IBlockState state){return false;}
  @Override public boolean isFullCube(IBlockState state){return false;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  @Override public AxisAlignedBB getBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return BOX;}
  @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state,IBlockAccess world,BlockPos pos){return NULL_AABB;}
  @Override public void onEntityCollidedWithBlock(World world,BlockPos pos,IBlockState state,Entity entity){
   if(world.isRemote||!(entity instanceof EntityPlayer)||((EntityPlayer)entity).isSpectator()||world.getBlockState(pos).getBlock()!=this)return;
   EntityPlayer player=(EntityPlayer)entity;
   // Remove before granting: repeated callbacks cannot collect the same ring twice.
   if(!world.setBlockToAir(pos))return;
   world.playSound(null,pos,SOUNDS.get("ring.collect"),SoundCategory.NEUTRAL,1,1);
   if(!player.capabilities.isCreativeMode)ItemHandlerHelper.giveItemToPlayer(player,new ItemStack(this));
   if(world instanceof WorldServer)((WorldServer)world).spawnParticle(EnumParticleTypes.FIREWORKS_SPARK,pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,3,.3,.3,.3,0);
  }
 }
}
