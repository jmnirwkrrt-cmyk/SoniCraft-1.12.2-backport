package org.sonicraft.backport;
import java.util.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
@Mod.EventBusSubscriber(modid=SonicraftLegacy.ID)
public final class EmeraldProgression {
 static final String TAG="SonicraftChaosEmeralds";
 static final String[] COLORS={"green","yellow","blue","purple","white","light_blue","red"};
 public static final Map<String,Item> ITEMS=new LinkedHashMap<>();
 public static final Map<String,SoundEvent> SOUNDS=new LinkedHashMap<>();
 public static final Item KEY=add("special_stage_key",new Item(){@Override public EnumRarity getRarity(ItemStack s){return EnumRarity.UNCOMMON;}});
 static{for(int i=0;i<7;i++)add("chaos_emerald_"+COLORS[i],new Gem(i));for(String n:new String[]{"signpost.spin","chaosemerald.collect"})SOUNDS.put(n,new SoundEvent(new ResourceLocation("sonicraft",n)).setRegistryName("sonicraft",n));}
 static Item add(String id,Item item){item.setRegistryName("sonicraft",id).setUnlocalizedName("sonicraft."+id).setCreativeTab(CreativeCatalog.MISC);ITEMS.put(id,item);return item;}
 static Item emerald(int i){return ITEMS.get("chaos_emerald_"+COLORS[i]);}
 static int stored(EntityPlayer p){return p.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).getInteger(TAG)&127;}
 static void store(EntityPlayer p,int mask){NBTTagCompound n=p.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);n.setInteger(TAG,mask&127);p.getEntityData().setTag(EntityPlayer.PERSISTED_NBT_TAG,n);}
 static int inventory(EntityPlayer p){int mask=0;for(int s=0;s<p.inventory.getSizeInventory();s++)for(int i=0;i<7;i++)if(p.inventory.getStackInSlot(s).getItem()==emerald(i))mask|=1<<i;return mask;}
 static boolean redeem(EntityPlayer p,EnumHand hand){
  if(p.world.isRemote||p.isSpectator()||hand!=EnumHand.MAIN_HAND||p.getHeldItem(hand).getItem()!=KEY)return false;
  int owned=stored(p)|inventory(p);p.getHeldItem(hand).shrink(1);
  if(owned==127){net.minecraftforge.items.ItemHandlerHelper.giveItemToPlayer(p,new ItemStack(Content.BLOCKS.get("gold_ring"),50));p.world.playSound(null,p.getPosition(),FoodContent.SOUNDS.get("ring.collect"),SoundCategory.NEUTRAL,1,1);return true;}
  for(int i=0;i<7;i++)if((owned&(1<<i))==0){store(p,stored(p)|(1<<i));p.world.playSound(null,p.getPosition(),SOUNDS.get("chaosemerald.collect"),SoundCategory.NEUTRAL,1,1);p.sendStatusMessage(new net.minecraft.util.text.TextComponentTranslation("sonicraft.emerald.collected"),true);return true;}return false;
 }
 static boolean withdraw(EntityPlayer p,int i){if(p.world.isRemote||p.isSpectator()||i<0||i>=7||(stored(p)&(1<<i))==0||(inventory(p)&(1<<i))!=0)return false;ItemStack stack=new ItemStack(emerald(i));if(!p.inventory.addItemStackToInventory(stack))return false;if(!p.capabilities.isCreativeMode)store(p,stored(p)&~(1<<i));p.inventory.markDirty();return true;}
 static final class Gem extends Item {
  final int color;Gem(int color){this.color=color;setMaxStackSize(1);}
  @Override public EnumRarity getRarity(ItemStack s){return EnumRarity.RARE;}
  @Override public void onUpdate(ItemStack s,net.minecraft.world.World w,net.minecraft.entity.Entity e,int slot,boolean selected){if(!w.isRemote&&e instanceof EntityPlayer&&((EntityPlayer)e).capabilities.isCreativeMode){EntityPlayer p=(EntityPlayer)e;store(p,stored(p)|(1<<color));}}
 }
 @SubscribeEvent public static void clonePlayer(PlayerEvent.Clone e){store(e.getEntityPlayer(),stored(e.getOriginal()));}
}
