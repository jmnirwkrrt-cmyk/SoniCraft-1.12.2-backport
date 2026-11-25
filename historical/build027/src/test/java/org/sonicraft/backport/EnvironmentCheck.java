package org.sonicraft.backport;
import java.util.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.*;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class EnvironmentCheck {
 static void require(boolean b,String m){if(!b)throw new AssertionError(m);}
 static class World extends FoodContentCheck.TestWorld {
  Chunk generated;
  @Override public Chunk getChunkFromChunkCoords(int x,int z){return generated;}
  @Override protected boolean isChunkLoaded(int x,int z,boolean empty){return true;}
  @Override public IBlockState getBlockState(BlockPos p){return blocks.containsKey(p)?blocks.get(p):p.getY()==63?Content.BLOCKS.get("marble_zone_grass").getDefaultState():Blocks.AIR.getDefaultState();}
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);ForgeRegistries.BLOCKS.register(b);ForgeRegistries.ITEMS.register(new ItemBlock(b).setRegistryName(b.getRegistryName()));}
  ForgeRegistries.ITEMS.register(MobiusPortal.IGNITER);
  World w=new World();FoodContentCheck.Player player=new FoodContentCheck.Player(w);player.setHeldItem(EnumHand.MAIN_HAND,new ItemStack(MobiusPortal.IGNITER));BlockPos p=new BlockPos(0,64,0);
  require(!MobiusPortal.placeRing(player,w,p,EnumHand.MAIN_HAND),"No free ring in survival");player.inventory.setInventorySlotContents(1,new ItemStack(Content.BLOCKS.get("gold_ring"),2));
  require(MobiusPortal.placeRing(player,w,p,EnumHand.MAIN_HAND)&&player.inventory.getStackInSlot(1).getCount()==1,"Place and consume ring");require(!MobiusPortal.placeRing(player,w,p,EnumHand.MAIN_HAND),"Do not overwrite");
  player.inventory.clear();player.capabilities.isCreativeMode=true;require(MobiusPortal.placeRing(player,w,p.east(),EnumHand.MAIN_HAND),"Creative ring without inventory supply");
  for(ZoneStructures.Template t:ZoneStructures.PILLARS){World test=new World();require(ZoneStructures.place(test,p,t),"Pillar "+t.id);for(Object[] c:t.cells){BlockPos pos=p.add(((Number)c[0]).intValue(),((Number)c[1]).intValue()-1,((Number)c[2]).intValue());require(test.getBlockState(pos).getBlock()==Content.BLOCKS.get((String)c[3]),"Exact source palette and coordinate");}
   World blocked=new World();blocked.blocks.put(p,Blocks.STONE.getDefaultState());require(!ZoneStructures.place(blocked,p,t)&&blocked.blocks.size()==1,"Atomic placement preflight");}
  require(BiomeMusic.TRACKS.size()==20,"All zone tracks");for(String name:BiomeMusic.TRACKS.values()){ClientBiomeMusic.Loop sound=new ClientBiomeMusic.Loop(name);require(sound.getCategory()==SoundCategory.AMBIENT&&sound.canRepeat(),"Independent ambient loop");try(java.io.InputStream in=EnvironmentCheck.class.getResourceAsStream("/assets/sonicraft/sounds/"+new com.google.gson.JsonParser().parse(CharacterSmokeCheck.resource("sounds.json")).getAsJsonObject().getAsJsonObject(name).getAsJsonArray("sounds").get(0).getAsJsonObject().get("name").getAsString().split(":")[1]+".ogg")){require(in!=null&&in.read()=='O'&&in.read()=='g'&&in.read()=='g'&&in.read()=='S',"Original Ogg");}}
  software.bernie.geckolib3.geo.render.built.GeoBone head=new software.bernie.geckolib3.geo.render.built.GeoBone();CharacterRenderers.look(head,-30,45);require(Math.abs(head.getRotationX()+Math.PI/6)<.001&&Math.abs(head.getRotationY()-Math.PI/4)<.001,"Head pitch/yaw");CharacterRenderers.look(head,0,0);require(head.getRotationX()==0&&head.getRotationY()==0,"Head reset");
  Content.BiomeDef def=Content.BIOME_DEFS[0];ZoneBiome biome=new ZoneBiome(def);biome.setRegistryName("sonicraft",def.id);ForgeRegistries.BIOMES.register(biome);
  ChunkPrimer primer=new ChunkPrimer();primer.setBlockState(0,20,0,Blocks.STONE.getDefaultState());primer.setBlockState(0,21,0,Blocks.DIAMOND_ORE.getDefaultState());primer.setBlockState(1,20,0,Blocks.STONE.getDefaultState());Chunk chunk=new Chunk(w,primer,0,0);Arrays.fill(chunk.getBiomeArray(),(byte)Biome.getIdForBiome(Biomes.PLAINS));chunk.getBiomeArray()[0]=(byte)Biome.getIdForBiome(biome);w.generated=chunk;ZoneTerrain listener=new ZoneTerrain();listener.beforePopulation(new net.minecraftforge.event.terraingen.PopulateChunkEvent.Pre(null,w,new Random(1),0,0,false));
  require(chunk.getBlockState(new BlockPos(0,20,0)).getBlock()==Content.BLOCKS.get("chaostone"),"Zone substrate");require(chunk.getBlockState(new BlockPos(0,21,0)).getBlock()==Blocks.DIAMOND_ORE&&chunk.getBlockState(new BlockPos(0,22,0)).getBlock()==Blocks.AIR,"Preserve ore and cave air");require(chunk.getBlockState(new BlockPos(1,20,0)).getBlock()==Blocks.STONE,"Leave vanilla biome intact");
  System.out.println("PASS: survival/creative ring placement and inventory; twelve exact pillar templates/blocked placement; 20 ambient Ogg loops; head pitch/yaw/reset; chaostone conversion preserves caves, ores and adjacent vanilla biomes");
 }
}
