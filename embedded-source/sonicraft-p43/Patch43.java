import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import org.objectweb.asm.tree.analysis.*;import java.nio.file.*;import java.util.zip.*;
public class Patch43 implements Opcodes {
 static final String P="org/sonicraft/backport/";
 static MethodInsnNode call(String owner,String name,String desc){return new MethodInsnNode(INVOKESTATIC,P+owner,name,desc,false);}
 public static void main(String[] args)throws Exception{try(ZipFile z=new ZipFile(args[0])){
 for(String name:new String[]{"P34","ZoneTerrain","StructurePort","CityComplexWorldgen","BadnikRenderers","LegacyCharacter"}){
  ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(P+name+".class"))).accept(c,0);int hits=0;
  if(name.equals("LegacyCharacter")){MethodNode tick=new MethodNode(ACC_PUBLIC,"onLivingUpdate","()V",null,null);tick.instructions.add(new VarInsnNode(ALOAD,0));tick.instructions.add(new MethodInsnNode(INVOKESPECIAL,c.superName,"onLivingUpdate","()V",false));tick.instructions.add(new VarInsnNode(ALOAD,0));tick.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"net/minecraft/entity/EntityLivingBase","updateArmSwingProgress","()V",false));tick.instructions.add(new InsnNode(RETURN));c.methods.add(tick);hits++;}
  for(MethodNode m:c.methods){
   if(name.equals("StructurePort")&&m.name.equals("canPlace")){
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();
    for(int i=0;i<4;i++)m.instructions.add(new VarInsnNode(ALOAD,i));m.instructions.add(new VarInsnNode(ILOAD,4));m.instructions.add(call("Placement43","canPlace",m.desc));m.instructions.add(new InsnNode(IRETURN));hits++;continue;
   }
   if(name.equals("P34")&&(m.name.equals("cannon")||m.name.equals("create"))){
    m.instructions.clear();m.tryCatchBlocks.clear();if(m.localVariables!=null)m.localVariables.clear();int count=m.name.equals("cannon")?4:1;for(int i=0;i<count;i++)m.instructions.add(new VarInsnNode(ALOAD,i));m.instructions.add(call(count==4?"Physics43":"Blocks43",m.name,m.desc));m.instructions.add(new InsnNode(count==4?RETURN:ARETURN));hits++;continue;
   }
   for(AbstractInsnNode n:m.instructions.toArray()){
    if(name.equals("ZoneTerrain")&&m.name.equals("replace")&&n.getOpcode()==RETURN){InsnList l=new InsnList();l.add(new VarInsnNode(ALOAD,0));l.add(call("Oil43","generate","(Lnet/minecraft/world/chunk/Chunk;)V"));m.instructions.insertBefore(n,l);hits++;}
    if(name.equals("BadnikRenderers")&&m.name.equals("register")&&n.getOpcode()==RETURN){m.instructions.insertBefore(n,call("Render43","register","()V"));hits++;}
    if(n instanceof MethodInsnNode){MethodInsnNode j=(MethodInsnNode)n;
     if(name.equals("CityComplexWorldgen")&&j.owner.equals(P+"Util41")&&j.name.equals("height")){j.owner=P+"Sites43";hits++;}
     if(name.equals("StructurePort")&&m.name.equals("get")&&j.name.equals("contains")&&j.owner.equals("java/util/Set")){
      AbstractInsnNode prev=n.getPrevious();if(prev instanceof VarInsnNode)prev=prev.getPrevious();
      if(prev instanceof FieldInsnNode&&((FieldInsnNode)prev).name.equals("SAFE_ENTITIES")){m.instructions.set(n,call("Entities43","safe","(Ljava/util/Set;Ljava/lang/Object;)Z"));hits++;}
     }
    }
   }
  }
  int expected=(name.equals("P34")||name.equals("StructurePort"))?2:1;if(hits!=expected)throw new AssertionError(name+" hooks="+hits);
  ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);byte[] data=w.toByteArray();ClassNode verify=new ClassNode();new ClassReader(data).accept(verify,0);for(MethodNode m:verify.methods)if(m.instructions.size()>0)new Analyzer<BasicValue>(new BasicVerifier()).analyze(c.name,m);
  Path dest=Paths.get(args[1],P+name+".class");Files.createDirectories(dest.getParent());Files.write(dest,data);System.out.println(name+": "+hits+" verified hooks");
 }
 }}
}
