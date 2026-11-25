import java.nio.file.*;import org.objectweb.asm.*;import org.objectweb.asm.tree.*;
public class Patch { public static void main(String[] args)throws Exception{
Path p=Paths.get(args[0]);ClassNode c=new ClassNode();new ClassReader(Files.readAllBytes(p)).accept(c,0);int changed=0;
for(MethodNode m:c.methods)for(AbstractInsnNode a:m.instructions.toArray())if(a instanceof LdcInsnNode&&"minecraft:chests/".equals(((LdcInsnNode)a).cst)){AbstractInsnNode n=a.getNext();if(!(n instanceof MethodInsnNode))throw new AssertionError();MethodInsnNode call=(MethodInsnNode)n;if(!call.owner.equals("java/lang/String")||!call.name.equals("startsWith"))throw new AssertionError();m.instructions.set(n,new MethodInsnNode(Opcodes.INVOKESTATIC,"org/sonicraft/backport/ZoneChestLoot","accept","(Ljava/lang/String;Ljava/lang/String;)Z",false));changed++;}
if(changed!=1)throw new AssertionError(changed);ClassWriter w=new ClassWriter(0);c.accept(w);Files.write(p,w.toByteArray());}}
