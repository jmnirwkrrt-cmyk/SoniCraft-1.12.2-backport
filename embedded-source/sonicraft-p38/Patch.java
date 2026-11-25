import org.objectweb.asm.*;import org.objectweb.asm.tree.*;import java.nio.file.*;import java.util.zip.*;
class Patch implements Opcodes {
 static final String P="org/sonicraft/backport/", D="(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/lang/String;)V";
 public static void main(String[] a)throws Exception{try(ZipFile z=new ZipFile(a[0])){for(String name:new String[]{"P34","MechanismSounds","SonicraftLegacy"}){
 String path=P+name+".class";ClassNode c=new ClassNode();new ClassReader(z.getInputStream(z.getEntry(path))).accept(c,0);int hits=0;
 for(MethodNode m:c.methods.toArray(new MethodNode[0])){
 if(name.equals("P34")&&m.name.equals("create")){m.name="createPrevious";MethodNode wrap=new MethodNode(ACC_STATIC,"create",m.desc,null,null);wrap.instructions.add(new VarInsnNode(ALOAD,0));wrap.instructions.add(new MethodInsnNode(INVOKESTATIC,P+"P38Gel","create",m.desc,false));wrap.instructions.add(new InsnNode(ARETURN));c.methods.add(wrap);hits++;}
 if(name.equals("P34")&&m.name.equals("syringe")){AbstractInsnNode n=m.instructions.getLast();while(n!=null&&!(n instanceof MethodInsnNode))n=n.getPrevious();if(!((MethodInsnNode)n).name.equals("set"))throw new AssertionError();InsnList i=new InsnList();i.add(new VarInsnNode(ALOAD,0));i.add(new VarInsnNode(ALOAD,1));i.add(new LdcInsnNode("chemical.change"));i.add(new MethodInsnNode(INVOKESTATIC,P+"MechanismSounds","playChemical",D,false));m.instructions.insert(n,i);hits++;}
 if(name.equals("MechanismSounds")&&m.name.equals("play")){MethodNode cp=new MethodNode(m.access,"playChemical",m.desc,null,null);m.accept(cp);for(AbstractInsnNode n:cp.instructions.toArray())if(n.getOpcode()==FCONST_1){cp.instructions.set(n,new LdcInsnNode(.9f));hits++;break;}c.methods.add(cp);}
 if(name.equals("MechanismSounds")&&m.name.equals("<clinit>"))for(AbstractInsnNode n:m.instructions.toArray()){
 if(n instanceof IntInsnNode&&((IntInsnNode)n).operand==12){((IntInsnNode)n).operand=15;hits++;}
 if(n instanceof LdcInsnNode&&"jump_panel.use".equals(((LdcInsnNode)n).cst)){InsnList i=new InsnList();int ix=12;for(String k:new String[]{"chemical.change","mega_mack_green.bounce","mega_mack_light_blue.bounce"}){i.add(new InsnNode(DUP));i.add(new IntInsnNode(BIPUSH,ix++));i.add(new LdcInsnNode(k));i.add(new InsnNode(AASTORE));}if(n.getNext().getOpcode()!=AASTORE)throw new AssertionError();m.instructions.insert(n.getNext(),i);hits++;}}
 }
 if(name.equals("SonicraftLegacy"))for(AnnotationNode ann:c.visibleAnnotations)if(ann.desc.endsWith("/Mod;"))for(int j=0;j<ann.values.size();j+=2)if("version".equals(ann.values.get(j))){ann.values.set(j+1,"0.1.27-p38-chemical");hits++;}
 if(hits!=(name.equals("P34")?2:name.equals("MechanismSounds")?3:1))throw new AssertionError(name+hits);
 ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(cw);Path dest=Paths.get(a[1],path);Files.createDirectories(dest.getParent());Files.write(dest,cw.toByteArray());
 }}}}
