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
 public static final int HUG_TICKS=40, COOLDOWN_TICKS=1200;
 static final Set<String> MOBIANS=new HashSet<>(Arrays.asList("sonic","tails","knuckles","amy_rose","rouge","shadow","silver","big_the_cat","espio","charmy","vector","infinite","blaze_the_cat","cream"));
 static boolean mobian(LegacyCharacter e){return MOBIANS.contains(e.definition().id);}
 static UUID owner(LegacyCharacter e){NBTTagCompound n=e.getEntityData();return n.hasUniqueId(OWNER)?n.getUniqueId(OWNER):null;}
 static boolean follows(LegacyCharacter e){return e.getEntityData().getBoolean(FOLLOW);}
 static boolean ownedBy(LegacyCharacter e,EntityPlayer p){return p.getUniqueID().equals(owner(e));}
 public static boolean protectedTarget(LegacyCharacter e,Entity t){
  UUID id=owner(e);if(id==null||t==null)return false;
  if(t instanceof EntityPlayer)return true;
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
  if(p.isSpectator())return false;
  ItemStack s=p.getHeldItem(h);UUID id=owner(e);
  if(favorite(e.definition().id,s)){
   if(id!=null&&!id.equals(p.getUniqueID())){if(!e.world.isRemote)message(p,"sonicraft.companion.other");return true;}
   if(!e.world.isRemote){
    if(id==null){e.getEntityData().setUniqueId(OWNER,p.getUniqueID());e.getEntityData().setBoolean(FOLLOW,true);e.setAttackTarget(null);e.setRevengeTarget(null);e.getNavigator().clearPath();message(p,"sonicraft.companion.bond");}
    e.heal(4);if(!p.capabilities.isCreativeMode)s.shrink(1);hearts(e);e.world.playSound(null,e.posX,e.posY,e.posZ,SoundEvents.ENTITY_GENERIC_EAT,SoundCategory.NEUTRAL,.65f,1.0f);
   }return true;
  }
  // Sneak-right-click remains available for Carry On and Tails' transport.
  if(id!=null&&id.equals(p.getUniqueID())&&h==EnumHand.MAIN_HAND&&s.isEmpty()&&!p.isSneaking()){
   if(!e.world.isRemote){boolean follow=!follows(e);e.getEntityData().setBoolean(FOLLOW,follow);e.getNavigator().clearPath();message(p,follow?"sonicraft.companion.follow":"sonicraft.companion.wander");}return true;
  }
  return false;
 }
 static void message(EntityPlayer p,String key,Object... args){p.sendStatusMessage(new TextComponentTranslation(key,args),true);}
 static void hearts(LegacyCharacter e){if(e.world instanceof WorldServer)((WorldServer)e.world).spawnParticle(EnumParticleTypes.HEART,e.posX,e.posY+e.height*.7,e.posZ,6,.35,.3,.35,0);}
 public static void init(LegacyCharacter e,net.minecraft.entity.ai.EntityAITasks tasks,net.minecraft.entity.ai.EntityAITasks targets){if(mobian(e)){tasks.addTask(4,new Follow(e));targets.addTask(0,new Defend(e));}}
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
  public boolean shouldExecute(){UUID id=owner(e);p=id==null?null:e.world.getPlayerEntityByUUID(id);if(p==null||p.isSpectator()||!p.isEntityAlive())return false;
   target=null;if(p.getRevengeTimer()!=hurt)target=p.getRevengeTarget();if(!valid(target)&&p.getLastAttackedEntityTime()!=attack)target=p.getLastAttackedEntity();return valid(target);}
  boolean valid(EntityLivingBase t){return t!=null&&t.isEntityAlive()&&t!=e&&!protectedTarget(e,t)&&e.getDistanceSq(t)<1024&&(!(t instanceof EntityPlayer)||p.canAttackPlayer((EntityPlayer)t));}
  public void startExecuting(){hurt=p.getRevengeTimer();attack=p.getLastAttackedEntityTime();e.setAttackTarget(target);}
  public boolean shouldContinueExecuting(){return false;}
 }
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void friendlyFire(LivingAttackEvent event){Entity source=event.getSource().getTrueSource();if(source instanceof LegacyCharacter&&protectedTarget((LegacyCharacter)source,event.getEntityLiving()))event.setCanceled(true);}
 // ItemEntity's tag format is Carry On 1.12.2's public storage format. No mandatory dependency.
 public static boolean carried(ItemStack s){if(s.isEmpty()||s.getItem().getRegistryName()==null||!s.getItem().getRegistryName().toString().equals("carryon:entity_item")||!s.hasTagCompound())return false;NBTTagCompound n=s.getTagCompound();String name=n.getString("entity");return n.hasKey("entityData",10)&&name.startsWith("sonicraft:")&&MOBIANS.contains(name.substring(10));}

 static long now(EntityPlayer p){return p.getServer()==null?p.world.getTotalWorldTime():p.getServer().getWorld(0).getTotalWorldTime();}
 static NBTTagCompound persisted(EntityPlayer p){NBTTagCompound n=p.getEntityData();if(!n.hasKey(EntityPlayer.PERSISTED_NBT_TAG,10))n.setTag(EntityPlayer.PERSISTED_NBT_TAG,new NBTTagCompound());return n.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);}
 static long remaining(long now,long ready){long d=ready-now;return d>0?Math.min(d,COOLDOWN_TICKS):0;}
 public static ActionResult<ItemStack> start(net.minecraft.world.World w,EntityPlayer p,EnumHand h){
  ItemStack s=p.getHeldItem(h);
  if(h!=EnumHand.MAIN_HAND||p.isSneaking()||p.isSpectator()||!carried(s))return new ActionResult<>(EnumActionResult.PASS,s);
  if(!w.isRemote){
   long ready=Math.max(s.getTagCompound().getCompoundTag("entityData").getCompoundTag("ForgeData").getLong(READY),persisted(p).getLong(READY));
   long wait=remaining(now(p),ready);
   if(wait>0){message(p,"sonicraft.companion.cooldown",(wait+19)/20);return new ActionResult<>(EnumActionResult.FAIL,s);}
  }
  p.setActiveHand(h);return new ActionResult<>(EnumActionResult.SUCCESS,s);
 }
 public static EnumAction action(ItemStack s){return carried(s)?EnumAction.EAT:EnumAction.NONE;}
 public static int duration(ItemStack s){return carried(s)?HUG_TICKS:0;}
 public static ItemStack finish(ItemStack s,net.minecraft.world.World w,EntityLivingBase user){
  if(w.isRemote||!(user instanceof EntityPlayer)||!carried(s))return s;
  EntityPlayer p=(EntityPlayer)user;
  if(!p.isEntityAlive()||p.getHeldItemMainhand()!=s)return s;
  long time=now(p);
  long ready=Math.max(s.getTagCompound().getCompoundTag("entityData").getCompoundTag("ForgeData").getLong(READY),persisted(p).getLong(READY));
  if(remaining(time,ready)>0)return s;
  Entity raw=EntityList.createEntityByIDFromName(new ResourceLocation(s.getTagCompound().getString("entity")),w);
  if(!(raw instanceof LegacyCharacter))return s;
  LegacyCharacter e=(LegacyCharacter)raw;
  e.readFromNBT(s.getTagCompound().getCompoundTag("entityData").copy());
  if(!e.isEntityAlive()||!(w instanceof WorldServer)||((WorldServer)w).getEntityFromUuid(e.getUniqueID())!=null)return s;
  if(!safePosition(e,p,false)){message(p,"sonicraft.companion.space");return s;}
  java.lang.reflect.Method release;
  try{release=Class.forName("tschipp.carryon.common.event.ItemEvents").getMethod("sendPacket",EntityPlayer.class,int.class,int.class);}
  catch(ReflectiveOperationException ex){message(p,"sonicraft.companion.compat");return s;}
  e.getEntityData().setLong(READY,time+COOLDOWN_TICKS);e.motionX=e.motionY=e.motionZ=0;e.fallDistance=0;
  if(!w.spawnEntity(e)){message(p,"sonicraft.companion.space");return s;}
  // Commit only after successful spawn. This method is called by vanilla's use-item lifecycle.
  persisted(p).setLong(READY,time+COOLDOWN_TICKS);
  p.getEntityData().removeTag("carrySlot");p.getEntityData().removeTag("overrideKey");
  try{release.invoke(null,p,9,0);}catch(ReflectiveOperationException ex){message(p,"sonicraft.companion.compat");}
  s.getTagCompound().removeTag("entityData");s.getTagCompound().removeTag("entity");
  hearts(e);w.spawnEntity(new EntityXPOrb(w,e.posX,e.posY+.3,e.posZ,1+p.getRNG().nextInt(10)));
  return ItemStack.EMPTY;
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
