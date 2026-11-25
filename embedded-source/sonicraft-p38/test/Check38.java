package org.sonicraft.backport;
class Check38 extends Check34{
 public static void main(String[] args){
 Check34.main(args);ok(MechanismSounds.count==2 && "chemical.change".equals(MechanismSounds.last));
 W w=new W();Ent e=new Ent();
 for(boolean green:new boolean[]{false,true}){P38Gel gel=(P38Gel)add(green?P34.GREEN:P34.CYAN);int start=MechanismSounds.count;
 e.field_70159_w=.25;e.field_70181_x=-1.5;e.field_70143_R=16;
 gel.func_180658_a(w,p,e,16);gel.func_176216_a(w,e);gel.func_176199_a(w,p,e);gel.func_176199_a(w,p,e);
 ok(e.field_70181_x==(green?1.8:.8)&&e.field_70159_w==.25&&e.field_70143_R==0&&MechanismSounds.count==start+1);
 ok(MechanismSounds.last.equals(green?"mega_mack_green.bounce":"mega_mack_light_blue.bounce"));w.field_72995_K=true;e.field_70181_x=0;gel.func_176199_a(w,p,e);ok(e.field_70181_x==0&&MechanismSounds.count==start+1);w.field_72995_K=false;}
 System.out.println("PASS: both landing/walk sequences, velocities, sound dispatch, no duplicate trigger, no client replay");}}
