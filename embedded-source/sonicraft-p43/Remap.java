import org.objectweb.asm.*;import org.objectweb.asm.commons.*;import java.util.*;import java.util.zip.*;import java.nio.file.*;
public class Remap {
 public static void main(String[] a)throws Exception{
 final Map<String,String> f=new HashMap<>(),m=new HashMap<>(); boolean reverse=a[3].equals("reverse");String owner="";
 for(String line:Files.readAllLines(Paths.get(a[2]))){String[] s=line.trim().split(" +");if(!line.startsWith("\t")){owner=s[0];continue;} if(s.length==2)f.put(reverse?s[1]:owner+"/"+s[0],reverse?s[0]:s[1]);else m.put(reverse?s[2]:owner+"/"+s[0]+s[1],reverse?s[0]:s[2]);}
 final Map<String,ClassReader> classes=new HashMap<>(); for(int k=4;k<a.length;k++)try(ZipFile z=new ZipFile(a[k])){for(ZipEntry e:Collections.list(z.entries()))if(e.getName().endsWith(".class")){ClassReader c=new ClassReader(z.getInputStream(e));classes.put(c.getClassName(),c);}}
 Remapper r=new Remapper(){String lookup(Map<String,String> map,String o,String n,String d,Set<String> seen){if(o==null||!seen.add(o))return null;String v=map.get(o+"/"+n+d);if(v!=null)return v;ClassReader c=classes.get(o);if(c==null)return null;v=lookup(map,c.getSuperName(),n,d,seen);if(v!=null)return v;for(String it:c.getInterfaces()){v=lookup(map,it,n,d,seen);if(v!=null)return v;}return null;}
 public String mapFieldName(String o,String n,String d){String v=reverse?f.get(n):lookup(f,o,n,"",new HashSet<>());return v==null?n:v;}
 public String mapMethodName(String o,String n,String d){String v=reverse?m.get(n):lookup(m,o,n,d,new HashSet<>());return v==null?n:v;}};
 try(ZipFile z=new ZipFile(a[0]);ZipOutputStream out=new ZipOutputStream(Files.newOutputStream(Paths.get(a[1])))){for(ZipEntry e:Collections.list(z.entries())){if(e.isDirectory())continue;byte[] b;try(java.io.InputStream in=z.getInputStream(e)){java.io.ByteArrayOutputStream buf=new java.io.ByteArrayOutputStream();byte[] x=new byte[8192];int l;while((l=in.read(x))!=-1)buf.write(x,0,l);b=buf.toByteArray();}if(e.getName().endsWith(".class")){ClassWriter w=new ClassWriter(0);new ClassReader(b).accept(new ClassRemapper(w,r),0);b=w.toByteArray();}out.putNextEntry(new ZipEntry(e.getName()));out.write(b);out.closeEntry();}}
 }
}
