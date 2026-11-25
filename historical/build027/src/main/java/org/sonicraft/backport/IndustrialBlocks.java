package org.sonicraft.backport;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid=SonicraftLegacy.ID)
public final class IndustrialBlocks {
 @net.minecraftforge.fml.common.eventhandler.SubscribeEvent public static void jump(net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent event){
  net.minecraft.entity.EntityLivingBase e=event.getEntityLiving();
  if(e.world.getBlockState(new BlockPos(e.posX,e.posY-.2,e.posZ)).getBlock() instanceof Oil){
   net.minecraft.potion.PotionEffect effect=e.getActivePotionEffect(net.minecraft.init.MobEffects.JUMP_BOOST);double boost=effect==null?0:(effect.getAmplifier()+1)*.1;
   e.motionY=(e.motionY-boost)*.9+boost;
  }
 }
 static Block create(Content.BlockDef d){
  Surface b=d.kind.equals("scaffold")?new Scaffold():d.kind.equals("oil")?new Oil():new Surface(d.kind.equals("metal")?Material.IRON:Material.ROCK);
  b.configure(d);
  if(!d.kind.equals("oil"))b.setHarvestLevel("pickaxe",0);
  return b;
 }
 static class Surface extends Block {
  Surface(Material material){super(material);}
  void configure(Content.BlockDef d){setHardness(d.hardness);setResistance(d.resistance*5f/3f);setSoundType(d.kind.equals("metal")||d.kind.equals("scaffold")?SoundType.METAL:SoundType.STONE);}
 }
 static final class Oil extends Surface {
  Oil(){super(Material.GROUND);slipperiness=1f;setTickRandomly(true);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing side){return 5;}
 }
 /** Source terrain uses blockstate=0. Additional decorative variants remain pending. */
 static final class Scaffold extends Surface {
  static final PropertyDirection FACING=BlockHorizontal.FACING;
  Scaffold(){super(Material.IRON);setLightOpacity(0);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH));}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING);}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(FACING,EnumFacing.getHorizontal(meta&3));}
  @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getHorizontalIndex();}
  @Override public IBlockState getStateForPlacement(World w,BlockPos p,EnumFacing face,float x,float y,float z,int meta,EntityLivingBase placer){return getDefaultState().withProperty(FACING,placer.getHorizontalFacing().getOpposite());}
  @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(FACING,r.rotate(s.getValue(FACING)));}
  @Override public IBlockState withMirror(IBlockState s,Mirror m){return withRotation(s,m.toRotation(s.getValue(FACING)));}
  @Override public boolean isOpaqueCube(IBlockState s){return false;}
  @Override public boolean isFullCube(IBlockState s){return false;}
  @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return FULL_BLOCK_AABB;}
  @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
  @Override public BlockFaceShape getBlockFaceShape(IBlockAccess w,IBlockState s,BlockPos p,EnumFacing f){return BlockFaceShape.SOLID;}
 }
}
