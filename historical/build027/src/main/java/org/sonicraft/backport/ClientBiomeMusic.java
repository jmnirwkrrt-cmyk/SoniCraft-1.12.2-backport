package org.sonicraft.backport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
/** One streamed ambient loop; independent of Minecraft's random music timer. */
@Mod.EventBusSubscriber(modid="sonicraft",value=Side.CLIENT)
public final class ClientBiomeMusic {
 private static Loop active;private static World world;private static int retry;
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event){
  if(event.phase!=TickEvent.Phase.END)return;Minecraft mc=Minecraft.getMinecraft();String wanted=null;
  if(SonicraftLegacy.biomeMusic&&mc.world!=null&&mc.player!=null){Biome b=mc.world.getBiome(mc.player.getPosition());if(b instanceof ZoneBiome)wanted=BiomeMusic.track(((ZoneBiome)b).definition.id);}
  if(active!=null&&(mc.world!=world||!active.key.equals(wanted))){active.done();mc.getSoundHandler().stopSound(active);active=null;retry=0;}
  world=mc.world;if(wanted==null||mc.isGamePaused())return;
  if(retry>0)--retry;
  if(active!=null&&retry==0&&!mc.getSoundHandler().isSoundPlaying(active)){active.done();active=null;}
  if(active==null){active=new Loop(wanted);mc.getSoundHandler().playSound(active);retry=40;}
 }
 static final class Loop extends MovingSound {
  final String key;
  Loop(String key){super(BiomeMusic.SOUNDS.get(key),SoundCategory.AMBIENT);this.key=key;repeat=true;repeatDelay=0;volume=1;attenuationType=ISound.AttenuationType.NONE;}
  void done(){donePlaying=true;}
  @Override public void update(){Minecraft mc=Minecraft.getMinecraft();if(mc.player!=null){xPosF=(float)mc.player.posX;yPosF=(float)mc.player.posY;zPosF=(float)mc.player.posZ;}}
 }
}
