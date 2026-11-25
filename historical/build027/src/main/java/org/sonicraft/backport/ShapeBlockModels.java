package org.sonicraft.backport;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.*;
import net.minecraftforge.client.model.ModelLoader;
final class ShapeBlockModels {
 static void register(){for(Block b:Content.BLOCKS.values()){
  if(b instanceof ShapeBlocks.Slab){ShapeBlocks.Slab s=(ShapeBlocks.Slab)b;StateMapperBase mapper=new StateMapperBase(){protected ModelResourceLocation getModelResourceLocation(IBlockState state){ShapeBlocks.Slab slab=(ShapeBlocks.Slab)state.getBlock();return new ModelResourceLocation(s.getRegistryName(),"type="+(slab.isDouble()?"double":state.getValue(BlockSlab.HALF)==BlockSlab.EnumBlockHalf.TOP?"top":"bottom"));}};ModelLoader.setCustomStateMapper(s,mapper);ModelLoader.setCustomStateMapper(s.full,mapper);}
  if(b instanceof ShapeBlocks.Wall)ModelLoader.setCustomStateMapper(b,new StateMap.Builder().ignore(BlockWall.VARIANT).build());
 }}
}
