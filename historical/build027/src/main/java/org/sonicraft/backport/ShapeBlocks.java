package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.*;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.item.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
public final class ShapeBlocks {
 static final Map<String,Slab> DOUBLES=new LinkedHashMap<>();
 enum Variant implements IStringSerializable { SINGLE; public String getName(){return "single";} }
 static final PropertyEnum<Variant> VARIANT=PropertyEnum.create("variant",Variant.class);
 static Block create(Content.BlockDef d){if(d.kind.equals("shape_wall"))return new Wall(d);Slab single=new Slab(d,false),full=new Slab(d,true);single.single=single;single.full=full;full.single=single;full.full=full;full.setRegistryName("sonicraft",d.id+"_double").setUnlocalizedName("sonicraft."+d.id);DOUBLES.put(d.id,full);return single;}
 static Item item(Block b){if(b instanceof BulkBlocks.Door)return new ItemDoor(b);if(b instanceof Slab){Slab s=(Slab)b;return new ItemSlab(s,s,s.full);}return new ItemBlock(b);}
 static class Slab extends BlockSlab {
  final Content.BlockDef d;final boolean doubled;Slab single,full;
  Slab(Content.BlockDef d,boolean doubled){super(BulkBlocks.material(d));this.d=d;this.doubled=doubled;setDefaultState(blockState.getBaseState().withProperty(VARIANT,Variant.SINGLE).withProperty(HALF,EnumBlockHalf.BOTTOM));setSoundType(BulkBlocks.sound(d));BulkBlocks.configure(this,d);useNeighborBrightness=!doubled;setLightOpacity(doubled&&!d.zeroLight?255:0);}
  @Override protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,HALF,VARIANT);}
  @Override public boolean isDouble(){return doubled;}
  @Override public IProperty<?> getVariantProperty(){return VARIANT;}
  @Override public Comparable<?> getTypeForItem(ItemStack stack){return Variant.SINGLE;}
  @Override public String getUnlocalizedName(int meta){return getUnlocalizedName();}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState().withProperty(HALF,!doubled&&(meta&8)!=0?EnumBlockHalf.TOP:EnumBlockHalf.BOTTOM);}
  @Override public int getMetaFromState(IBlockState s){return !doubled&&s.getValue(HALF)==EnumBlockHalf.TOP?8:0;}
  @Override public int damageDropped(IBlockState s){return 0;}
  @Override public Item getItemDropped(IBlockState s,Random r,int fortune){return Item.getItemFromBlock(single);}
  @Override public ItemStack getItem(World w,BlockPos p,IBlockState s){return new ItemStack(single);}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }
 static class Wall extends BlockWall {
  final Content.BlockDef d;
  Wall(Content.BlockDef d){super(new BulkBlocks.Cube(d));this.d=d;setSoundType(BulkBlocks.sound(d));BulkBlocks.configure(this,d);}
  @Override public IBlockState getStateFromMeta(int meta){return getDefaultState();}
  @Override public int getMetaFromState(IBlockState s){return 0;}
  @Override public int damageDropped(IBlockState s){return 0;}
  @Override public void getSubBlocks(CreativeTabs tab,NonNullList<ItemStack> items){items.add(new ItemStack(this));}
  @Override public int getFlammability(IBlockAccess w,BlockPos p,EnumFacing f){return d.flammability;}
  @Override public int getFireSpreadSpeed(IBlockAccess w,BlockPos p,EnumFacing f){return d.fireSpread;}
 }
}
