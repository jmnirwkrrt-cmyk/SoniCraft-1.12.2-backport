package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.item.*;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.world.*;
/** Real vanilla leaf ancestry for collision-transforming mods; preserve the port's persistent foliage. */
public final class Leaves43 extends BlockLeaves {
 public Leaves43(){setHardness(.2f);setSoundType(SoundType.PLANT);setLightOpacity(1);setTickRandomly(false);setDefaultState(blockState.getBaseState().withProperty(CHECK_DECAY,false).withProperty(DECAYABLE,false));}
 protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,CHECK_DECAY,DECAYABLE);}
 public BlockPlanks.EnumType getWoodType(int meta){return BlockPlanks.EnumType.OAK;}
 public IBlockState getStateFromMeta(int meta){return getDefaultState();}
 public int getMetaFromState(IBlockState state){return 0;}
 public int damageDropped(IBlockState state){return 0;}
 public boolean isOpaqueCube(IBlockState state){return false;}
 public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT_MIPPED;}
 public void beginLeavesDecay(IBlockState s,World w,BlockPos p){}
 public void updateTick(World w,BlockPos p,IBlockState s,Random r){}
 public Item getItemDropped(IBlockState s,Random r,int fortune){return Item.getItemFromBlock(this);}
 public int quantityDropped(Random r){return 1;}
 public List<ItemStack> onSheared(ItemStack item,IBlockAccess w,BlockPos p,int fortune){return Collections.singletonList(new ItemStack(this));}
}
