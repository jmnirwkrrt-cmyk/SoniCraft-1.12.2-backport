from pathlib import Path
p=Path('p42/src/org/sonicraft/backport/Companions42.java')
s=p.read_text().replace('COOLDOWN_TICKS=6000','COOLDOWN_TICKS=1200')
s=s.replace('if(id.equals(t.getUniqueID()))return true;','if(t instanceof EntityPlayer)return true;')
s=s.replace('public static void init(LegacyCharacter e){if(mobian(e)){e.tasks.addTask(3,new Follow(e));e.targetTasks.addTask(0,new Defend(e));}}','public static void init(LegacyCharacter e,net.minecraft.entity.ai.EntityAITasks tasks,net.minecraft.entity.ai.EntityAITasks targets){if(mobian(e)){tasks.addTask(4,new Follow(e));targets.addTask(0,new Defend(e));}}')
s=s.replace('if(!mobian(e))return FoodContent.feed(e,p,h);','if(!mobian(e))return FoodContent.feed(e,p,h);\n  if(p.isSpectator())return false;')
s=s.replace('if(p==null||!follows(e)||p.isSpectator()','if(p==null||p.isSpectator()')
start=s.index(' static void start(PlayerInteractEvent ev)')
end=s.index(' static boolean safePosition', start)
s=s[:start]+'''
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
''' + s[end:]
p.write_text(s)
p=Path('p42/src/org/sonicraft/backport/CuddleRender42.java')
s=p.read_text()
s=s.replace('!s.getTagCompound().hasKey(Companions42.START)','!mc.player.isHandActive()||mc.player.getActiveItemStack()!=s')
s=s.replace('mc.world.getTotalWorldTime()-s.getTagCompound().getLong(Companions42.START)+event.getPartialTicks()','Companions42.HUG_TICKS-mc.player.getItemInUseCount()+event.getPartialTicks()')
p.write_text(s)
