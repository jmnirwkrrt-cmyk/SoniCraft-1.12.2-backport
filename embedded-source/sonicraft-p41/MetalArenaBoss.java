package org.sonicraft.backport;
import net.minecraft.world.World;import net.minecraft.entity.EntityLivingBase;
public final class MetalArenaBoss extends SoniBoss {
 public MetalArenaBoss(World w){super(w);}
 String bossId(){return "metal_sonic_boss";}String geoId(){return "metal_sonic";}String textureId(){return geoId();}
 double bossHealth(){return 75;}double bossArmor(){return .8;}double bossSpeed(){return .4;}
 float bossWidth(){return .8f;}float bossHeight(){return 1.5f;}
 int attackDelay(){return 100;}double attackRange(){return 16;}
 void performAttack(EntityLivingBase t){double dx=t.field_70165_t-field_70165_t,dz=t.field_70161_v-field_70161_v;
 if(field_70146_Z.nextBoolean()){shockwave(5,4);return;}
 double l=Math.max(.01,Math.hypot(dx,dz));P34.velocity(this,1.35*dx/l,.15,1.35*dz/l);shockwave(3,2.4);}
 String animationName(){return func_70638_az()==null?"idle":"aggro";}
}
