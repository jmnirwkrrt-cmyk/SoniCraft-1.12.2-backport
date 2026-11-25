package org.sonicraft.backport;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public final class ZoneFlora {
 static final class Choice {String id,soil;float chance;}
 static final class Patch {String biome,source;int tries,xz,y;Choice[] choices;}
 static final Patch[] PATCHES=Content.read("flora.json",Patch[].class);
 static Choice choose(Patch patch,Random r){
  for(Choice c:patch.choices)if(c.chance>=1||r.nextFloat()<c.chance)return c;
  return null; // Source random_selector defaults to no_op.
 }
 static boolean place(World w,BlockPos p,String biome,Choice c){
  if(c==null||p.getY()<1||p.getY()>255||!w.isBlockLoaded(p)||!w.isAirBlock(p))return false;
  Biome actual=w.getBiome(p);
  if(!(actual instanceof ZoneBiome)||!((ZoneBiome)actual).definition.id.equals(biome))return false;
  if(w.getBlockState(p.down()).getBlock()!=Content.BLOCKS.get(c.soil))return false;
  Block plant=Content.BLOCKS.get(c.id);
  return plant instanceof TallZoneFlower?((TallZoneFlower)plant).place(w,p):w.setBlockState(p,plant.getDefaultState(),2);
 }
 static void decorate(World w,Random r,BlockPos chunk,String biome){
  for(Patch patch:PATCHES)if(patch.biome.equals(biome)){
   BlockPos origin=chunk.add(8+r.nextInt(16),0,8+r.nextInt(16));if(!w.isBlockLoaded(origin))continue;origin=w.getHeight(origin);
   for(int i=0;i<patch.tries;i++){
    BlockPos p=origin.add(r.nextInt(patch.xz+1)-r.nextInt(patch.xz+1),r.nextInt(patch.y+1)-r.nextInt(patch.y+1),r.nextInt(patch.xz+1)-r.nextInt(patch.xz+1));
    place(w,p,biome,choose(patch,r));
   }
  }
 }
}
