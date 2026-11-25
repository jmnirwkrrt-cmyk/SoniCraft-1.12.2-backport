package org.sonicraft.backport;
import net.minecraft.util.EnumFacing;import net.minecraft.util.math.BlockPos;
class Check40 extends Check34{
 public static void main(String[] args){W w=new W();Ent e=new Ent();
 for(EnumFacing f:EnumFacing.values()){e.field_70159_w=.7;e.field_70179_y=-.3;e.field_70181_x=-.9;e.field_70133_I=false;P40.ring(w,p,f,e);
 ok(e.field_70159_w==(f==EnumFacing.WEST?-1.6:f==EnumFacing.EAST?1.6:.7)&&e.field_70181_x==(f==EnumFacing.DOWN?-1.4:f==EnumFacing.UP?1.4:.1)&&e.field_70179_y==(f==EnumFacing.NORTH?-1.6:f==EnumFacing.SOUTH?1.6:-.3)&&e.field_70133_I);}
 ok(MechanismSounds.count==6);ok(P40.allow(w,p,e)&&!P40.allow(w,p,e));w.t+=11;ok(!P40.allow(w,p,e));w.t++;ok(P40.allow(w,p,e));BlockPos q=p.func_177979_c(1);ok(P40.allow(w,q,e));w.t=0;ok(P40.allow(w,q,e));e.field_71093_bK++;ok(P40.allow(w,q,e));System.out.println("PASS: six orientations, transverse momentum, sync, cooldown, adjacent rings, dimension and clock changes");}}
