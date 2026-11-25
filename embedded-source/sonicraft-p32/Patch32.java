import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;
import java.nio.file.*;import java.util.*;import java.util.jar.*;
public class Patch32 implements Opcodes{
 static String pkg="org/sonicraft/backport/", C=pkg+"CityComplexWorldgen",H=pkg+"P32Sites", P="net/minecraft/util/math/BlockPos", W="net/minecraft/world/World", S=pkg+"StructurePort";
 static String terrain="(L"+W+";L"+P+";Lnet/minecraft/block/Block;Z)I";
 static MethodInsnNode call(String o,String n,String d){return new MethodInsnNode(INVOKESTATIC,o,n,d,false);}
 static VarInsnNode v(int o,int i){return new VarInsnNode(o,i);}
 static InsnNode op(int o){return new InsnNode(o);}
 static MethodNode method(ClassNode c,String n){return c.methods.stream().filter(m->m.name.equals(n)).findFirst().get();}
 static AbstractInsnNode prev(AbstractInsnNode i){do{i=i.getPrevious();}while(i.getOpcode()<0);return i;}
 static void chk(boolean x){if(!x)throw new AssertionError();}
 public static void main(String[] args)throws Exception{
 JarFile jar=new JarFile(args[0]);ClassNode c=new ClassNode();new ClassReader(jar.getInputStream(jar.getJarEntry(C+".class"))).accept(c,0);
 MethodNode cl=method(c,"<clinit>");for(String biome:new String[]{"green_hill_zone","emerald_hill_zone"}){
 InsnList a=new InsnList();a.add(new FieldInsnNode(GETSTATIC,C,"BIOME_COMPLEX","Ljava/util/Map;"));a.add(new LdcInsnNode(biome));a.add(new LdcInsnNode("tailslab"));a.add(new MethodInsnNode(INVOKEINTERFACE,"java/util/Map","put","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",true));a.add(op(POP));cl.instructions.insertBefore(cl.instructions.getLast().getOpcode()==RETURN?cl.instructions.getLast():Arrays.stream(cl.instructions.toArray()).filter(i->i.getOpcode()==RETURN).findFirst().get(),a);}
 MethodNode m=method(c,"chunkPopulated");int changed=0;
 // Preserve both original grid draws and both branch targets. Record city membership explicitly.
 // A lab-only candidate goes through the same persistent queue as other natural complexes.
 for(AbstractInsnNode i:m.instructions.toArray())if(i.getOpcode()==IF_ICMPEQ){
  JumpInsnNode j=(JumpInsnNode)i;LabelNode accept=j.label;
  AbstractInsnNode reject=Arrays.stream(m.instructions.toArray()).filter(q->q.getOpcode()==IF_ICMPNE).map(q->((JumpInsnNode)q).label).findFirst().get();
  AbstractInsnNode ret=reject;while(ret.getOpcode()<0)ret=ret.getNext();chk(ret.getOpcode()==RETURN);
  // Don't introduce locals into existing stack-map frames: decide grid membership again only for candidates.
  InsnList a=new InsnList();a.add(v(ALOAD,2));a.add(new MethodInsnNode(INVOKEVIRTUAL,W,"func_72905_C","()J",false));a.add(v(ILOAD,3));a.add(v(ILOAD,4));a.add(call(H,"lab","(JII)Z"));a.add(new JumpInsnNode(IFNE,accept));m.instructions.insertBefore(ret,a);changed++;
 }
 for(AbstractInsnNode i:m.instructions.toArray())if(i instanceof MethodInsnNode&&((MethodInsnNode)i).name.equals("candidateBiome")){
 InsnList a=new InsnList();a.add(v(ALOAD,2));a.add(new MethodInsnNode(INVOKEVIRTUAL,W,"func_72905_C","()J",false));a.add(v(ILOAD,3));a.add(v(ILOAD,4));a.add(new FieldInsnNode(GETSTATIC,C,"spacing","I"));a.add(call(C,"filterSite","(Ljava/lang/String;JIII)Ljava/lang/String;"));m.instructions.insert(i,a);changed++;}
 chk(changed==2);
 MethodNode filter=new MethodNode(ACC_PRIVATE|ACC_STATIC,"filterSite","(Ljava/lang/String;JIII)Ljava/lang/String;",null,null);
 // Reuse original hash & java.util.Random draws. for each axis: region + offset == chunk.
 org.objectweb.asm.commons.GeneratorAdapter g=new org.objectweb.asm.commons.GeneratorAdapter(filter,filter.access,filter.name,filter.desc);
 Type I=Type.INT_TYPE,R=Type.getType(Random.class); int[] reg={g.newLocal(I),g.newLocal(I)};
 for(int k=0;k<2;k++){g.loadArg(2+k);g.loadArg(4);g.visitMethodInsn(INVOKESTATIC,"java/lang/Math","floorDiv","(II)I",false);g.storeLocal(reg[k]);}
 g.newInstance(R);g.dup();g.loadArg(1);g.loadLocal(reg[0]);g.loadLocal(reg[1]);g.visitMethodInsn(INVOKESTATIC,C,"mix","(JII)J",false);g.visitMethodInsn(INVOKESPECIAL,"java/util/Random","<init>","(J)V",false);int rnd=g.newLocal(R);g.storeLocal(rnd);
 g.loadArg(0);for(int k=0;k<2;k++){g.loadArg(2+k);g.loadLocal(reg[k]);g.loadArg(4);g.math(g.MUL,I);g.math(g.SUB,I);g.loadLocal(rnd);g.loadArg(4);g.visitMethodInsn(INVOKEVIRTUAL,"java/util/Random","nextInt","(I)I",false);g.math(g.XOR,I);if(k==1)g.math(g.OR,I);}
 // x==0 -> 1, x>0 -> 0; both operands for XOR are within [0,spacing).
 g.push(1);g.math(g.SUB,I);g.push(31);g.math(g.USHR,I);g.loadArg(1);g.loadArg(2);g.loadArg(3);g.visitMethodInsn(INVOKESTATIC,H,"lab","(JII)Z",false);g.visitMethodInsn(INVOKESTATIC,H,"select","(Ljava/lang/String;ZZ)Ljava/lang/String;",false);g.returnValue();g.endMethod();c.methods.add(filter);
 MethodNode med=new MethodNode(ACC_PRIVATE|ACC_STATIC,"cityTerrain",terrain,null,null);g=new org.objectweb.asm.commons.GeneratorAdapter(med,med.access,med.name,med.desc);g.push(9);g.newArray(I);int k=0;
 for(int dx:new int[]{-7,0,7})for(int dz:new int[]{-7,0,7}){g.dup();g.push(k++);g.loadArg(0);g.loadArg(1);g.push(dx);g.push(0);g.push(dz);g.visitMethodInsn(INVOKEVIRTUAL,P,"func_177982_a","(III)L"+P+";",false);g.loadArg(2);g.loadArg(3);g.visitMethodInsn(INVOKESTATIC,S,"surfaceTerrain",terrain,false);g.arrayStore(I);}
 g.visitMethodInsn(INVOKESTATIC,H,"median","([I)I",false);g.returnValue();g.endMethod();c.methods.add(med);
 MethodNode tick=method(c,"worldTick");changed=0;for(AbstractInsnNode i:tick.instructions.toArray())if(i instanceof MethodInsnNode){
 MethodInsnNode t=(MethodInsnNode)i;if(t.name.equals("surfaceTerrain")){chk(t.desc.equals(terrain));t.owner=C;t.name="cityTerrain";changed++;}
 if(t.name.equals("rotatedDepth")){AbstractInsnNode z=prev(prev(prev(prev(i)))); // zero Y before the two piece field loads
 while(z.getOpcode()!=ICONST_0)z=prev(z);
 InsnList a=new InsnList();a.add(v(ALOAD,10));a.add(call(H,"roadShift","(Ljava/lang/String;)I"));tick.instructions.insertBefore(z,a);tick.instructions.remove(z);changed++;}
 }chk(changed==2);
 ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(cw);byte[] out=cw.toByteArray();ClassNode check=new ClassNode();new ClassReader(out).accept(check,0);for(MethodNode f:check.methods)new Analyzer<>(new BasicVerifier()).analyze(C,f);
 Files.write(Paths.get(args[1]),out);
 }
}
