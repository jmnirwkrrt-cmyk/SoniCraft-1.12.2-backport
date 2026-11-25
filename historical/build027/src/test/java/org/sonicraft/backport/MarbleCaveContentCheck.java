package org.sonicraft.backport;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Forge/1.12.2 runtime test, unlike the Python offline NBT validation. */
public final class MarbleCaveContentCheck {
 static void check(boolean test,String label){if(!test)throw new AssertionError(label);}
 public static void main(String[] args){
  Bootstrap.register();
  for(Content.BlockDef def:Content.BLOCK_DEFS){Block block=LegacyBlocks.create(def).setRegistryName("sonicraft",def.id);Content.BLOCKS.put(def.id,block);ForgeRegistries.BLOCKS.register(block);}
  for(Block block:ShapeBlocks.DOUBLES.values())ForgeRegistries.BLOCKS.register(block);
  check(ShapeBlocks.DOUBLES.containsKey("marble_zone_stone_slab"),"double slab must be registered");
  Block spikes=Content.BLOCKS.get("spikes");
  for(EnumFacing facing:EnumFacing.values()){
   IBlockState state=spikes.getDefaultState().withProperty(BlockDirectional.FACING,facing);
   check(spikes.getStateFromMeta(spikes.getMetaFromState(state)).getValue(BlockDirectional.FACING)==facing,"spikes meta "+facing);
  }
  Block torch=Content.BLOCKS.get("marble_zone_cave_wall_torch");
  for(EnumFacing facing:EnumFacing.HORIZONTALS)for(int status=0;status<2;status++){
   IBlockState state=torch.getDefaultState().withProperty(BlockHorizontal.FACING,facing).withProperty(MarbleCaveBlocks.Torch.STATUS,status);
   IBlockState decoded=torch.getStateFromMeta(torch.getMetaFromState(state));
   check(decoded.getValue(BlockHorizontal.FACING)==facing && decoded.getValue(MarbleCaveBlocks.Torch.STATUS)==status,"wall torch meta state");
  }
  IBlockState mural=Content.BLOCKS.get("small_marble_mural").getDefaultState();
  check(mural.withRotation(Rotation.CLOCKWISE_90)!=null,"mural rotation");
  for(String name:new String[]{"marble_cave","marble_cave_1","marble_cave_1_hallway_1","marble_cave_1_hallway_2","marble_cave_1_hallway_3","marble_cave_1_hallway_4","marble_cave_1_hallway_end","marble_cave_1_hallway_turn","marble_cave_1_spawner","marble_cave_1_tunnel_1","marble_cave_1_tunnel_2"})
   check(StructurePort.get(name).usable(),"cave original ID compatibility "+name+": "+StructurePort.get(name).missing);
  check(!StructurePort.get("marble_cave_1").deferred.isEmpty(),"incomplete mechanism disclosure");
  System.out.println("PASS: Marble Cave blockstates/metadata, 11 NBT palettes and partial mechanism markers");
 }
}
