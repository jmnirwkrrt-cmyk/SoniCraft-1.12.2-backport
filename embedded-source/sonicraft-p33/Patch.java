import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;import java.nio.file.*;import java.util.*;import java.util.zip.*;
public class Patch implements Opcodes {
 public static void main(String[] a)throws Exception {
 String owner="org/sonicraft/backport/BulkBlocks";ClassNode c=new ClassNode();try(ZipFile z=new ZipFile(a[0])){new ClassReader(z.getInputStream(z.getEntry(owner+".class"))).accept(c,0);}
 int edits=0;
 for(MethodNode m:c.methods)if(m.name.equals("configure"))for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof MethodInsnNode&&((MethodInsnNode)n).name.equals("emitsLight")){
 AbstractInsnNode j=n.getNext(),v=j.getNext().getNext();if(j.getOpcode()!=IFEQ||!(v instanceof LdcInsnNode)||!Float.valueOf(.9f).equals(((LdcInsnNode)v).cst)||m.maxLocals!=2)throw new AssertionError("Unexpected P32 code");
 m.instructions.insertBefore(n,new InsnNode(DUP));
 InsnList l=new InsnList();l.add(new MethodInsnNode(INVOKESTATIC,"org/sonicraft/backport/P33Light","intensity","(Ljava/lang/String;Z)F",false));l.add(new VarInsnNode(FSTORE,2));l.add(new VarInsnNode(FLOAD,2));l.add(new InsnNode(FCONST_0));l.add(new InsnNode(FCMPL));m.instructions.insert(n,l);m.instructions.set(v,new VarInsnNode(FLOAD,2));m.maxLocals++;m.maxStack++;edits++;
 }
 if(edits!=1)throw new AssertionError();ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);Files.write(Paths.get(a[1]),w.toByteArray());
 for(MethodNode m:c.methods)new Analyzer<BasicValue>(new BasicVerifier()).analyze(c.name,m);
 c.methods.removeIf(m->!m.name.equals("emitsLight")&&!m.name.equals("configure"));c.innerClasses.clear();c.fields.clear();ClassWriter f=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(f);Files.write(Paths.get(a[2]),f.toByteArray());}
}
