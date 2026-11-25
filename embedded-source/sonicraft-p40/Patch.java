import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.zip.*;
class Patch implements Opcodes {
 static final String P="org/sonicraft/backport/",W="Lnet/minecraft/world/World;",B="Lnet/minecraft/util/math/BlockPos;",E="Lnet/minecraft/entity/Entity;",F="Lnet/minecraft/util/EnumFacing;";
 static void load(InsnList i,int...v){for(int k:v)i.add(new VarInsnNode(ALOAD,k));}
 static MethodInsnNode call(String owner,String n,String d){return new MethodInsnNode(INVOKESTATIC,P+owner,n,d,false);}
 public static void main(String[] a)throws Exception{try(ZipFile z=new ZipFile(a[0])){for(String name:new String[]{"ZoneMotionBlocks$DashRing","ZoneMachines$Dash","MechanismSounds"}){
 String path=P+name+".class";ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(path))).accept(c,0);int hit=0;
 for(MethodNode m:c.methods){
 if(name.endsWith("$DashRing")&&m.name.equals("func_180634_a")){
 m.instructions.clear();m.tryCatchBlocks.clear();m.localVariables=null;InsnList i=m.instructions;LabelNode end=new LabelNode();load(i,1,4);i.add(call("ZoneMotionBlocks","canLaunch","("+W+E+")Z"));i.add(new JumpInsnNode(IFEQ,end));load(i,1,2,4);i.add(call("P40","allow","("+W+B+E+")Z"));i.add(new JumpInsnNode(IFEQ,end));load(i,1,2,3);i.add(new FieldInsnNode(GETSTATIC,c.name,"FACING","Lnet/minecraft/block/properties/PropertyDirection;"));i.add(new MethodInsnNode(INVOKEINTERFACE,"net/minecraft/block/state/IBlockState","func_177229_b","(Lnet/minecraft/block/properties/IProperty;)Ljava/lang/Comparable;",true));i.add(new TypeInsnNode(CHECKCAST,"net/minecraft/util/EnumFacing"));load(i,4);i.add(call("P40","ring","("+W+B+F+E+")V"));i.add(end);i.add(new FrameNode(F_SAME,0,null,0,null));i.add(new InsnNode(RETURN));hit++;}
 if(name.endsWith("$Dash")&&m.name.equals("boost"))for(AbstractInsnNode n:m.instructions.toArray())if(n instanceof FieldInsnNode&&((FieldInsnNode)n).name.equals("field_70133_I")){InsnList i=new InsnList();load(i,1,2);i.add(new LdcInsnNode("dash_pad.classic"));i.add(call("MechanismSounds","play","("+W+B+"Ljava/lang/String;)V"));m.instructions.insert(n,i);hit++;}
 if(name.equals("MechanismSounds")&&m.name.equals("<clinit>"))for(AbstractInsnNode n:m.instructions.toArray()){
 if(n instanceof IntInsnNode&&((IntInsnNode)n).operand==15){((IntInsnNode)n).operand=17;hit++;}
 if(n instanceof LdcInsnNode&&"mega_mack_light_blue.bounce".equals(((LdcInsnNode)n).cst)){InsnList i=new InsnList();int k=15;for(String s:new String[]{"dash_pad.classic","dash_ring.normal"}){i.add(new InsnNode(DUP));i.add(new IntInsnNode(BIPUSH,k++));i.add(new LdcInsnNode(s));i.add(new InsnNode(AASTORE));}m.instructions.insert(n.getNext(),i);hit++;}}
 }
 if(hit!=(name.equals("MechanismSounds")?2:1))throw new AssertionError();ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(cw);Path dest=Paths.get(a[1],path);Files.createDirectories(dest.getParent());Files.write(dest,cw.toByteArray());
 }}}}
