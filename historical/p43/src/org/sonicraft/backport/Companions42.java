package org.sonicraft.backport;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.oredict.OreDictionary;

@Mod.EventBusSubscriber(modid="sonicraft")
public final class Companions42 {
 public static final String OWNER="SC42Owner", FOLLOW="SC42Follow", READY="SC42HugReady", START="SC42HugStart";
 public static final int HUG_TICKS=80, COOLDOWN_TICKS=6000;
 static final Set<String> MOBIANS=new HashSet<>(Arrays.asList("sonic","tails","knuckles","amy_rose","rouge","shadow","silver","big_the_cat","espio","charmy","vector","infinite","blaze_the_cat","cream"));
 static boolean mobian(LegacyCharacter e){return MOBIANS.contains(e.definition().id);}
 static UUID owner(LegacyCharacter e){NBTTagCompound n=e.getEntityData();return n.hasUniqueId(OWNER)?n.getUniqueId(OWNER):null;}
 static boolean follows(LegacyCharacter e){return e.getEntityData().getBoolean(FOLLOW);}
 static boolean ownedBy(LegacyCharacter e,EntityPlayer p){return p.getUniqueID().equals(owner(e));}
 public static boolean protectedTarget(LegacyCharacter e,Entity t){
  UUID id=owner(e);if(id==null||t==null)return false;
  if(id.equals(t.getUniqueID()))return true;
  if(t instanceof LegacyCharacter&&id.equals(owner((LegacyCharacter)t)))return true;
  if(t instanceof net.minecraft.entity.passive.EntityTameable&&id.equals(((net.minecraft.entity.passive.EntityTameable)t).getOwnerId()))return true;
  EntityPlayer p=e.world.getPlayerEntityByUUID(id);return p!=null&&p.isOnSameTeam(t);
 }
 public static EntityLivingBase filterTarget(LegacyCharacter e,EntityLivingBase t){return protectedTarget(e,t)?null:t;}
 public static boolean favorite(String id,ItemStack s){
  if(s.isEmpty())return false;
  if(id.equals("rouge")){
   if(s.getItem()==Items.DIAMOND||s.getItem()==Items.EMERALD)return true;
   for(int ore:OreDictionary.getOreIDs(s))if(OreDictionary.getOreName(ore).equals("gemSapphire"))return true;
   ResourceLocation r=s.getItem().getRegistryName();return r!=null&&(r.getResourcePath().equals("sapphire")||r.getResourcePath().equals("sapphire_gem"));
  }
  if(FoodContent.matchesFavorite(id,s))return true;
  switch(id){case "big_the_cat":return s.getItem()==Items.FISH;case "blaze_the_cat":return s.getItem()==FoodContent.ITEMS.get("grapes");case "espio":return s.getItem()==Items.MELON;case "charmy":return s.getItem()==FoodContent.ITEMS.get("chocolate_ice_cream");case "infinite":return s.getItem()==Items.COOKED_BEEF;default:return false;}
 }
 public static boolean interact(LegacyCharacter e,EntityPlayer p,EnumHand h){
  if(!mobian(e))return FoodContent.feed(e,p,h);
  ItemStack s=p.getHeldItem(h);UUID id=owner(e);
  if(favorite(e.definition().id,s)){
   if(id!=null&&!id.equals(p.getUniqueID())){if(!e.world.isRemote)message(p,"sonicraft.companion.other");return true;}
   if(!e.world.isRemote){
    if(!p.capabilities.isCreativeMode)s.shrink(1);
    if(id==null){
     boolean success=e.getRNG().nextInt(3)==0;
     particles(e,success?EnumParticleTypes.HEART:EnumParticleTypes.SMOKE_NORMAL);
     if(!success)return true;
     e.getEntityData().setUniqueId(OWNER,p.getUniqueID());e.getEntityData().setBoolean(FOLLOW,true);e.setAttackTarget(null);e.setRevengeTarget(null);e.getNavigator().clearPath();message(p,"sonicraft.companion.bond");
    }
    e.heal(4);e.world.playSound(null,e.posX,e.posY,e.posZ,SoundEvents.ENTITY_GENERIC_EAT,SoundCategory.NEUTRAL,.65f,1.0f);
   }return true;
  }
  // Sneak-right-click remains available for Carry On and Tails' transport.
  if(id!=null&&id.equals(p.getUniqueID())&&h==EnumHand.MAIN_HAND&&s.isEmpty()&&!p.isSneaking()){
   if(!e.world.isRemote){boolean follow=!follows(e);e.getEntityData().setBoolean(FOLLOW,follow);e.getNavigator().clearPath();message(p,follow?"sonicraft.companion.follow":"sonicraft.companion.wander");}return true;
  }
  return false;
 }
 static void message(EntityPlayer p,String key,Object... args){p.sendStatusMessage(new TextComponentTranslation(key,args),true);}
 static void particles(LegacyCharacter e,EnumParticleTypes type){if(e.world instanceof WorldServer)((WorldServer)e.world).spawnParticle(type,e.posX,e.posY+e.height*.7,e.posZ,7,.35,.3,.35,0);}
 public static void init(LegacyCharacter e){if(mobian(e)){e.tasks.addTask(2,new Follow(e));e.targetTasks.addTask(0,new Defend(e));}}
 static final class Follow extends EntityAIBase{
  final LegacyCharacter e;EntityPlayer p;int recalc;
  Follow(LegacyCharacter e){this.e=e;setMutexBits(3);}
  public boolean shouldExecute(){UUID id=owner(e);p=id==null?null:e.world.getPlayerEntityByUUID(id);return follows(e)&&p!=null&&p.isEntityAlive()&&!p.isSpectator()&&!e.isRiding()&&!e.isBeingRidden()&&e.getAttackTarget()==null&&e.getDistanceSq(p)>16;}
  public boolean shouldContinueExecuting(){return follows(e)&&p!=null&&p.isEntityAlive()&&!p.isSpectator()&&!e.isRiding()&&!e.isBeingRidden()&&e.getAttackTarget()==null&&e.getDistanceSq(p)>6.25;}
  public void startExecuting(){recalc=0;}
  public void resetTask(){p=null;e.getNavigator().clearPath();}
  public void updateTask(){e.getLookHelper().setLookPositionWithEntity(p,30,e.getVerticalFaceSpeed());if(--recalc<=0){recalc=10;e.getNavigator().tryMoveToEntityLiving(p,1.15);if(e.getDistanceSq(p)>144&&!e.getLeashed())safePosition(e,p,true);}}
 }
 static final class Defend extends EntityAIBase{
  final LegacyCharacter e;int hurt=-1,attack=-1;EntityLivingBase target;EntityPlayer p;
  Defend(LegacyCharacter e){this.e=e;setMutexBits(1);}
  public boolean shouldExecute(){UUID id=owner(e);p=id==null?null:e.world.getPlayerEntityByUUID(id);if(p==null||!follows(e)||p.isSpectator()||!p.isEntityAlive())return false;
   target=null;if(p.getRevengeTimer()!=hurt)target=p.getRevengeTarget();if(!valid(target)&&p.getLastAttackedEntityTime()!=attack)target=p.getLastAttackedEntity();return valid(target);}
  boolean valid(EntityLivingBase t){return t!=null&&t.isEntityAlive()&&t!=e&&!protectedTarget(e,t)&&e.getDistanceSq(t)<1024&&(!(t instanceof EntityPlayer)||p.canAttackPlayer((EntityPlayer)t));}
  public void startExecuting(){hurt=p.getRevengeTimer();attack=p.getLastAttackedEntityTime();e.setAttackTarget(target);}
  public boolean shouldContinueExecuting(){return false;}
 }
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void friendlyFire(LivingAttackEvent event){Entity source=event.getSource().getTrueSource();if(source instanceof LegacyCharacter&&protectedTarget((LegacyCharacter)source,event.getEntityLiving()))event.setCanceled(true);}
 // ItemEntity's tag format is Carry On 1.12.2's public storage format. No mandatory dependency.
 public static boolean carried(ItemStack s){if(s.isEmpty()||s.getItem().getRegistryName()==null||!s.getItem().getRegistryName().toString().equals("carryon:entity_item")||!s.hasTagCompound())return false;NBTTagCompound n=s.getTagCompound();String name=n.getString("entity");return n.hasKey("entityData",10)&&name.startsWith("sonicraft:")&&MOBIANS.contains(name.substring(10));}
 static void start(PlayerInteractEvent ev){
  EntityPlayer p=ev.getEntityPlayer();if(ev.getHand()!=EnumHand.MAIN_HAND||p.isSneaking()||!carried(p.getHeldItemMainhand()))return;
  ev.setCanceled(true);ev.setCancellationResult(EnumActionResult.SUCCESS);if(p.world.isRemote)return;
  NBTTagCompound tag=p.getHeldItemMainhand().getTagCompound();if(tag.hasKey(START))return;
  long now=p.world.getTotalWorldTime(),ready=tag.getCompoundTag("entityData").getCompoundTag("ForgeData").getLong(READY);
  tag.setBoolean("SC43HugReward",ready<=now);
  tag.setLong(START,now);p.inventory.markDirty();
 }
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void useItem(PlayerInteractEvent.RightClickItem e){start(e);}
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void useBlock(PlayerInteractEvent.RightClickBlock e){start(e);}
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void useEntity(PlayerInteractEvent.EntityInteract e){start(e);}
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void useSpecific(PlayerInteractEvent.EntityInteractSpecific e){start(e);}
 @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent ev){
  EntityPlayer p=ev.player;if(ev.phase!=TickEvent.Phase.END||p.world.isRemote)return;ItemStack s=p.getHeldItemMainhand();if(!carried(s)||!s.getTagCompound().hasKey(START))return;
  long elapsed=p.world.getTotalWorldTime()-s.getTagCompound().getLong(START);
  if(elapsed<0||elapsed>HUG_TICKS+20||!p.isEntityAlive()){s.getTagCompound().removeTag(START);return;}
  if(elapsed%8==0)p.world.playSound(null,p.posX,p.posY,p.posZ,SoundEvents.ENTITY_GENERIC_EAT,SoundCategory.PLAYERS,.35f,1.15f);
  if(elapsed<HUG_TICKS)return;
  // Remove transient state before any failure: failed releases never grant XP.
  s.getTagCompound().removeTag(START);
  Entity raw=EntityList.createEntityByIDFromName(new ResourceLocation(s.getTagCompound().getString("entity")),p.world);
  if(!(raw instanceof LegacyCharacter))return;LegacyCharacter e=(LegacyCharacter)raw;e.readFromNBT(s.getTagCompound().getCompoundTag("entityData").copy());
  if(!safePosition(e,p,false)){message(p,"sonicraft.companion.space");return;}
  java.lang.reflect.Method release;
  try{release=Class.forName("tschipp.carryon.common.event.ItemEvents").getMethod("sendPacket",EntityPlayer.class,int.class,int.class);}catch(ReflectiveOperationException ex){message(p,"sonicraft.companion.compat");return;}
  boolean reward=s.getTagCompound().getBoolean("SC43HugReward");
  if(reward)e.getEntityData().setLong(READY,p.world.getTotalWorldTime()+COOLDOWN_TICKS);e.motionX=e.motionY=e.motionZ=0;e.fallDistance=0;
  if(!p.world.spawnEntity(e)){message(p,"sonicraft.companion.space");return;}
  p.setHeldItem(EnumHand.MAIN_HAND,ItemStack.EMPTY);p.inventory.markDirty();
  try{release.invoke(null,p,9,0);}catch(ReflectiveOperationException ex){p.getEntityData().removeTag("carrySlot");p.getEntityData().removeTag("overrideKey");message(p,"sonicraft.companion.compat");}
  particles(e,reward?EnumParticleTypes.VILLAGER_HAPPY:EnumParticleTypes.SMOKE_NORMAL);
  if(reward)p.world.spawnEntity(new EntityXPOrb(p.world,e.posX,e.posY+.3,e.posZ,1+p.getRNG().nextInt(10)));
 }
 static boolean safePosition(LegacyCharacter e,EntityPlayer p,boolean teleport){
  double oldX=e.posX,oldY=e.posY,oldZ=e.posZ;BlockPos base=p.getPosition();
  for(int radius=1;radius<=3;radius++)for(int dy=0;dy<=2;dy++)for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){
   if(Math.max(Math.abs(x),Math.abs(z))!=radius)continue;BlockPos b=base.add(x,dy==2?-1:dy,z);
   if(!p.world.isBlockLoaded(b)||!p.world.getBlockState(b.down()).isTopSolid())continue;
   e.setPosition(b.getX()+.5,b.getY(),b.getZ()+.5);
   if(p.world.getWorldBorder().contains(e.getEntityBoundingBox())&&p.world.getCollisionBoxes(e,e.getEntityBoundingBox()).isEmpty()&&!p.world.containsAnyLiquid(e.getEntityBoundingBox())&&p.world.checkNoEntityCollision(e.getEntityBoundingBox(),e)){
    if(teleport)e.setPositionAndUpdate(e.posX,e.posY,e.posZ);e.getNavigator().clearPath();return true;}
  }e.setPosition(oldX,oldY,oldZ);return false;
 }
}
