package org.sonicraft.backport;
import java.util.*;import net.minecraft.nbt.*;import net.minecraft.util.Rotation;import net.minecraft.util.math.BlockPos;import net.minecraft.world.*;import net.minecraft.world.biome.Biome;import net.minecraft.world.storage.*;
public final class Arenas47 {
 static final String SAVE="sonicraft_arena_regions_p47";
 static boolean boss(StructurePort.Template t){return t!=null&&(!t.eggCrusherMarkers.isEmpty()||!t.portedBossMarkers.isEmpty());}
 static BlockPos anchor(BlockPos p,StructurePort.Template t,Rotation r){BlockPos local=!t.eggCrusherMarkers.isEmpty()?t.eggCrusherMarkers.get(0):t.portedBossMarkers.values().iterator().next().get(0);return StructurePort.at(t,p,new StructurePort.Cell(local.getX(),local.getY(),local.getZ(),net.minecraft.init.Blocks.AIR.getDefaultState(),null),r);}
 static long cell(BlockPos p){return Region47.key(p.getX()>>4,p.getZ()>>4);}
 static BlockPos center(long k){return new BlockPos(((int)(k>>32)<<4)+8,64,((int)k<<4)+8);}
 static String biome(World w,long k){Biome b=w.getBiomeProvider().getBiome(center(k));return b.getRegistryName().toString();}
 static Data data(World w){MapStorage s=w.getPerWorldStorage();Data d=(Data)s.getOrLoadData(Data.class,SAVE);if(d==null){d=new Data(SAVE);s.setData(SAVE,d);}if(!d.imported){
  WorldSavedData old=s.getOrLoadData(BossArena.Data.class,"sonicraft_boss_encounters_p12");importOld(w,d,old,"Markers");old=s.getOrLoadData(EggCrusherArena.Data.class,"sonicraft_egg_crusher_arenas_v1");importOld(w,d,old,"Triggers");d.imported=true;d.markDirty();}return d;}
 static void importOld(World w,Data d,WorldSavedData old,String list){if(old==null)return;NBTTagList n=old.writeToNBT(new NBTTagCompound()).getTagList(list,10);for(int i=0;i<n.tagCount();i++)add(w,d,BlockPos.fromLong(n.getCompoundTagAt(i).getLong("Pos")));}
 static void add(World w,Data d,BlockPos p){long k=cell(p);d.claims.computeIfAbsent(biome(w,k),v->new HashSet<>()).add(k);d.markDirty();}
 public static boolean allowed(World w,BlockPos p,StructurePort.Template t,Rotation r){if(!boss(t))return true;Data d=data(w);long start=cell(anchor(p,t,r));String b=biome(w,start);Set<Long> claims=d.claims.get(b);if(claims==null||claims.isEmpty())return true;if(claims.contains(start)||d.blocked.contains(start))return false;
  Set<Long> component=new HashSet<>();int result=Region47.connected(start,claims,k->b.equals(biome(w,k)),8192,component);
  if(result==1){if(d.blocked.size()>65536)d.blocked.clear();d.blocked.addAll(component);}return result==0;
 }
 public static void placed(World w,BlockPos p,StructurePort.Template t,Rotation r){if(boss(t))add(w,data(w),anchor(p,t,r));}
 public static class Data extends WorldSavedData {
  Map<String,Set<Long>> claims=new HashMap<>();Set<Long> blocked=new HashSet<>();boolean imported;
  public Data(){super(SAVE);}public Data(String n){super(n);}
  public void readFromNBT(NBTTagCompound n){claims.clear();blocked.clear();imported=n.getBoolean("Imported");NBTTagList list=n.getTagList("Claims",10);for(int i=0;i<list.tagCount();i++){NBTTagCompound c=list.getCompoundTagAt(i);claims.computeIfAbsent(c.getString("Biome"),v->new HashSet<>()).add(c.getLong("Cell"));}}
  public NBTTagCompound writeToNBT(NBTTagCompound n){NBTTagList list=new NBTTagList();for(Map.Entry<String,Set<Long>> e:claims.entrySet())for(long k:e.getValue()){NBTTagCompound c=new NBTTagCompound();c.setString("Biome",e.getKey());c.setLong("Cell",k);list.appendTag(c);}n.setTag("Claims",list);n.setBoolean("Imported",imported);return n;}
 }
}
