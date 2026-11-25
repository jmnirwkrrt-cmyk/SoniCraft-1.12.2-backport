import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.zip.*;
class Patch implements Opcodes{
 static String P="org/sonicraft/backport/";static MethodInsnNode call(String n,String d){return new MethodInsnNode(INVOKESTATIC,P+"Util41",n,d,false);}
 public static void main(String[] a)throws Exception{try(ZipFile z=new ZipFile(a[0])){for(String name:new String[]{"BossRoster","BossRenderers","BossArena","CityComplexWorldgen","CityComplexWorldgen$Data","StructurePort","MechanismSounds","ExtraZoneMechanisms$Balloon"}){
 String file=P+name+".class";ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(file))).accept(c,0);int hit=0;
 for(MethodNode m:c.methods)for(AbstractInsnNode n:m.instructions.toArray()){
 if(name.equals("BossRoster")&&m.name.equals("<clinit>")&&n.getOpcode()==RETURN){InsnList i=new InsnList();i.add(new FieldInsnNode(GETSTATIC,c.name,"TYPES","Ljava/util/Map;"));i.add(new LdcInsnNode("metal_sonic_boss"));i.add(new LdcInsnNode(Type.getObjectType(P+"MetalArenaBoss")));i.add(new MethodInsnNode(INVOKEINTERFACE,"java/util/Map","put","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",true));i.add(new InsnNode(POP));m.instructions.insertBefore(n,i);hit++;}
 if(name.equals("BossRenderers")&&m.name.equals("register")&&n instanceof LdcInsnNode&&Type.getObjectType(P+"SoniBoss$SilverSonic").equals(((LdcInsnNode)n).cst)){InsnList i=new InsnList();i.add(new LdcInsnNode(Type.getObjectType(P+"MetalArenaBoss")));i.add(n.getNext().clone(null));i.add(n.getNext().getNext().clone(null));m.instructions.insertBefore(n,i);hit++;}
 if(name.equals("BossArena")&&m.name.equals("typeForMarker")&&n.getOpcode()==ACONST_NULL){InsnList i=new InsnList();i.add(new VarInsnNode(ALOAD,0));i.add(call("marker","(Ljava/lang/String;)Ljava/lang/String;"));m.instructions.insertBefore(n,i);m.instructions.remove(n);hit++;}
 if(n instanceof MethodInsnNode){MethodInsnNode j=(MethodInsnNode)n;
 if(name.equals("StructurePort")&&j.name.equals("contains")&&m.name.equals("get")){AbstractInsnNode p=n;for(int k=0;k<4;k++)p=p.getPrevious();if(p instanceof FieldInsnNode&&((FieldInsnNode)p).name.equals("BOSS_TRIGGER_IDS")){m.instructions.set(n,call("trigger","(Ljava/util/Set;Ljava/lang/Object;)Z"));hit++;}}
 if(name.equals("CityComplexWorldgen$Data")&&j.name.equals("containsValue")){m.instructions.set(n,call("known","(Ljava/util/Map;Ljava/lang/Object;)Z"));hit++;}
 if(name.equals("CityComplexWorldgen")&&j.name.equals("roadShift")){m.instructions.set(n,call("height",j.desc));hit++;}
 if(name.endsWith("$Balloon")&&j.name.equals("func_175698_g")){m.instructions.set(n,call("pop","(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z"));hit++;}}
 if(name.equals("MechanismSounds")&&m.name.equals("<clinit>")){
 if(n instanceof IntInsnNode&&((IntInsnNode)n).operand==17){((IntInsnNode)n).operand=18;hit++;}
 if(n instanceof LdcInsnNode&&"dash_ring.normal".equals(((LdcInsnNode)n).cst)){InsnList i=new InsnList();i.add(new InsnNode(DUP));i.add(new IntInsnNode(BIPUSH,17));i.add(new LdcInsnNode("balloon.pop"));i.add(new InsnNode(AASTORE));m.instructions.insert(n.getNext(),i);hit++;}}
 }if(hit!=(name.equals("MechanismSounds")?2:1))throw new AssertionError(name+hit);ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(cw);Path dest=Paths.get(a[1],file);Files.createDirectories(dest.getParent());Files.write(dest,cw.toByteArray());
 }}}}
