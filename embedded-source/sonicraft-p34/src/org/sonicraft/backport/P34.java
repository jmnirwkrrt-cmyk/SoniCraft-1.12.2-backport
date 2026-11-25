package org.sonicraft.backport;
import java.util.Random;
import net.minecraft.world.*;import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.entity.*;import net.minecraft.entity.player.*;import net.minecraft.util.*;import net.minecraft.util.math.*;

public final class P34 {
 static final String BLUE="mega_mack_block",CYAN=BLUE+"_light_blue",GREEN=BLUE+"_green",PIPE="cpz_pipe",FOOT="scrap_brain_foothold",SPIN="scrap_brain_spinning_foothold",NC="night_carnival_switch_platform";
 static Block b(String name){return Content.BLOCKS.get(name);}
 static Block at(World w,BlockPos p){return w.func_175667_e(p)?w.func_180495_p(p).func_177230_c():null;}
 static void set(World w,BlockPos p,String id){w.func_180501_a(p,b(id).func_176223_P(),3);}
 static void exchange(World w,BlockPos p,String id){IBlockState s=w.func_180495_p(p);w.func_180501_a(p,b(id).func_176203_a(s.func_177230_c().func_176201_c(s)),3);}
 static boolean actor(World w,Entity e){return !w.field_72995_K && e instanceof EntityLivingBase && !(e instanceof EntityPlayer && ((EntityPlayer)e).func_175149_v());}
 static void velocity(Entity e,double x,double y,double z){e.field_70159_w=x;e.field_70181_x=y;e.field_70179_y=z;e.field_70143_R=0;e.field_70133_I=true;}
 static EnumFacing dir(IBlockState s,boolean horizontal){int m=s.func_177230_c().func_176201_c(s);return horizontal?EnumFacing.func_176731_b(m&3):EnumFacing.func_82600_a(m%6);}
 static void panel(World w,BlockPos p,Entity e,boolean classic){if(!actor(w,e)||e.func_70093_af()||e.field_70181_x>0.15)return;if(classic){velocity(e,e.field_70159_w,.75,e.field_70179_y);return;}EnumFacing f=dir(w.func_180495_p(p),true);velocity(e,-1.4*f.func_82601_c(),1.2,-1.4*f.func_82599_e());}
 static boolean matches(World w,BlockPos p,Block block){return block!=null&&at(w,p)==block;}
 static void syringe(World w,BlockPos p,Entity e){if(!actor(w,e)||!(e instanceof EntityPlayer)||!matches(w,p,b("cpz_syringe_button")))return;
 Block fill=at(w,p.func_177979_c(1));boolean cyan=fill==b("cpz_syringe_pipe_light_blue");if(fill==null||(!cyan&&fill!=b("cpz_syringe_pipe_green"))||!matches(w,p.func_177979_c(2),fill)||!matches(w,p.func_177979_c(3),b("cpz_syringe_needle")))return;
 BlockPos target=p.func_177979_c(4);Block prev=at(w,target);String id=prev==b(BLUE)?(cyan?BLUE+"_blue_to_light_blue":BLUE+"_blue_to_green"):!cyan&&prev==b(CYAN)?BLUE+"_light_blue_to_green":null;if(id==null)return;
 set(w,p,"cpz_syringe_button_pressed");set(w,p.func_177979_c(1),"cpz_syringe_pipe");set(w,target,id);}

 static void cannon(World w,BlockPos p,IBlockState state,Entity e){
  if(!actor(w,e))return;long t=w.func_82737_E(),key=p.func_177986_g();net.minecraft.nbt.NBTTagCompound n=e.getEntityData();
  if(n.func_74764_b("sc34CannonPos")&&n.func_74763_f("sc34CannonPos")==key&&n.func_74763_f("sc34CannonEnd")>t&&n.func_74763_f("sc34CannonEnd")<=t+3)return;
  n.func_74772_a("sc34CannonPos",key);n.func_74772_a("sc34CannonEnd",t+3);EnumFacing f=dir(state,false);velocity(e,2*f.func_82601_c(),1.8*f.func_96559_d(),2*f.func_82599_e());}
 static void markArena(Entity e){net.minecraft.nbt.NBTTagCompound t=e.getEntityData();t.func_74780_a("sc34ArenaX",e.field_70165_t);t.func_74780_a("sc34ArenaY",e.field_70163_u);t.func_74780_a("sc34ArenaZ",e.field_70161_v);}
 static void holdArena(Entity e){if(e.field_70170_p==null||e.field_70170_p.field_72995_K)return;net.minecraft.nbt.NBTTagCompound t=e.getEntityData();if(!t.func_74764_b("sc34ArenaY"))return;double x=t.func_74769_h("sc34ArenaX"),y=t.func_74769_h("sc34ArenaY"),z=t.func_74769_h("sc34ArenaZ");if(e.field_70165_t!=x||e.field_70163_u!=y||e.field_70161_v!=z)e.func_70107_b(x,y,z);e.field_70159_w=e.field_70181_x=e.field_70179_y=0;}
 static void monitor(Block self,World w,BlockPos p,Entity e){
  if(!actor(w,e)||!(e instanceof EntityPlayer)||at(w,p)!=self)return;
  self.func_180639_a(w,p,w.func_180495_p(p),(EntityPlayer)e,EnumHand.MAIN_HAND,EnumFacing.UP,.5f,1f,.5f);
 }
 static boolean ringRemove(World w,BlockPos p){return net.minecraft.item.Item.func_111206_d("sonicraft:gold_ring")!=null&&w.func_175698_g(p);}
 static class Transition extends BulkBlocks.Cutout {final String id;Transition(Content.BlockDef d){super(d);id=d.id;}
 public void func_176213_c(World w,BlockPos p,IBlockState s){if(!w.field_72995_K)w.func_175684_a(p,this,5);}
 public void func_180650_b(World w,BlockPos p,IBlockState s,Random rng){if(w.field_72995_K||at(w,p)!=this)return;
 boolean cyan=id.endsWith("to_light_blue");Block old=b(id.contains("light_blue_to_green")?CYAN:BLUE);set(w,p,cyan?CYAN:GREEN);
 for(EnumFacing f:EnumFacing.values()){BlockPos q=p.func_177972_a(f);if(matches(w,q,old))set(w,q,id);}}}
 static class Green extends ZoneMachines.Jelly {Green(Content.BlockDef d){super(d);}public void func_176199_a(World w,BlockPos p,Entity e){if(actor(w,e)&&e.field_70181_x<=.15)velocity(e,e.field_70159_w,e.field_70181_x+1.8,e.field_70179_y);}}
 static class Panel extends StructureFacadeBlocks.Horizontal {Panel(Content.BlockDef d){super(d);}public void func_180658_a(World w,BlockPos p,Entity e,float h){panel(w,p,e,false);}public void func_176199_a(World w,BlockPos p,Entity e){panel(w,p,e,false);}}
 static class Classic extends StructureFacadeBlocks.HorizontalVariant {Classic(Content.BlockDef d){super(d);}public void func_180658_a(World w,BlockPos p,Entity e,float h){panel(w,p,e,true);}public void func_176199_a(World w,BlockPos p,Entity e){panel(w,p,e,true);}}
 static class Glass extends BulkBlocks.Cutout {Glass(Content.BlockDef d){super(d);func_149713_g(0);}public BlockRenderLayer func_180664_k(){return BlockRenderLayer.TRANSLUCENT;}}
 static void walls(int axis,BlockPos p,AxisAlignedBB area,java.util.List<AxisAlignedBB> out){for(int a=0;a<3;a++)if(a!=axis)for(int side=0;side<2;side++){double[] lo={0,0,0},hi={1,1,1};if(side==0)hi[a]=1./16;else lo[a]=15./16;AxisAlignedBB box=new AxisAlignedBB(lo[0],lo[1],lo[2],hi[0],hi[1],hi[2]).func_186670_a(p);if(box.func_72326_a(area))out.add(box);}}
 static class CasinoElevator extends StructureFacadeBlocks.Axis {CasinoElevator(Content.BlockDef d){super(d);}public boolean isLadder(IBlockState s,IBlockAccess w,BlockPos p,EntityLivingBase e){return true;}
 public boolean func_149662_c(IBlockState s){return false;}public boolean func_149686_d(IBlockState s){return false;}
 public void func_185477_a(IBlockState s,World w,BlockPos p,AxisAlignedBB area,java.util.List<AxisAlignedBB> out,Entity e,boolean actual){walls(func_176201_c(s),p,area,out);}}
 static class Elevator extends Glass {Elevator(Content.BlockDef d){super(d);}
 public void func_185477_a(IBlockState s,World w,BlockPos p,AxisAlignedBB area,java.util.List<AxisAlignedBB> out,Entity e,boolean actual){walls(1,p,area,out);}
 public boolean isLadder(IBlockState s,IBlockAccess w,BlockPos p,EntityLivingBase e){return true;}
 public AxisAlignedBB func_180646_a(IBlockState s,IBlockAccess w,BlockPos p){return null;}}
 static class Foot extends StructureFacadeBlocks.Horizontal {final boolean spin;Foot(Content.BlockDef d){super(d);spin=d.id.equals(SPIN);}
 public AxisAlignedBB func_180646_a(IBlockState s,IBlockAccess w,BlockPos p){return spin?null:new AxisAlignedBB(0,.625,0,1,1,1);}
 void start(World w,BlockPos p,Entity e){if(!spin&&actor(w,e))w.func_175684_a(p,this,20);}
 public void func_180658_a(World w,BlockPos p,Entity e,float h){start(w,p,e);}public void func_176199_a(World w,BlockPos p,Entity e){start(w,p,e);}
 public void func_176213_c(World w,BlockPos p,IBlockState s){if(spin&&!w.field_72995_K)w.func_175684_a(p,this,80);}
 public void func_180650_b(World w,BlockPos p,IBlockState s,Random r){if(!w.field_72995_K&&at(w,p)==this)exchange(w,p,spin?FOOT:SPIN);}}
 static void tube(World w,BlockPos p,Entity e,boolean corner){if(!actor(w,e)||e.func_70093_af()||Math.floor(e.field_70165_t)!=p.func_177958_n()||Math.floor(e.field_70161_v)!=p.func_177952_p()||Math.floor(e.field_70163_u)!=p.func_177956_o())return;
 EnumFacing f=null;if(!corner)f=dir(w.func_180495_p(p),false);else for(EnumFacing side:EnumFacing.values()){BlockPos q=p.func_177972_a(side);if(matches(w,q,b(PIPE))&&dir(w.func_180495_p(q),false)==side){f=side;break;}}
 if(f==null)return;double speed=corner?(f.func_96559_d()!=0?.5:.9):.6;
 velocity(e,f.func_82601_c()!=0?speed*f.func_82601_c():Math.max(-.35,Math.min(.35,p.func_177958_n()+.5-e.field_70165_t)),f.func_96559_d()!=0?speed*f.func_96559_d():0,f.func_82599_e()!=0?speed*f.func_82599_e():Math.max(-.35,Math.min(.35,p.func_177952_p()+.5-e.field_70161_v)));}
 static class Powered extends StructureFacadeBlocks.Horizontal {final boolean on;Powered(Content.BlockDef d){super(d);on=d.id.endsWith("_on");}public void func_189540_a(IBlockState s,World w,BlockPos p,Block other,BlockPos from){if(!w.field_72995_K&&at(w,p)==this&&w.func_175640_z(p)!=on)exchange(w,p,on?NC:NC+"_on");}}
 static final String WIRE="night_carnival_switch_wireless_connector";
 static class Wire extends BulkBlocks.Cutout {final int stage;Wire(Content.BlockDef d){super(d);stage=d.id.equals(WIRE)?0:d.id.charAt(d.id.length()-1)-'0';}
 public boolean func_149744_f(IBlockState s){return stage==1||stage==2;}
 public int func_180656_a(IBlockState s,IBlockAccess w,BlockPos p,EnumFacing f){return stage==1||stage==2?15:0;}
 public AxisAlignedBB func_180646_a(IBlockState s,IBlockAccess w,BlockPos p){return null;}
 public void func_176213_c(World w,BlockPos p,IBlockState s){if(!w.field_72995_K&&(stage==1||stage==3))w.func_175684_a(p,this,10);}
 public void func_180650_b(World w,BlockPos p,IBlockState s,Random r){if(!w.field_72995_K&&at(w,p)==this&&(stage==1||stage==3))set(w,p,stage==1?WIRE+"_stage_2":WIRE);}}
 static boolean pressNight(World w,BlockPos p,boolean release){
 if(w.field_72995_K||!matches(w,p,b("switch_night_carnival"))||!(w.func_175625_s(p) instanceof ExtendedStructureBlocks.StateTile))return false;
 ExtendedStructureBlocks.StateTile t=(ExtendedStructureBlocks.StateTile)w.func_175625_s(p);if(t.variant!=(release?1:0))return false;t.variant=release?0:1;t.updated();w.func_175685_c(p,at(w,p),false);
 for(EnumFacing f:EnumFacing.values()){BlockPos q=p.func_177972_a(f);Block cur=at(w,q);
 if(!release&&cur==b(WIRE))set(w,q,WIRE+"_stage_1");else if(release&&cur==b(WIRE+"_stage_2"))set(w,q,WIRE+"_stage_3");}
 if(!release)w.func_175684_a(p,at(w,p),100);return true;}
 static void nightTouch(World w,BlockPos p){pressNight(w,p,false);}static void nightEnd(World w,BlockPos p){pressNight(w,p,true);}
 static Block create(Content.BlockDef d){if(d.id.equals(WIRE)||d.id.startsWith(WIRE+"_stage_"))return new Wire(d);switch(d.id){
 case "jump_panel_modern":case "trick_jump_panel_modern":return new Panel(d);case "jump_panel_classic":return new Classic(d);case "scrap_brain_safety_glass":return new Glass(d);case "casino_night_elevator_tube":case "casino_night_elevator_floor_tube":return new CasinoElevator(d);case "scrap_brain_elevator_tube":return new Elevator(d);
 case FOOT:case SPIN:return new Foot(d);case PIPE:return new Straight(d);case "cpz_pipe_corner":return new Corner(d);case "cpz_pipe_corner_y_axis":return new CornerY(d);
 case NC:case NC+"_on":return new Powered(d);case GREEN:return new Green(d);case BLUE+"_blue_to_green":case BLUE+"_blue_to_light_blue":case BLUE+"_light_blue_to_green":return new Transition(d);default:return null;}}
static class Straight extends StructureFacadeBlocks.AnyFacing{Straight(Content.BlockDef d){super(d);} public AxisAlignedBB func_180646_a(IBlockState s,IBlockAccess w,BlockPos p){return null;}public boolean func_149662_c(IBlockState s){return false;}public boolean func_149686_d(IBlockState s){return false;}public void func_180634_a(World w,BlockPos p,IBlockState s,Entity e){tube(w,p,e,false);}}
static class Corner extends StructureFacadeBlocks.Attachment{Corner(Content.BlockDef d){super(d);} public AxisAlignedBB func_180646_a(IBlockState s,IBlockAccess w,BlockPos p){return null;}public boolean func_149662_c(IBlockState s){return false;}public boolean func_149686_d(IBlockState s){return false;}public void func_180634_a(World w,BlockPos p,IBlockState s,Entity e){tube(w,p,e,true);}}
static class CornerY extends StructureFacadeBlocks.Horizontal{CornerY(Content.BlockDef d){super(d);} public AxisAlignedBB func_180646_a(IBlockState s,IBlockAccess w,BlockPos p){return null;}public boolean func_149662_c(IBlockState s){return false;}public boolean func_149686_d(IBlockState s){return false;}public void func_180634_a(World w,BlockPos p,IBlockState s,Entity e){tube(w,p,e,true);}}
}
