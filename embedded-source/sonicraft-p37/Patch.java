import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.zip.*;
class Patch{static final String P="org/sonicraft/backport/";
 public static void main(String[] a)throws Exception{try(ZipFile z=new ZipFile(a[0])){for(String name:new String[]{"MobiusPortal","P34","MechanismSounds","SonicraftLegacy"}){
 String path=P+name+".class";ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(path))).accept(c,0);int hits=0;
 for(MethodNode m:c.methods)for(AbstractInsnNode n:m.instructions.toArray()){
 if(name.equals("MobiusPortal")&&m.name.equals("func_180634_a")&&n instanceof FieldInsnNode&&((FieldInsnNode)n).name.equals("field_72995_K")){
  InsnList i=new InsnList();i.add(new VarInsnNode(Opcodes.ALOAD,4));i.add(new FieldInsnNode(Opcodes.GETFIELD,"net/minecraft/entity/Entity","field_71093_bK","I"));i.add(new MethodInsnNode(Opcodes.INVOKESTATIC,P+"MobiusSpace","blocksPortalEntry","(I)Z",false));i.add(new InsnNode(Opcodes.IOR));m.instructions.insert(n,i);hits++;
 }else if(name.equals("P34")&&m.name.equals("panel")&&n instanceof MethodInsnNode&&((MethodInsnNode)n).name.equals("velocity")){
  InsnList i=new InsnList();i.add(new VarInsnNode(Opcodes.ALOAD,0));i.add(new VarInsnNode(Opcodes.ALOAD,1));i.add(new LdcInsnNode(hits==0?"spring.use":"jump_panel.use"));i.add(new MethodInsnNode(Opcodes.INVOKESTATIC,P+"MechanismSounds","play","(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/lang/String;)V",false));m.instructions.insert(n,i);hits++;
 }else if(name.equals("MechanismSounds")&&m.name.equals("<clinit>")&&n instanceof IntInsnNode&&((IntInsnNode)n).operand==11){
 ((IntInsnNode)n).operand=12;hits++;
 }else if(name.equals("MechanismSounds")&&m.name.equals("<clinit>")&&n instanceof LdcInsnNode&&"spike.hurt".equals(((LdcInsnNode)n).cst)){
 AbstractInsnNode end=n.getNext();if(end.getOpcode()!=Opcodes.AASTORE)throw new AssertionError();InsnList i=new InsnList();i.add(new InsnNode(Opcodes.DUP));i.add(new IntInsnNode(Opcodes.BIPUSH,11));i.add(new LdcInsnNode("jump_panel.use"));i.add(new InsnNode(Opcodes.AASTORE));m.instructions.insert(end,i);hits++;
 }}
 if(name.equals("SonicraftLegacy"))for(AnnotationNode v:c.visibleAnnotations)if(v.desc.endsWith("/Mod;"))for(int j=0;j<v.values.size();j+=2)if("version".equals(v.values.get(j))){v.values.set(j+1,"0.1.27-p37-warp");hits++;}
 if(hits!=(name.equals("MechanismSounds")||name.equals("P34")?2:1))throw new AssertionError(name+hits);ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);Path dest=Paths.get(a[1],path);Files.createDirectories(dest.getParent());Files.write(dest,w.toByteArray());
 }}}}
