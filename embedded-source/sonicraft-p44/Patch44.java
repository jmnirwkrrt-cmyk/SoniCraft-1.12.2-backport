import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.zip.*;
public class Patch44 {
 public static void main(String[] a)throws Exception{String n="org/sonicraft/backport/StructurePort.class";ClassNode c=new ClassNode();try(ZipFile z=new ZipFile(a[0])){new ClassReader(z.getInputStream(z.getEntry(n))).accept(c,0);}int hits=0;
 for(MethodNode m:c.methods)for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode x=(MethodInsnNode)i;if(x.owner.equals("org/sonicraft/backport/ScrapBrainSequence")&&x.name.equals("register")){x.owner="org/sonicraft/backport/Sequence44";hits++;}}
 if(hits!=1)throw new AssertionError(hits);ClassWriter w=new ClassWriter(0);c.accept(w);Path p=Paths.get(a[1],n);Files.createDirectories(p.getParent());Files.write(p,w.toByteArray());System.out.println("PASS: shifted Scrap Brain sequence registration");}
}
