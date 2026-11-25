package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.init.*;
import net.minecraft.util.math.*;
import net.minecraft.world.biome.*;
import net.minecraft.world.chunk.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class MobiusZonesCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static class World extends MobiusPortalCheck.TerrainWorld {
  BiomeProvider selected;
  @Override public BiomeProvider getBiomeProvider(){return selected==null?super.getBiomeProvider():selected;}
 }
 public static void main(String[] args){
  Bootstrap.register();for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);ForgeRegistries.BLOCKS.register(b);}
  for(Content.BiomeDef d:Content.BIOME_DEFS){ZoneBiome b=new ZoneBiome(d);b.setRegistryName("sonicraft",d.id);Content.BIOMES.put(d.id,b);ForgeRegistries.BIOMES.register(b);}
  MobiusWorld.TYPE=net.minecraft.world.DimensionType.register("Zones Test","_zones_test",11230,MobiusWorld.class,false);
  MobiusWorld.Provider legacy=new MobiusWorld.Provider(234,false);Set<String> old=new HashSet<>();
  for(int x=-1024;x<=1024;x+=64)for(Biome b:legacy.getBiomes(null,x,-1024,32,32))old.add(b.getRegistryName().getResourcePath());
  require(old.equals(new HashSet<>(Arrays.asList(MobiusWorld.LEGACY_ZONES))),"Legacy palette excludes all new zones");
  int count=0;
  for(Content.BiomeDef d:Content.BIOME_DEFS)if(d.industrial){
   count++;ZoneBiome biome=Content.BIOMES.get(d.id);require(BiomeMusic.TRACKS.containsKey(d.id),"Track "+d.id);
   require(biome.getSpawnableList(net.minecraft.entity.EnumCreatureType.CREATURE).isEmpty(),"No default livestock "+d.id);
   World w=new World();w.selected=new BiomeProviderSingle(biome);net.minecraft.world.gen.ChunkGeneratorOverworld generator=new net.minecraft.world.gen.ChunkGeneratorOverworld(w,234,false,"");Chunk c=generator.generateChunk(0,0);
   int top=0,filler=0;for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=1;y<255;y++){Block b=c.getBlockState(new BlockPos(x,y,z)).getBlock();if(b==Content.BLOCKS.get(d.top))top++;if(b==Content.BLOCKS.get(d.filler))filler++;}
   require(top>0&&filler>0,"Real terrain uses source surfaces "+d.id+" "+top+"/"+filler);
   ZoneTerrain.replace(c);for(int x=0;x<16;x++)for(int z=0;z<16;z++)for(int y=1;y<255;y++)require(c.getBlockState(new BlockPos(x,y,z)).getBlock()!=Blocks.STONE,"Post-carving substrate conversion "+d.id);
  }
  require(count==8&&Content.BIOME_DEFS.length==20&&MobiusWorld.ZONES.length==16,"Full source Mobius palette");
  Block scaffold=Content.BLOCKS.get("spring_yard_scaffolding");require(!scaffold.isOpaqueCube(scaffold.getDefaultState())&&scaffold.getCollisionBoundingBox(scaffold.getDefaultState(),new FoodContentCheck.TestWorld(),BlockPos.ORIGIN).equals(Block.FULL_BLOCK_AABB),"Source scaffold transparency/collision");
  Block oil=Content.BLOCKS.get("hardened_oil");require(oil.slipperiness==1&&oil.getFlammability(new FoodContentCheck.TestWorld(),BlockPos.ORIGIN,net.minecraft.util.EnumFacing.UP)==5,"Oil source properties");
  FoodContentCheck.TestWorld oilWorld=new FoodContentCheck.TestWorld();FoodContentCheck.Player player=new FoodContentCheck.Player(oilWorld);player.setPosition(.5,64,.5);oilWorld.blocks.put(new BlockPos(0,63,0),oil.getDefaultState());player.motionY=.42;IndustrialBlocks.jump(new net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent(player));require(Math.abs(player.motionY-.378)<.0001,"Oil jump factor");
  player.addPotionEffect(new net.minecraft.potion.PotionEffect(MobEffects.JUMP_BOOST,200,1));player.motionY=.62;IndustrialBlocks.jump(new net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent(player));require(Math.abs(player.motionY-.578)<.0001,"Preserve jump boost bonus");
  for(Content.BlockDef d:Content.BLOCK_DEFS)if(d.resistance>0)require(Math.abs(Content.BLOCKS.get(d.id).getExplosionResistance(null)-d.resistance)<.001,"Explosion resistance units "+d.id);
  System.out.println("PASS: all eight new zones generate real chunks with source top/filler and chaostone; 20 tracks/biomes, 16-zone Mobius and legacy eight-zone palette; scaffold/oil properties");
 }
}
