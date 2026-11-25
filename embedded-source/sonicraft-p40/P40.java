package org.sonicraft.backport;
import net.minecraft.world.World;import net.minecraft.entity.Entity;import net.minecraft.util.EnumFacing;import net.minecraft.util.math.BlockPos;import net.minecraft.nbt.NBTTagCompound;
final class P40 {
 static boolean allow(World w,BlockPos p,Entity e){
  NBTTagCompound n=e.getEntityData();long now=w.func_82737_E(),pos=p.func_177986_g(),last=n.func_74763_f("sc40RingTime");
  if(n.func_74764_b("sc40RingTime")&&n.func_74763_f("sc40RingPos")==pos&&n.func_74763_f("sc40RingDim")==e.field_71093_bK&&now>=last&&now-last<12)return false;
  n.func_74772_a("sc40RingTime",now);n.func_74772_a("sc40RingPos",pos);n.func_74772_a("sc40RingDim",e.field_71093_bK);return true;
 }
 static void ring(World w,BlockPos p,EnumFacing f,Entity e){
  int x=f.func_82601_c(),y=f.func_96559_d(),z=f.func_82599_e();
  P34.velocity(e,x==0?e.field_70159_w:x*1.6,y==0?.1:y*1.4,z==0?e.field_70179_y:z*1.6);
  MechanismSounds.play(w,p,"dash_ring.normal");
 }
}
