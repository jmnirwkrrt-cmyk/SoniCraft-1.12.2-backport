package org.sonicraft.backport;
import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.entity.player.EntityPlayer;import net.minecraft.util.*;import net.minecraft.util.math.BlockPos;import net.minecraft.util.text.TextComponentTranslation;import net.minecraft.world.World;import java.util.Random;
public final class Magnet43 extends StructureFacadeBlocks.Horizontal {
 final String id;
 public Magnet43(Content.BlockDef d){super(d);id=d.id;}
 public boolean isOpaqueCube(IBlockState s){return false;}public boolean isFullCube(IBlockState s){return false;}
 public boolean onBlockActivated(World w,BlockPos p,IBlockState s,EntityPlayer player,EnumHand h,EnumFacing f,float x,float y,float z){
  if(!id.equals("death_egg_magnet_button"))return true;if(w.isRemote)return true;
  if(w.getBlockState(p.up(3)).getBlock()!=Content.BLOCKS.get("magnet_piece")||!tips(w,p)) {player.sendStatusMessage(new TextComponentTranslation("sonicraft.magnet.incomplete"),true);return true;}
  StructurePort.Template t=StructurePort.get("death_egg");if(p.getY()+48+t.height>=255){player.sendStatusMessage(new TextComponentTranslation("sonicraft.magnet.height"),true);return true;}
  Block on=Content.BLOCKS.get("death_egg_magnet_button_on");w.setBlockState(p,on.getStateFromMeta(getMetaFromState(s)),3);w.scheduleUpdate(p,on,120);w.playSound(null,p,net.minecraft.init.SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON,SoundCategory.BLOCKS,1,1);return true;
 }
 static boolean tips(World w,BlockPos p){Block tip=Content.BLOCKS.get("magnet_tip");return (w.getBlockState(p.up(7).east(2)).getBlock()==tip&&w.getBlockState(p.up(7).west(2)).getBlock()==tip)||(w.getBlockState(p.up(7).north(2)).getBlock()==tip&&w.getBlockState(p.up(7).south(2)).getBlock()==tip);}
 public void onBlockAdded(World w,BlockPos p,IBlockState s){if(!w.isRemote&&id.equals("death_egg_magnet_button_on"))w.scheduleUpdate(p,this,120);}
 public void updateTick(World w,BlockPos p,IBlockState s,Random r){if(w.isRemote||!id.equals("death_egg_magnet_button_on"))return;
  StructurePort.Template t=StructurePort.get("death_egg");BlockPos at=p.add(-15,48,-15);
  if(at.getY()+t.height<255){for(int x=at.getX()>>4;x<=(at.getX()+t.width-1)>>4;x++)for(int z=at.getZ()>>4;z<=(at.getZ()+t.depth-1)>>4;z++)w.getChunkFromChunkCoords(x,z);
   if(StructurePort.place(w,at,t,Rotation.NONE,true)){Block damaged=Content.BLOCKS.get("damaged_death_egg_magnet_button");w.setBlockState(p,damaged.getStateFromMeta(getMetaFromState(s)),3);return;}}
  w.setBlockState(p,Content.BLOCKS.get("death_egg_magnet_button").getStateFromMeta(getMetaFromState(s)),3);
  for(EntityPlayer player:w.playerEntities)if(player.getDistanceSq(p)<4096)player.sendStatusMessage(new TextComponentTranslation("sonicraft.magnet.blocked"),true);
 }
}
