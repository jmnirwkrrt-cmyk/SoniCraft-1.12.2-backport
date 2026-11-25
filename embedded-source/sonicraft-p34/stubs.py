from pathlib import Path
base=Path('work34/stub')
imports='import net.minecraft.world.*;import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.util.*;import net.minecraft.util.math.*;import net.minecraft.entity.*;import net.minecraft.entity.player.*;'
def src(f,s):
 pkg,n=f.rsplit('.',1);p=base/(f.replace('.','/')+'.java');p.parent.mkdir(parents=True,exist_ok=True);p.write_text('package '+pkg+';'+imports+s)
d={
'world.IBlockAccess':'public interface IBlockAccess {IBlockState func_180495_p(BlockPos p);}',
'world.World':'public abstract class World implements IBlockAccess {public boolean field_72995_K;public abstract boolean func_175667_e(BlockPos p); public abstract boolean func_180501_a(BlockPos p,IBlockState s,int f);public abstract void func_175684_a(BlockPos p,Block b,int t);public abstract boolean func_175640_z(BlockPos p); public abstract long func_82737_E(); public abstract boolean func_175698_g(BlockPos p);}',
'util.EnumFacing':'public enum EnumFacing {DOWN,UP,NORTH,SOUTH,WEST,EAST;public int func_82601_c(){return this==EAST?1:this==WEST?-1:0;}public int func_96559_d(){return this==UP?1:this==DOWN?-1:0;}public int func_82599_e(){return this==SOUTH?1:this==NORTH?-1:0;} public static EnumFacing func_82600_a(int m){return values()[m%6];}public static EnumFacing func_176731_b(int m){return new EnumFacing[]{SOUTH,WEST,NORTH,EAST}[m&3];}}',
'util.BlockRenderLayer':'public enum BlockRenderLayer {TRANSLUCENT,CUTOUT,SOLID}',
'util.EnumHand':'public enum EnumHand {MAIN_HAND,OFF_HAND}',
'util.math.AxisAlignedBB':'public class AxisAlignedBB {public AxisAlignedBB func_186670_a(BlockPos p){return this;}public boolean func_72326_a(AxisAlignedBB b){return true;}public AxisAlignedBB(double a,double b,double c,double d,double e,double f){}}',
'util.math.BlockPos':'public class BlockPos {public int x,y,z; public BlockPos(int a,int b,int c){x=a;y=b;z=c;}public BlockPos func_177979_c(int v){return new BlockPos(x,y-v,z);}public BlockPos func_177972_a(EnumFacing f){return new BlockPos(x+f.func_82601_c(),y+f.func_96559_d(),z+f.func_82599_e());}public long func_177986_g(){return ((long)x<<38)^((long)z<<12)^y;}public int func_177958_n(){return x;}public int func_177956_o(){return y;}public int func_177952_p(){return z;}}',
'entity.Entity':'public class Entity {public World field_70170_p;public void func_70107_b(double x,double y,double z){field_70165_t=x;field_70163_u=y;field_70161_v=z;}public double field_70159_w,field_70181_x,field_70179_y,field_70165_t,field_70163_u,field_70161_v;public float field_70143_R;public boolean field_70133_I;public boolean func_70093_af(){return false;} public net.minecraft.nbt.NBTTagCompound getEntityData(){return null;}}',
'nbt.NBTTagCompound':'public class NBTTagCompound {public void func_74780_a(String k,double v){}public double func_74769_h(String k){return 0;}public boolean func_74764_b(String k){return false;}public long func_74763_f(String k){return 0;}public void func_74772_a(String k,long v){}}',
'entity.EntityLivingBase':'public class EntityLivingBase extends Entity {}',
'entity.player.EntityPlayer':'public class EntityPlayer extends EntityLivingBase {public boolean func_175149_v(){return false;}}',
'block.state.IBlockState':'public interface IBlockState {Block func_177230_c();}',
'block.Block':'public class Block {public boolean func_180639_a(World w,BlockPos p,IBlockState s,EntityPlayer e,EnumHand hand,EnumFacing f,float x,float y,float z){return false;}public int func_176201_c(IBlockState s){return 0;}public IBlockState func_176203_a(int n){return func_176223_P();}public IBlockState func_176223_P(){return ()->this;} public Block func_149713_g(int n){return this;}}'
}
for k,v in d.items():src('net.minecraft.'+k,v)
pkg='org.sonicraft.backport.'
src(pkg+'Content','class Content {static java.util.Map<String,Block> BLOCKS=new java.util.HashMap<>();static class BlockDef {String id;}}')
for outer,cl in [('BulkBlocks',['Cutout']),('StructureFacadeBlocks',['Horizontal','HorizontalVariant','Attachment','AnyFacing','Axis']),('ZoneMachines',['Jelly'])]:
 src(pkg+outer,'class '+outer+' {'+''.join('static class '+x+' extends Block {'+x+'(Content.BlockDef d){}}' for x in cl)+'}')

src("net.minecraft.item.Item","public class Item {public static Item func_111206_d(String id){return null;}}")
src('net.minecraft.world.biome.Biome','public class Biome {}')
src('net.minecraft.util.Rotation','public enum Rotation {NONE,CLOCKWISE_90,CLOCKWISE_180,COUNTERCLOCKWISE_90}')
src('net.minecraft.tileentity.TileEntity','public class TileEntity {}')
src(pkg+'ExtendedStructureBlocks','class ExtendedStructureBlocks {public static class StateTile extends net.minecraft.tileentity.TileEntity {int variant,mount;boolean powered;void updated(){}}}')
p=base/'net/minecraft/world/World.java';p.write_text(p.read_text().replace('public abstract long func_82737_E();','public abstract long func_82737_E(); public abstract long func_72905_C(); public abstract net.minecraft.world.biome.Biome func_180494_b(BlockPos p); public abstract net.minecraft.tileentity.TileEntity func_175625_s(BlockPos p);public abstract void func_175685_c(BlockPos p,Block b,boolean observers);'))
p=base/'org/sonicraft/backport/Content.java';p.write_text(p.read_text().replace('static class BlockDef','static java.util.Map<String,net.minecraft.world.biome.Biome> BIOMES=new java.util.HashMap<>();static class BlockDef'))
p=base/'net/minecraft/util/math/BlockPos.java';p.write_text(p.read_text().replace('public int func_177958_n()', 'public BlockPos func_177982_a(int a,int b,int c){return new BlockPos(x+a,y+b,z+c);}public int func_177958_n()'))
