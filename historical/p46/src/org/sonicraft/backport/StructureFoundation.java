package org.sonicraft.backport;
import java.util.*;import net.minecraft.block.*;import net.minecraft.block.state.IBlockState;import net.minecraft.init.Blocks;import net.minecraft.util.Rotation;import net.minecraft.util.math.BlockPos;import net.minecraft.world.World;
final class StructureFoundation {
 private static boolean enabled=true;private static int maxDepth=48;
 static void configure(){enabled=SonicraftLegacy.config.getBoolean("fillStructureFoundations","structures",true,"Extend surface foundations down to ground; new placements only.");maxDepth=SonicraftLegacy.config.getInt("structureFoundationMaxDepth","structures",48,1,128,"Maximum foundation depth; respects existing configured values.");}
 static boolean column(IBlockState s){String id=s.getBlock().getRegistryName()==null?"":s.getBlock().getRegistryName().getResourcePath();return (id.contains("pillar")||id.contains("column")||id.contains("support"))&&!id.contains("glass")&&!id.contains("bar")&&!id.contains("horizontal");}
 static boolean suitable(IBlockState s){return s.getMaterial().isSolid()&&!(s.getBlock() instanceof BlockDoor)&&!s.getBlock().hasTileEntity(s)&&(s.isFullCube()||s.getBlock() instanceof BlockSlab||s.getBlock() instanceof BlockStairs||column(s));}
 static int fill(World w,BlockPos origin,StructurePort.Template t,Rotation r){
  if(!enabled||w.isRemote||t==null)return 0;String name=t.name;
  if(name.contains("cave")||name.contains("dungeon")||name.contains("tunnel")||name.contains("hallway")||name.contains("underground")||name.contains("sky_sanctuary")||name.contains("death_egg")||name.contains("mountain")||name.contains("lake")||name.contains("tree")||name.contains("mushroom"))return 0;
  int bottom=256;Map<Long,StructurePort.Cell> lowest=new HashMap<>();
  for(StructurePort.Cell c:t.blocks)if(suitable(c.block)){bottom=Math.min(bottom,c.y);long key=((long)c.x<<32)^(c.z&0xffffffffL);StructurePort.Cell prev=lowest.get(key);if(prev==null||prev.y>c.y)lowest.put(key,c);}
  int count=0;
  for(StructurePort.Cell c:lowest.values()){
   if(c.y>bottom+2&&!column(c.block))continue;
   BlockPos p=StructurePort.at(t,origin,c,r);IBlockState state=c.block.withRotation(r);int gap=0;boolean grounded=false;
   for(int d=1;d<=maxDepth+1&&p.getY()-d>0;d++){
    BlockPos q=p.down(d);if(!w.isBlockLoaded(q)||w.getTileEntity(q)!=null)break;IBlockState s=w.getBlockState(q);
    if(s.getMaterial().isLiquid())break;
    if(w.isAirBlock(q)||s.getMaterial().isReplaceable()){if(d>maxDepth)break;gap++;continue;}
    grounded=s.isFullCube();break;
   }
   if(!grounded||gap==0)continue;
   if(state.getBlock() instanceof BlockSlab||state.getBlock() instanceof BlockStairs){String id=state.getBlock().getRegistryName().getResourcePath().replaceFirst("_slab$","").replaceFirst("_stairs$","");Block b=Content.BLOCKS.get(id);state=b!=null&&b.getDefaultState().isFullCube()?b.getDefaultState():Blocks.STONE.getDefaultState();}
   for(int d=1;d<=gap;d++)if(w.setBlockState(p.down(d),state,2))count++;
  }return count;
 }
}
