import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.zip.*;
public class Patch46 implements Opcodes {
 static final String P="org/sonicraft/backport/";
 public static void main(String[] a)throws Exception{try(ZipFile z=new ZipFile(a[0])){for(String name:new String[]{"P34","BadnikRenderers","StructurePort","CityComplexWorldgen","LegacyBlocks"}){ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(P+name+".class"))).accept(c,0);int hits=0;
  for(MethodNode m:c.methods)for(AbstractInsnNode i:m.instructions.toArray()){
   if(i instanceof MethodInsnNode){MethodInsnNode x=(MethodInsnNode)i;
    if(name.equals("LegacyBlocks")&&m.name.equals("p33create")&&x.owner.equals("java/lang/String")&&x.name.equals("startsWith")&&x.getPrevious() instanceof LdcInsnNode&&"bulk_".equals(((LdcInsnNode)x.getPrevious()).cst)){x.setOpcode(INVOKESTATIC);x.owner=P+"Blocks46";x.name="bulkKind";x.desc="(Ljava/lang/String;Ljava/lang/String;)Z";hits++;}
    if(name.equals("P34")&&m.name.equals("create")&&x.owner.equals(P+"Blocks43")){x.owner=P+"Blocks46";hits++;}
    if(name.equals("BadnikRenderers")&&x.owner.equals(P+"Render43")){x.owner=P+"Render46";hits++;}
    if(name.equals("StructurePort")&&x.owner.equals(P+"Entities43")&&x.name.equals("safe")){x.owner=P+"Entities46";hits++;}
   }
   // Keep unloaded plans pending instead of irreversibly truncating to a root after twenty seconds.
   if(name.equals("CityComplexWorldgen")&&m.name.equals("worldTick")&&i instanceof IntInsnNode&&((IntInsnNode)i).operand==20&&i.getNext() instanceof JumpInsnNode&&i.getNext().getOpcode()==IF_ICMPGE){m.instructions.set(i,new LdcInsnNode(Integer.MAX_VALUE));hits++;}
  }
  if(hits!=1)throw new AssertionError(name+" hits="+hits);ClassWriter w=new ClassWriter(0);c.accept(w);Path p=Paths.get(a[1],P+name+".class");Files.createDirectories(p.getParent());Files.write(p,w.toByteArray());System.out.println("Patched "+name+": "+hits);
 }}}
}
