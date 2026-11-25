package org.sonicraft.backport;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
@Mod.EventBusSubscriber(modid=SonicraftLegacy.ID,value=Side.CLIENT)
public final class ClientEmeraldMenu extends GuiContainer {
 static final KeyBinding KEY=new KeyBinding("key.sonicraft.emeralds",Keyboard.KEY_K,"SoniCraft");
 static void register(){ClientRegistry.registerKeyBinding(KEY);}
 @SubscribeEvent public static void key(InputEvent.KeyInputEvent e){if(KEY.isPressed()&&Minecraft.getMinecraft().player!=null&&Minecraft.getMinecraft().currentScreen==null)EmeraldMenu.NET.sendToServer(new EmeraldMenu.Open());}
 ClientEmeraldMenu(EntityPlayer p){super(new EmeraldMenu.Collection(p));xSize=282;ySize=210;}
 @Override public void initGui(){super.initGui();for(int i=0;i<7;i++)buttonList.add(new GuiButton(i,guiLeft+205,guiTop+31+i*24,68,20,I18n.format("sonicraft.emerald.withdraw")));}
 @Override protected void actionPerformed(GuiButton b)throws IOException {mc.playerController.sendEnchantPacket(inventorySlots.windowId,b.id);}
 @Override protected void drawGuiContainerBackgroundLayer(float partial,int x,int y){drawRect(guiLeft,guiTop,guiLeft+xSize,guiTop+ySize,0xff20242c);EmeraldMenu.Collection c=(EmeraldMenu.Collection)inventorySlots;for(GuiButton b:buttonList)b.enabled=(c.stored&(1<<b.id))!=0&&(c.inventory&(1<<b.id))==0;}
 @Override protected void drawGuiContainerForegroundLayer(int x,int y){fontRenderer.drawString(I18n.format("sonicraft.emerald.title"),10,10,0xffffff);EmeraldMenu.Collection c=(EmeraldMenu.Collection)inventorySlots;for(int i=0;i<7;i++){ItemStack item=new ItemStack(EmeraldProgression.emerald(i));itemRender.renderItemAndEffectIntoGUI(item,8,32+i*24);fontRenderer.drawString(item.getDisplayName(),30,30+i*24,0xffffff);String key=(c.inventory&(1<<i))!=0?"inventory":(c.stored&(1<<i))!=0?"stored":"missing";fontRenderer.drawString(I18n.format("sonicraft.emerald."+key),30,40+i*24,0xaaaaaa);}}
}
