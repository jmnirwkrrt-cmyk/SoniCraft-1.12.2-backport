import java.nio.file.*;import java.util.*;import java.util.zip.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class Patch42 implements Opcodes{
 static final String C="org/sonicraft/backport/LegacyCharacter",H="org/sonicraft/backport/Companions42";
 public static void main(String[]a)throws Exception{
  try(ZipFile z=new ZipFile(a[0])){
   ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(C+".class"))).accept(c,0);
   int constructors=0,feeds=0,attacks=0;
   for(MethodNode m:c.methods){
    if(m.name.equals("<init>"))for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()==RETURN){
     InsnList i=new InsnList();i.add(new VarInsnNode(ALOAD,0));i.add(new VarInsnNode(ALOAD,0));i.add(new FieldInsnNode(GETFIELD,C,"field_70714_bg","Lnet/minecraft/entity/ai/EntityAITasks;"));i.add(new VarInsnNode(ALOAD,0));i.add(new FieldInsnNode(GETFIELD,C,"field_70715_bh","Lnet/minecraft/entity/ai/EntityAITasks;"));
     i.add(new MethodInsnNode(INVOKESTATIC,H,"init","(L"+C+";Lnet/minecraft/entity/ai/EntityAITasks;Lnet/minecraft/entity/ai/EntityAITasks;)V",false));m.instructions.insertBefore(n,i);constructors++;
    }
    for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode){MethodInsnNode call=(MethodInsnNode)n;if(call.owner.equals("org/sonicraft/backport/FoodContent")&&call.name.equals("feed")){call.owner=H;call.name="interact";feeds++;}}
    if(m.name.equals("func_70652_k")){
     LabelNode ok=new LabelNode();InsnList i=new InsnList();i.add(new VarInsnNode(ALOAD,0));i.add(new VarInsnNode(ALOAD,1));i.add(new MethodInsnNode(INVOKESTATIC,H,"protectedTarget","(L"+C+";Lnet/minecraft/entity/Entity;)Z",false));i.add(new JumpInsnNode(IFEQ,ok));i.add(new InsnNode(ICONST_0));i.add(new InsnNode(IRETURN));i.add(ok);i.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(i);attacks++;
    }
   }
   for(String name:new String[]{"func_70624_b","func_70604_c"}){
    for(MethodNode m:c.methods)if(m.name.equals(name))throw new IllegalStateException("existing override "+name);
    MethodNode m=new MethodNode(ACC_PUBLIC,name,"(Lnet/minecraft/entity/EntityLivingBase;)V",null,null);
    m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,1));
    m.instructions.add(new MethodInsnNode(INVOKESTATIC,H,"filterTarget","(L"+C+";Lnet/minecraft/entity/EntityLivingBase;)Lnet/minecraft/entity/EntityLivingBase;",false));
    m.instructions.add(new MethodInsnNode(INVOKESPECIAL,c.superName,name,m.desc,false));m.instructions.add(new InsnNode(RETURN));c.methods.add(m);
   }
   if(constructors!=1||feeds!=1||attacks!=1)throw new IllegalStateException("Unexpected P41 base");
   ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);
   Path out=Paths.get(a[1],C+".class");Files.createDirectories(out.getParent());Files.write(out,w.toByteArray());
   System.out.println("P41 hooks patched: constructor, interaction, melee, attack/revenge targets");
  }
 }
}
