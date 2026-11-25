import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.*;import java.util.zip.*;
public class Patch47 implements Opcodes {
 static final String P="org/sonicraft/backport/";
 static MethodInsnNode call(String c,String n,String d){return new MethodInsnNode(INVOKESTATIC,P+c,n,d,false);}
 static void args(InsnList l,boolean natural){for(int i=0;i<4;i++)l.add(new VarInsnNode(ALOAD,i));if(natural)l.add(new VarInsnNode(ILOAD,4));}
 static MethodNode wrapper(String name,String desc,boolean record){
  MethodNode m=new MethodNode(ACC_STATIC,name,desc,null,null);InsnList l=m.instructions;LabelNode allowed=new LabelNode(),done=new LabelNode();
  l.add(new VarInsnNode(ILOAD,4));l.add(new JumpInsnNode(IFEQ,allowed));args(l,false);l.add(call("Arenas47","allowed",desc.replace(";Z)",";)")));l.add(new JumpInsnNode(IFNE,allowed));l.add(new InsnNode(ICONST_0));l.add(new InsnNode(IRETURN));l.add(allowed);l.add(new FrameNode(F_SAME,0,null,0,null));args(l,true);l.add(call("StructurePort",name+"46",desc));
  if(record){l.add(new VarInsnNode(ISTORE,5));l.add(new VarInsnNode(ILOAD,5));l.add(new JumpInsnNode(IFEQ,done));l.add(new VarInsnNode(ILOAD,4));l.add(new JumpInsnNode(IFEQ,done));args(l,false);l.add(call("Arenas47","placed",desc.replace(";Z)Z",";)V")));l.add(done);l.add(new FrameNode(F_APPEND,1,new Object[]{INTEGER},0,null));l.add(new VarInsnNode(ILOAD,5));}
  l.add(new InsnNode(IRETURN));return m;
 }
 public static void main(String[] a)throws Exception{try(ZipFile z=new ZipFile(a[0])){for(String name:new String[]{"P34","BadnikRenderers","StructurePort","SonicraftLegacy"}){ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(P+name+".class"))).accept(c,0);int hits=0;List<MethodNode> extra=new ArrayList<>();
  for(MethodNode m:c.methods){
   if(name.equals("StructurePort")&&(m.name.equals("place")||m.name.equals("canPlace"))){extra.add(wrapper(m.name,m.desc,m.name.equals("place")));m.name+="46";hits++;}
   if(name.equals("SonicraftLegacy")&&m.name.equals("init")){m.instructions.insert(new MethodInsnNode(INVOKESTATIC,"software/bernie/geckolib3/GeckoLib","initialize","()V",false));hits++;}
   for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode){MethodInsnNode x=(MethodInsnNode)i;
    if(name.equals("P34")&&m.name.equals("create")&&x.owner.equals(P+"Blocks46")){x.owner=P+"Blocks47";hits++;}
    if(name.equals("BadnikRenderers")&&x.owner.equals(P+"Render46")){x.owner=P+"Render47";hits++;}
    if(name.equals("StructurePort")&&x.owner.equals(P+"Entities46")&&x.name.equals("safe")){x.owner=P+"Vehicles47";hits++;}
    if(name.equals("SonicraftLegacy")&&m.name.equals("<init>")&&x.owner.equals("software/bernie/geckolib3/GeckoLib")&&x.name.equals("initialize")){m.instructions.remove(i);hits++;}
   }
  }c.methods.addAll(extra);int expected=name.equals("StructurePort")?3:name.equals("SonicraftLegacy")?2:1;if(hits!=expected)throw new AssertionError(name+" hits="+hits);ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);Path p=Paths.get(a[1],P+name+".class");Files.createDirectories(p.getParent());Files.write(p,w.toByteArray());System.out.println("Patched "+name+": "+hits);
 }}}
}
