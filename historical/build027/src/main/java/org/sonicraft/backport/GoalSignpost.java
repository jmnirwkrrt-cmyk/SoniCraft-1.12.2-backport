package org.sonicraft.backport;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
public final class GoalSignpost extends Block {
 static final PropertyDirection FACING=BlockHorizontal.FACING;final String id;
 GoalSignpost(String id){super(Material.IRON);this.id=id;setHardness(1);setResistance(50f/3f);setHarvestLevel("pickaxe",0);setSoundType(SoundType.METAL);setLightOpacity(0);setDefaultState(blockState.getBaseState().withProperty(FACING,EnumFacing.NORTH));}
 @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,FACING);}
 @Override public IBlockState getStateFromMeta(int m){return getDefaultState().withProperty(FACING,EnumFacing.getHorizontal(m&3));}
 @Override public int getMetaFromState(IBlockState s){return s.getValue(FACING).getHorizontalIndex();}
 @Override public IBlockState getStateForPlacement(World w,BlockPos p,EnumFacing f,float x,float y,float z,int meta,EntityLivingBase e){return getDefaultState().withProperty(FACING,e.getHorizontalFacing().getOpposite());}
 @Override public IBlockState withRotation(IBlockState s,Rotation r){return s.withProperty(FACING,r.rotate(s.getValue(FACING)));}
 @Override public IBlockState withMirror(IBlockState s,Mirror m){return withRotation(s,m.toRotation(s.getValue(FACING)));}
 @Override public boolean isOpaqueCube(IBlockState s){return false;}
 @Override public boolean isFullCube(IBlockState s){return false;}
 @Override public BlockRenderLayer getBlockLayer(){return BlockRenderLayer.CUTOUT;}
 @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return NULL_AABB;}
 @Override public AxisAlignedBB getBoundingBox(IBlockState s,IBlockAccess w,BlockPos p){return s.getValue(FACING).getAxis()==EnumFacing.Axis.Z?new AxisAlignedBB(-.25,0,.1875,1.25,1.5,.8125):new AxisAlignedBB(.1875,0,-.25,.8125,1.5,1.25);}
 @Override public BlockFaceShape getBlockFaceShape(IBlockAccess w,IBlockState s,BlockPos p,EnumFacing f){return BlockFaceShape.UNDEFINED;}
 @Override public Item getItemDropped(IBlockState s,Random r,int fortune){return Item.getItemFromBlock(Content.BLOCKS.get("signpost"));}
 @Override public int damageDropped(IBlockState s){return 0;}
 @Override public void onBlockAdded(World w,BlockPos p,IBlockState s){if(!w.isRemote&&id.equals("signpost_spinning"))w.scheduleUpdate(p,this,60);}
 @Override public void onEntityCollidedWithBlock(World w,BlockPos p,IBlockState s,Entity e){if(w.isRemote||!id.equals("signpost")||(!(e instanceof EntityPlayer)&&!(e instanceof LegacyCharacter))||(e instanceof EntityPlayer&&((EntityPlayer)e).isSpectator()))return;w.setBlockState(p,Content.BLOCKS.get("signpost_spinning").getDefaultState().withProperty(FACING,s.getValue(FACING)),3);w.playSound(null,p,EmeraldProgression.SOUNDS.get("signpost.spin"),SoundCategory.NEUTRAL,1,1);}
 @Override public void updateTick(World w,BlockPos p,IBlockState s,Random r){if(!w.isRemote&&id.equals("signpost_spinning")&&w.getBlockState(p).getBlock()==this)w.setBlockState(p,Content.BLOCKS.get("signpost_player").getDefaultState().withProperty(FACING,s.getValue(FACING)),3);}
 @Override public boolean onBlockActivated(World w,BlockPos p,IBlockState s,EntityPlayer player,EnumHand hand,EnumFacing f,float x,float y,float z){if(!id.equals("signpost_player")||hand!=EnumHand.MAIN_HAND||player.getHeldItem(hand).getItem()!=EmeraldProgression.KEY)return false;return w.isRemote||EmeraldProgression.redeem(player,hand);}
}
