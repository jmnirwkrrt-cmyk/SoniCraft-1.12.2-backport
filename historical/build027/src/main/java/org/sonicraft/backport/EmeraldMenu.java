package org.sonicraft.backport;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.*;
import net.minecraftforge.fml.common.network.simpleimpl.*;
import net.minecraftforge.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
public final class EmeraldMenu implements IGuiHandler {
 static final SimpleNetworkWrapper NET=NetworkRegistry.INSTANCE.newSimpleChannel("sonicraft_gems");
 static void register(){NET.registerMessage(OpenHandler.class,Open.class,0,Side.SERVER);NetworkRegistry.INSTANCE.registerGuiHandler(SonicraftLegacy.instance,new EmeraldMenu());}
 @Override public Object getServerGuiElement(int id,EntityPlayer p,World w,int x,int y,int z){return id==0?new Collection(p):null;}
 @Override public Object getClientGuiElement(int id,EntityPlayer p,World w,int x,int y,int z){return id==0?SonicraftLegacy.proxy.emeraldGui(p):null;}
 public static final class Open implements IMessage {public void fromBytes(ByteBuf b){}public void toBytes(ByteBuf b){}}
 public static final class OpenHandler implements IMessageHandler<Open,IMessage>{public IMessage onMessage(Open m,MessageContext c){final EntityPlayerMP p=c.getServerHandler().player;p.getServerWorld().addScheduledTask(()->{if(p.isEntityAlive()&&!p.isSpectator())p.openGui(SonicraftLegacy.instance,0,p.world,0,0,0);});return null;}}
 public static final class Collection extends Container {
  final EntityPlayer owner;public int stored,inventory;
  Collection(EntityPlayer p){owner=p;}
  @Override public boolean canInteractWith(EntityPlayer p){return p==owner&&p.isEntityAlive()&&!p.isSpectator();}
  @Override public void addListener(IContainerListener l){super.addListener(l);l.sendWindowProperty(this,0,EmeraldProgression.stored(owner));l.sendWindowProperty(this,1,EmeraldProgression.inventory(owner));}
  @Override public void detectAndSendChanges(){super.detectAndSendChanges();int s=EmeraldProgression.stored(owner),i=EmeraldProgression.inventory(owner);for(IContainerListener l:listeners){if(stored!=s)l.sendWindowProperty(this,0,s);if(inventory!=i)l.sendWindowProperty(this,1,i);}stored=s;inventory=i;}
  @Override public void updateProgressBar(int id,int value){if(id==0)stored=value&127;if(id==1)inventory=value&127;}
  @Override public boolean enchantItem(EntityPlayer p,int id){return canInteractWith(p)&&EmeraldProgression.withdraw(p,id);}
  @Override public ItemStack transferStackInSlot(EntityPlayer p,int index){return ItemStack.EMPTY;}
 }
}
