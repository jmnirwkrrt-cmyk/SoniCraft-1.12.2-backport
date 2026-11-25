import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.zip.*;
public class Patch45 {
 public static void main(String[] a)throws Exception{String n="org/sonicraft/backport/StructurePort.class";ClassNode c=new ClassNode();try(ZipFile z=new ZipFile(a[0])){new ClassReader(z.getInputStream(z.getEntry(n))).accept(c,0);}int hits=0;
 for(MethodNode m:c.methods)if(m.name.equals("decorate"))for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode x=(MethodInsnNode)i;if(x.owner.equals("org/sonicraft/backport/StructurePort")&&x.name.equals("decorateSmall")){x.owner="org/sonicraft/backport/Features45";x.name="legacySmall";hits++;}}
 if(hits<10)throw new AssertionError(hits);ClassWriter w=new ClassWriter(0);c.accept(w);Path p=Paths.get(a[1],n);Files.createDirectories(p.getParent());Files.write(p,w.toByteArray());System.out.println("PASS: "+hits+" decoration call sites routed through duplicate guard");}
}
