package org.sonicraft.backport;
import java.util.Set;import io.netty.buffer.ByteBuf;import net.minecraft.entity.Entity;import net.minecraft.entity.player.*;import net.minecraft.item.*;import net.minecraft.util.*;import net.minecraft.util.math.BlockPos;import net.minecraft.world.World;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.event.RegistryEvent;import net.minecraftforge.fml.common.registry.*;import net.minecraftforge.fml.common.network.NetworkRegistry;import net.minecraftforge.fml.common.network.simpleimpl.*;import net.minecraftforge.fml.relauncher.Side;
@Mod.EventBusSubscriber(modid="sonicraft")
public class Vehicles47 {
 public static final SimpleNetworkWrapper NET=NetworkRegistry.INSTANCE.newSimpleChannel("sonicraft_flight");
 public static final Item PLANE=new PlaneItem().setRegistryName("sonicraft","tornado_item").setUnlocalizedName("sonicraft.tornado_item").setMaxStackSize(1).setCreativeTab(net.minecraft.creativetab.CreativeTabs.TRANSPORTATION);
 static {NET.registerMessage(ControlHandler.class,Control.class,0,Side.SERVER);}
 @SubscribeEvent public static void items(RegistryEvent.Register<Item> e){e.getRegistry().register(PLANE);}
 @SubscribeEvent public static void entities(RegistryEvent.Register<EntityEntry> e){e.getRegistry().register(EntityEntryBuilder.create().entity(Tornado47.class).id(new ResourceLocation("sonicraft","tornado"),465).name("sonicraft.tornado").tracker(128,1,true).build());}
 public static boolean safe(Set<?> s,Object v){return "sonicraft:tornado".equals(v)||Entities46.safe(s,v);}
 public static class PlaneItem extends Item {
  public EnumActionResult onItemUse(EntityPlayer p,World w,BlockPos pos,EnumHand hand,EnumFacing face,float x,float y,float z){BlockPos at=pos.offset(face);if(!p.canPlayerEdit(at,face,p.getHeldItem(hand)))return EnumActionResult.FAIL;if(w.isRemote)return EnumActionResult.SUCCESS;Tornado47 plane=new Tornado47(w);plane.setLocationAndAngles(at.getX()+.5,at.getY(),at.getZ()+.5,p.rotationYaw,0);
   if(!w.getCollisionBoxes(plane,plane.getEntityBoundingBox()).isEmpty()||!w.checkNoEntityCollision(plane.getEntityBoundingBox(),plane))return EnumActionResult.FAIL;
   ItemStack stack=p.getHeldItem(hand);if(stack.hasTagCompound()&&stack.getTagCompound().hasKey("PlaneHealth"))plane.setHealth(Math.max(1,Math.min(80,stack.getTagCompound().getFloat("PlaneHealth"))));
   if(!w.spawnEntity(plane))return EnumActionResult.FAIL;if(!p.capabilities.isCreativeMode)stack.shrink(1);return EnumActionResult.SUCCESS;
  }
 }
 public static class Control implements IMessage {
  int id;float throttle,vertical;
  public Control(){}public Control(int id,float throttle,float vertical){this.id=id;this.throttle=throttle;this.vertical=vertical;}
  public void fromBytes(ByteBuf b){id=b.readInt();throttle=b.readFloat();vertical=b.readFloat();}public void toBytes(ByteBuf b){b.writeInt(id);b.writeFloat(throttle);b.writeFloat(vertical);}
 }
 public static class ControlHandler implements IMessageHandler<Control,IMessage> {
  public IMessage onMessage(Control m,MessageContext ctx){EntityPlayerMP p=ctx.getServerHandler().player;p.getServerWorld().addScheduledTask(()->{Entity e=p.getRidingEntity();if(!(e instanceof Tornado47)||e.getEntityId()!=m.id||e.getControllingPassenger()!=p||!Float.isFinite(m.throttle)||!Float.isFinite(m.vertical))return;Tornado47 plane=(Tornado47)e;plane.throttle=net.minecraft.util.math.MathHelper.clamp(m.throttle,-1,1);plane.vertical=net.minecraft.util.math.MathHelper.clamp(m.vertical,-1,1);plane.lastInput=plane.ticksExisted;});return null;}
 }
}
