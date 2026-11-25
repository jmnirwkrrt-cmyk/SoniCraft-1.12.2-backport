import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.*;import java.util.zip.*;
public class Patch48 implements Opcodes {
 static final String P="org/sonicraft/backport/";
 public static void main(String[] a)throws Exception{try(ZipFile z=new ZipFile(a[0])){for(String name:new String[]{"SonicraftLegacy","ZoneWorldType","BadnikRenderers","Badniks","StructurePort"}){ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(P+name+".class"))).accept(c,0);int hits=0;MethodNode extra=null;
 for(MethodNode m:c.methods){
  if(name.equals("Badniks")&&m.name.equals("entityClass")){
   m.name="entityClass47";extra=new MethodNode(m.access,"entityClass",m.desc,null,null);InsnList l=extra.instructions;LabelNode fallback=new LabelNode();
   l.add(new VarInsnNode(ALOAD,0));l.add(new MethodInsnNode(INVOKESTATIC,P+"Entities48","type",m.desc,false));l.add(new VarInsnNode(ASTORE,1));l.add(new VarInsnNode(ALOAD,1));l.add(new JumpInsnNode(IFNULL,fallback));l.add(new VarInsnNode(ALOAD,1));l.add(new InsnNode(ARETURN));l.add(fallback);l.add(new FrameNode(F_APPEND,1,new Object[]{"java/lang/Class"},0,null));l.add(new VarInsnNode(ALOAD,0));l.add(new MethodInsnNode(INVOKESTATIC,P+"Badniks","entityClass47",m.desc,false));l.add(new InsnNode(ARETURN));hits++;
  }
  for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode x=(MethodInsnNode)i;
   if(name.equals("SonicraftLegacy")&&x.owner.equals("net/minecraftforge/common/BiomeManager")&&(x.name.equals("addBiome")||x.name.equals("addSpawnBiome"))){x.owner=P+"World48";hits++;}
   if(name.equals("ZoneWorldType")&&x.owner.equals(P+"RegionalBiomes")&&x.name.equals("all")){x.owner=P+"World48";hits++;}
   if(name.equals("BadnikRenderers")&&x.owner.equals(P+"Render47")){x.owner=P+"Render48";hits++;}
   if(name.equals("StructurePort")&&x.owner.equals(P+"Vehicles47")&&x.name.equals("safe")){x.owner=P+"Entities48";hits++;}
  }
 }
 if(extra!=null)c.methods.add(extra);int expected=name.equals("SonicraftLegacy")?2:1;if(hits!=expected)throw new AssertionError(name+" hits "+hits);ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);Path p=Paths.get(a[1],P+name+".class");Files.createDirectories(p.getParent());Files.write(p,w.toByteArray());System.out.println("Patched "+name+": "+hits);
 }}}
}
