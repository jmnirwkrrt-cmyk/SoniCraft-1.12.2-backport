import java.nio.file.*;
import jdk.internal.org.objectweb.asm.*;
import jdk.internal.org.objectweb.asm.tree.*;
import jdk.internal.org.objectweb.asm.tree.analysis.*;
public class Patch implements Opcodes{
 static String dir="p29/classes/org/sonicraft/backport/",out="p29/work/",P="net/minecraft/block/properties/",S="net/minecraft/block/state/IBlockState",I="L"+P+"PropertyInteger;",W="net/minecraft/world/World",pos="Lnet/minecraft/util/math/BlockPos;",pr="L"+P+"IProperty;",st="L"+S+";",B="Lnet/minecraft/block/Block;";
 static ClassNode read(String n)throws Exception{ClassNode c=new ClassNode();new ClassReader(Files.readAllBytes(Paths.get(dir+n+".class"))).accept(c,0);return c;}
 static MethodNode m(ClassNode c,String name){return c.methods.stream().filter(x->x.name.equals(name)).findFirst().get();}
 static AbstractInsnNode op(MethodNode m,int op){for(AbstractInsnNode n:m.instructions.toArray())if(n.getOpcode()==op)return n;throw new Error();}
 static MethodInsnNode call(int opc,String c,String n,String d){return new MethodInsnNode(opc,c,n,d,opc==INVOKEINTERFACE);}
 static void frame(InsnList l,ClassNode c){l.add(new FieldInsnNode(GETSTATIC,c.name,"STATUS",I));}
 static void value(InsnList l){l.add(call(INVOKESTATIC,"java/lang/Integer","valueOf","(I)Ljava/lang/Integer;"));l.add(call(INVOKEINTERFACE,S,"func_177226_a","("+pr+"Ljava/lang/Comparable;)"+st));}
 static void get(InsnList l,ClassNode c){frame(l,c);l.add(call(INVOKEINTERFACE,S,"func_177229_b","("+pr+")Ljava/lang/Comparable;"));l.add(new TypeInsnNode(CHECKCAST,"java/lang/Integer"));l.add(call(INVOKEVIRTUAL,"java/lang/Integer","intValue","()I"));}
 static void add(MethodNode m,AbstractInsnNode at,InsnList l){m.instructions.insertBefore(at,l);}
 static void save(ClassNode c)throws Exception{ClassWriter w=new ClassWriter(ClassWriter.COMPUTE_MAXS);c.accept(w);byte[] bytes=w.toByteArray();ClassNode d=new ClassNode();new ClassReader(bytes).accept(d,0);for(MethodNode m:d.methods)new Analyzer<BasicValue>(new BasicVerifier()).analyze(d.name,m);Files.write(Paths.get(out+c.name.substring(c.name.lastIndexOf('/')+1)+".class"),bytes);}
 public static void main(String[]args)throws Exception{
 ClassNode c=read("MarbleCaveBlocks$Spring"),t=read("ExtraZoneMechanisms$ChemicalSpring"),dash=read("ZoneMachines$Dash");
 c.fields.add(new FieldNode(ACC_PRIVATE|ACC_STATIC|ACC_FINAL,"STATUS",I,null,null));
 for(String name:new String[]{"<clinit>","func_180650_b"}){MethodNode a=m(t,name);for(AbstractInsnNode n:a.instructions.toArray())if(n instanceof FieldInsnNode && ((FieldInsnNode)n).owner.equals(t.name))((FieldInsnNode)n).owner=c.name;
 if(name.equals("<clinit>"))a.instructions.set(op(a,ICONST_3),new InsnNode(ICONST_1));c.methods.add(a);}
 MethodNode a=m(c,"func_180661_e");a.instructions.set(op(a,ICONST_1),new InsnNode(ICONST_2));InsnList l=new InsnList();l.add(new InsnNode(DUP));l.add(new InsnNode(ICONST_1));frame(l,c);l.add(new InsnNode(AASTORE));a.instructions.insert(op(a,AASTORE),l);
 a=m(c,"func_176203_a");l=new InsnList();l.add(new IntInsnNode(BIPUSH,7));l.add(new InsnNode(IAND));a.instructions.insert(op(a,ILOAD),l);l=new InsnList();frame(l,c);l.add(new VarInsnNode(ILOAD,1));l.add(new InsnNode(ICONST_3));l.add(new InsnNode(IUSHR));l.add(new InsnNode(ICONST_1));l.add(new InsnNode(IAND));value(l);add(a,op(a,ARETURN),l);
 a=m(c,"func_176201_c");l=new InsnList();l.add(new VarInsnNode(ALOAD,1));get(l,c);l.add(new InsnNode(ICONST_3));l.add(new InsnNode(ISHL));l.add(new InsnNode(IOR));add(a,op(a,IRETURN),l);
 for(MethodNode mm:c.methods)if(mm.name.equals("<init>"))for(AbstractInsnNode n:mm.instructions.toArray())if(n instanceof MethodInsnNode && ((MethodInsnNode)n).name.equals("func_180632_j")){l=new InsnList();frame(l,c);l.add(new InsnNode(ICONST_0));value(l);add(mm,n,l);}
 a=m(c,"launch");l=new InsnList();l.add(new VarInsnNode(ALOAD,1));l.add(new VarInsnNode(ALOAD,2));l.add(call(INVOKEVIRTUAL,W,"func_180495_p","("+pos+")"+st));l.add(new VarInsnNode(ASTORE,3));l.add(new VarInsnNode(ALOAD,3));l.add(call(INVOKEINTERFACE,S,"func_177230_c","()"+B));l.add(new VarInsnNode(ALOAD,0));LabelNode skip=new LabelNode(),go=new LabelNode();l.add(new JumpInsnNode(IF_ACMPNE,skip));l.add(new VarInsnNode(ALOAD,3));get(l,c);l.add(new JumpInsnNode(IFEQ,go));l.add(skip);l.add(new FrameNode(F_SAME,0,null,0,null));l.add(new InsnNode(RETURN));l.add(go);l.add(new FrameNode(F_SAME,0,null,0,null));a.instructions.insert(l);
 for(AbstractInsnNode n:a.instructions.toArray()){if(n instanceof LdcInsnNode && Double.valueOf(.92).equals(((LdcInsnNode)n).cst))((LdcInsnNode)n).cst=1.4D;
 if(n instanceof MethodInsnNode && ((MethodInsnNode)n).name.equals("spring")){l=new InsnList();l.add(new VarInsnNode(ALOAD,1));l.add(new VarInsnNode(ALOAD,2));l.add(new VarInsnNode(ALOAD,3));frame(l,c);l.add(new InsnNode(ICONST_1));value(l);l.add(new InsnNode(ICONST_3));l.add(call(INVOKEVIRTUAL,W,"func_180501_a","("+pos+st+"I)Z"));l.add(new InsnNode(POP));l.add(new VarInsnNode(ALOAD,1));l.add(new VarInsnNode(ALOAD,2));l.add(new VarInsnNode(ALOAD,0));l.add(new IntInsnNode(BIPUSH,10));l.add(call(INVOKEVIRTUAL,W,"func_175684_a","("+pos+B+"I)V"));add(a,n,l);}}
 save(c);a=m(dash,"boost");for(AbstractInsnNode n:a.instructions.toArray()){if(n instanceof LdcInsnNode && Double.valueOf(1.25).equals(((LdcInsnNode)n).cst))((LdcInsnNode)n).cst=-1.4D;
 if(n instanceof FieldInsnNode && n.getOpcode()==PUTFIELD && ((FieldInsnNode)n).name.equals("field_70181_x")){AbstractInsnNode first=n;for(int i=0;i<5;i++)first=first.getPrevious();if(!(first instanceof VarInsnNode))throw new Error();while(first!=n){AbstractInsnNode next=first.getNext();a.instructions.remove(first);first=next;}a.instructions.remove(n);}}
 save(dash);System.out.println("Verified stack types and stack heights of patched methods.");}
}
