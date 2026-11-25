package org.sonicraft.backport;
public class ClientProxy extends CommonProxy {@Override public void preInit(){CharacterRenderers.register();BadnikRenderers.register();ClientEmeraldMenu.register();}@Override public Object emeraldGui(net.minecraft.entity.player.EntityPlayer p){return new ClientEmeraldMenu(p);}}
