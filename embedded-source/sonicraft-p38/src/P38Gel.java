package org.sonicraft.backport;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Block;

final class P38Gel extends ZoneMachines.Jelly {
 final boolean green;
 P38Gel(Content.BlockDef d){super(d);green=P34.GREEN.equals(d.id);}
 static Block create(Content.BlockDef d){
  if(P34.GREEN.equals(d.id)||P34.CYAN.equals(d.id))return new P38Gel(d);
  return P34.createPrevious(d);
 }
 public void func_180658_a(World w,BlockPos p,Entity e,float distance){
  e.field_70143_R=0;
 }
 // onLanded precedes onEntityWalk; remove the inherited generic jelly impulse.
 public void func_176216_a(World w,Entity e){if(e.field_70181_x<0)e.field_70181_x=0;}
 public void func_176199_a(World w,BlockPos p,Entity e){
  if(!P34.actor(w,e)||e.field_70181_x>.15)return;
  P34.velocity(e,e.field_70159_w,Math.max(0,e.field_70181_x)+(green?1.8:.8),e.field_70179_y);
  MechanismSounds.play(w,p,green?"mega_mack_green.bounce":"mega_mack_light_blue.bounce");
 }
}
