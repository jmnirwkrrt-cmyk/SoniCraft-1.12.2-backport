import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;import java.nio.file.*;import java.util.zip.*;
public class Patch implements Opcodes {
 static final String P="org/sonicraft/backport/",C=P+"LegacyCharacter",H=P+"Companions42",E="net/minecraft/entity/EntityLivingBase",ED="L"+E+";";
 static MethodInsnNode call(String name,String desc){return new MethodInsnNode(INVOKESTATIC,H,name,desc,false);}
 public static void main(String[] a)throws Exception{
 try(ZipFile z=new ZipFile(a[0])){ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(C+".class"))).accept(c,0);int init=0,interact=0,attack=0;
 for(MethodNode m:c.methods){for(AbstractInsnNode n:m.instructions.toArray()){
 if(m.name.equals("<init>")&&n.getOpcode()==RETURN){InsnList l=new InsnList();l.add(new VarInsnNode(ALOAD,0));l.add(call("init","(L"+C+";)V"));m.instructions.insertBefore(n,l);init++;}
 if(n instanceof MethodInsnNode&&m.name.equals("func_184645_a")){MethodInsnNode j=(MethodInsnNode)n;if(j.owner.equals(P+"FoodContent")&&j.name.equals("feed")){j.owner=H;j.name="interact";interact++;}}
 }
 if(m.name.equals("func_70652_k")){InsnList l=new InsnList();LabelNode ok=new LabelNode();l.add(new VarInsnNode(ALOAD,0));l.add(new VarInsnNode(ALOAD,1));l.add(call("protectedTarget","(L"+C+";Lnet/minecraft/entity/Entity;)Z"));l.add(new JumpInsnNode(IFEQ,ok));l.add(new InsnNode(ICONST_0));l.add(new InsnNode(IRETURN));l.add(ok);l.add(new FrameNode(F_SAME,0,null,0,null));m.instructions.insert(l);attack++;}
 }
 for(String method:new String[]{"func_70624_b","func_70604_c"}){
  for(MethodNode m:c.methods)if(m.name.equals(method))throw new AssertionError("override already exists");
  MethodNode m=new MethodNode(ACC_PUBLIC,method,"("+ED+")V",null,null);m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,0));m.instructions.add(new VarInsnNode(ALOAD,1));m.instructions.add(call("filterTarget","(L"+C+";"+ED+")"+ED));m.instructions.add(new MethodInsnNode(INVOKESPECIAL,c.superName,method,m.desc,false));m.instructions.add(new InsnNode(RETURN));c.methods.add(m);
 }
 if(init!=1||interact!=1||attack!=1)throw new AssertionError(init+"/"+interact+"/"+attack);
 ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);byte[] b=w.toByteArray();ClassNode checked=new ClassNode();new ClassReader(b).accept(checked,0);for(MethodNode m:checked.methods)new Analyzer<BasicValue>(new BasicVerifier()).analyze(c.name,m);
 Path out=Paths.get(a[1],C+".class");Files.createDirectories(out.getParent());Files.write(out,b);System.out.println("Patched constructor, interaction, attack and two target guards; ASM verification passed.");
 }
 }
}
