package org.sonicraft.backport;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Simple structure-compatible axis/facing variants. */
final class MarbleDecorative {
 static class Axis extends BlockRotatedPillar {
  Axis(){super(Material.ROCK);setHardness(1.5f).setResistance(10f);setSoundType(SoundType.STONE);}
 }
 static class Facing extends BlockHorizontal {
  Facing(){super(Material.ROCK);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH));setHardness(1.5f).setResistance(10f);setSoundType(SoundType.STONE);}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING);}
  @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(FACING,EnumFacing.getHorizontal(m&3));}
  @Override public int getMetaFromState(IBlockState state){return state.getValue(FACING).getHorizontalIndex();}
  @Override public IBlockState withRotation(IBlockState state,Rotation rot){return state.withProperty(FACING,rot.rotate(state.getValue(FACING)));}
  @Override public IBlockState withMirror(IBlockState state,Mirror mirror){return state.withRotation(mirror.toRotation(state.getValue(FACING)));}
 }
 static class SolidLava extends Block {
  SolidLava(){super(Material.ROCK);setHardness(1.5f).setResistance(10f);setLightLevel(0.9f);setSoundType(SoundType.STONE);}
  @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState state,Entity entity){entity.attackEntityFrom(net.minecraft.util.DamageSource.LAVA,4.0f);entity.setFire(5);}
 }
}
