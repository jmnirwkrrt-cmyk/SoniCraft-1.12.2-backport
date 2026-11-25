package org.sonicraft.backport;
import net.minecraft.world.World;import net.minecraft.entity.*;import net.minecraft.util.DamageSource;
public final class SceneEggman43 extends LegacyCharacter {
 public SceneEggman43(World w){super(w);setNoAI(true);setEntityInvulnerable(true);enablePersistence();}
 public Characters.Definition definition(){return Characters.forClass(LegacyCharacter.Eggman.class);}
 protected void initEntityAI(){}public boolean canBePushed(){return false;}public void applyEntityCollision(Entity e){}public boolean attackEntityFrom(DamageSource s,float a){return false;}
}
