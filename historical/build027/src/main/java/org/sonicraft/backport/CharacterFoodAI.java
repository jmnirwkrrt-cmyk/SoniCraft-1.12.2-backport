package org.sonicraft.backport;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;

/** Food attraction also works for creative players and checks both hands. */
final class CharacterFoodAI extends EntityAIBase {
 private final LegacyCharacter character;
 private EntityPlayer player;
 CharacterFoodAI(LegacyCharacter character){this.character=character;setMutexBits(3);}
 static EntityPlayer findPlayer(LegacyCharacter character){
  EntityPlayer best=null;double distance=100;
  for(EntityPlayer p:character.world.playerEntities){
   if(!p.isEntityAlive()||p.isSpectator()||!FoodContent.isTempting(character.definition().id,p))continue;
   double d=character.getDistanceSq(p);if(d<distance){distance=d;best=p;}
  }
  return best;
 }
 @Override public boolean shouldExecute(){player=findPlayer(character);return player!=null&&character.getAttackTarget()==null;}
 @Override public boolean shouldContinueExecuting(){return shouldExecute();}
 @Override public void resetTask(){player=null;character.getNavigator().clearPath();}
 @Override public void updateTask(){
  character.getLookHelper().setLookPositionWithEntity(player,30,character.getVerticalFaceSpeed());
  if(character.getDistanceSq(player)<6.25)character.getNavigator().clearPath();
  else character.getNavigator().tryMoveToEntityLiving(player,character.definition().id.equals("rouge")?.5:1.0);
 }
}
