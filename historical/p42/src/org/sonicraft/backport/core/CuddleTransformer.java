package org.sonicraft.backport.core;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class CuddleTransformer implements IClassTransformer,Opcodes {
 public byte[] transform(String name,String transformedName,byte[] bytes){
  if(bytes==null||!"tschipp.carryon.common.item.ItemEntity".equals(transformedName))return bytes;
  ClassNode c=new ClassNode();new ClassReader(bytes).accept(c,0);
  String[] named={"onItemRightClick","getItemUseAction","getMaxItemUseDuration","onItemUseFinish"};
  String[] srg={"func_77659_a","func_77661_b","func_77626_a","func_77654_b"};
  String stack="Lnet/minecraft/item/ItemStack;";
  String[] desc={"(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/ActionResult;","("+stack+")Lnet/minecraft/item/EnumAction;","("+stack+")I","("+stack+"Lnet/minecraft/world/World;Lnet/minecraft/entity/EntityLivingBase;)"+stack};
  // Leave an incompatible Carry On version entirely untouched.
  for(MethodNode m:c.methods)for(int i=0;i<4;i++)if((m.name.equals(named[i])||m.name.equals(srg[i]))&&m.desc.equals(desc[i]))return bytes;
  String[] helper={"start","action","duration","finish"};
  for(int i=0;i<4;i++){
   MethodNode m=new MethodNode(ACC_PUBLIC,CuddlePlugin.srg?srg[i]:named[i],desc[i],null,null);
   for(int a=1;a<=((i==0||i==3)?3:1);a++)m.instructions.add(new VarInsnNode(ALOAD,a));
   m.instructions.add(new MethodInsnNode(INVOKESTATIC,"org/sonicraft/backport/Companions42",helper[i],desc[i],false));
   m.instructions.add(new InsnNode(i==2?IRETURN:ARETURN));c.methods.add(m);
  }
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);return w.toByteArray();
 }
}
