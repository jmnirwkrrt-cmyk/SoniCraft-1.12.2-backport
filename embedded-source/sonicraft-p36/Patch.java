import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*; import java.util.zip.*; import java.io.*;
public final class Patch {
 public static void main(String[] a)throws Exception {
   String pkg="org/sonicraft/backport/";
   try(ZipFile jar=new ZipFile(a[0])){
    for(String cls:new String[]{"MobiusWorld","SonicraftLegacy"}){
     String path=pkg+cls+".class";ClassNode c=new ClassNode();
     new ClassReader(jar.getInputStream(jar.getEntry(path))).accept(c,0);int edits=0;
     for(MethodNode m:c.methods)for(AbstractInsnNode n:m.instructions.toArray()){
      if(cls.equals("MobiusWorld")&&m.name.equals("register")&&n instanceof IntInsnNode&&((IntInsnNode)n).operand==11230){
       m.instructions.set(n,new MethodInsnNode(Opcodes.INVOKESTATIC,pkg+"MobiusSpace","freshDefault","()I",false));edits++;
      }else if(cls.equals("SonicraftLegacy")&&m.name.equals("serverStarting")&&m.desc.equals("(Lnet/minecraftforge/fml/common/event/FMLServerStartingEvent;)V")&&n.getOpcode()==Opcodes.RETURN){
       m.instructions.insertBefore(n,new FieldInsnNode(Opcodes.GETSTATIC,pkg+"MobiusWorld","dimensionId","I"));
       m.instructions.insertBefore(n,new MethodInsnNode(Opcodes.INVOKESTATIC,pkg+"MobiusSpace","serverStarting","(I)V",false));edits++;
      }
     }
     if(edits!=1)throw new IOException(cls+" unexpected hook count "+edits);
     if(cls.equals("SonicraftLegacy")){int changed=0;for(AnnotationNode v:c.visibleAnnotations)if(v.desc.equals("Lnet/minecraftforge/fml/common/Mod;"))for(int i=0;i<v.values.size();i+=2)if(v.values.get(i).equals("version")){v.values.set(i+1,"0.1.27-p36-mobius-planet");changed++;}if(changed!=1)throw new IOException("annotation");}
     ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);Path out=Paths.get(a[1],path);Files.createDirectories(out.getParent());Files.write(out,w.toByteArray());
    }
   }
 }
}
