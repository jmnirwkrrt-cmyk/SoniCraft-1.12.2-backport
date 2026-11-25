package org.sonicraft.backport;
import java.util.*;import net.minecraft.block.Block;import net.minecraft.block.state.IBlockState;import net.minecraft.nbt.*;import net.minecraft.util.*;import net.minecraft.util.math.BlockPos;import net.minecraft.world.*;import net.minecraft.world.storage.*;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;import net.minecraftforge.fml.common.gameevent.TickEvent;import net.minecraftforge.event.terraingen.PopulateChunkEvent;
@Mod.EventBusSubscriber(modid="sonicraft")
public final class Features45 {
 static final Def[] DEFS=Content.read("features45.json",Def[].class);static final Map<String,Def> BY_ID=new LinkedHashMap<>();static final Map<String,StructurePort.Template> CACHE=new HashMap<>();static Boolean enabled;
 static{for(Def d:DEFS)BY_ID.put(d.id,d);}
 static class Def{String id,template,original;String[] biomes,ground;int chance;long salt;int[] offset;boolean rotate,mirror;}
 static boolean enabled(){if(enabled==null){enabled=SonicraftLegacy.config.getBoolean("naturalSourceFeatures","structures",true,"Generate original small structure features in NEW chunks; waits for loaded neighbors. P45.");if(SonicraftLegacy.config.hasChanged())SonicraftLegacy.config.save();}return enabled;}
 public static boolean legacySmall(World w,Random r,BlockPos p,String name,String biome,int depth){return !managed(name)&&StructurePort.decorateSmall(w,r,p,name,biome,depth);}
 public static boolean managed(String name){for(Def d:DEFS)if(d.original.equals(name))return true;return false;}
 static Data data(World w){MapStorage m=w.getPerWorldStorage();Data d=(Data)m.getOrLoadData(Data.class,"sonicraft_features_p45");if(d==null){d=new Data("sonicraft_features_p45");m.setData("sonicraft_features_p45",d);}return d;}
 static boolean biome(Def d,World w,BlockPos p){for(String b:d.biomes)if(Content.BIOMES.get(b)!=null&&w.getBiome(p)==Content.BIOMES.get(b))return true;return false;}
 @SubscribeEvent public static void populated(PopulateChunkEvent.Post ev){World w=ev.getWorld();if(w.isRemote||!enabled())return;Data state=null;
  for(Def d:DEFS){Random r=new Random(w.getSeed()^ev.getChunkX()*341873128712L^ev.getChunkZ()*132897987541L^d.salt);if(r.nextInt(d.chance)!=0)continue;
   BlockPos p=new BlockPos(ev.getChunkX()*16+r.nextInt(16),64,ev.getChunkZ()*16+r.nextInt(16));if(!w.isBlockLoaded(p)||!biome(d,w,p))continue;
   ZoneBiome b=(ZoneBiome)w.getBiome(p);Block top=Content.BLOCKS.get(b.definition.top);int y=StructurePort.surfaceTerrain(w,p,top,false);if(y<1)continue;p=new BlockPos(p.getX(),y+1,p.getZ());
   if(d.ground.length>0){String id=w.getBlockState(p.down()).getBlock().getRegistryName().toString();if(!Arrays.asList(d.ground).contains(id))continue;}
   if(state==null)state=data(w);if(state.pending.size()>=2048)break;Job j=new Job(d.id,p,d.rotate?r.nextInt(4):0,d.mirror&&r.nextBoolean());state.pending.put(j.key(),j);state.markDirty();
  }
 }
 static BlockPos origin(Def d,Job j,StructurePort.Template t){int[] a=FeatureMath45.origin(t.width,t.depth,d.offset,j.rotation,j.mirror);return j.pos.add(a[0],a[1],a[2]);}
 static StructurePort.Template template(Def d,boolean mirror){if(!mirror)return StructurePort.get(d.template);StructurePort.Template t=CACHE.get(d.template);if(t!=null)return t;StructurePort.Template a=StructurePort.get(d.template);if(a==null||!a.usable())return a;
  if(!a.connectors.isEmpty()||!a.eggCrusherMarkers.isEmpty()||!a.portedBossMarkers.isEmpty())throw new IllegalStateException("Feature contains complex markers: "+d.template);
  t=new StructurePort.Template(a.name,a.width,a.height,a.depth);
  for(StructurePort.Cell c:a.blocks)t.blocks.add(new StructurePort.Cell(c.x,c.y,a.depth-1-c.z,c.block.withMirror(Mirror.LEFT_RIGHT),c.tileData==null?null:c.tileData.copy()));
  for(StructurePort.Spawn s:a.spawns)t.spawns.add(new StructurePort.Spawn(s.id,s.x,s.y,a.depth-s.z,180-s.yaw,s.noGravity,s.persistent));
  for(StructurePort.ItemSpawn s:a.itemSpawns)t.itemSpawns.add(new StructurePort.ItemSpawn(s.x,s.y,a.depth-s.z,s.id,s.count));
  CACHE.put(d.template,t);return t;
 }
 @SubscribeEvent public static void tick(TickEvent.WorldTickEvent ev){World w=ev.world;if(w.isRemote||ev.phase!=TickEvent.Phase.END||w.getTotalWorldTime()%20!=0||!enabled())return;Data state=data(w);if(state.pending.isEmpty())return;
  List<Job> jobs=new ArrayList<>(state.pending.values());int start=(int)((w.getTotalWorldTime()/20*97)%jobs.size()),attempts=0;
  for(int i=0;i<Math.min(256,jobs.size())&&attempts<2;i++){Job j=jobs.get((start+i)%jobs.size());if(!w.isBlockLoaded(j.pos))continue;Def d=BY_ID.get(j.id);if(d==null){state.pending.remove(j.key());state.markDirty();continue;}
   StructurePort.Template t=template(d,j.mirror);if(t==null||!t.usable()){state.pending.remove(j.key());state.rejected++;state.markDirty();continue;}
   Rotation r=Rotation.values()[j.rotation];BlockPos at=origin(d,j,t),end=at.add(StructurePort.rotatedWidth(t,r)-1,t.height-1,StructurePort.rotatedDepth(t,r)-1);if(!w.isAreaLoaded(at,end))continue;
   attempts++;state.pending.remove(j.key());state.markDirty();
   if(at.getY()<1||end.getY()>=255||!biome(d,w,j.pos)||!StructurePort.canPlace(w,at,t,r,true)){state.rejected++;continue;}
   // Preflight already checked natural terrain. false suppresses artificial foundations on floating source platforms.
   if(StructurePort.place(w,at,t,r,false))state.placed++;else state.rejected++;
  }
 }
 static class Job{final String id;final BlockPos pos;final int rotation;final boolean mirror;Job(String i,BlockPos p,int r,boolean m){id=i;pos=p;rotation=r&3;mirror=m;}String key(){return id+":"+pos.toLong();}}
 public static class Data extends WorldSavedData{final Map<String,Job> pending=new LinkedHashMap<>();int placed,rejected;public Data(){super("sonicraft_features_p45");}public Data(String n){super(n);}
  public void readFromNBT(NBTTagCompound n){pending.clear();placed=n.getInteger("Placed");rejected=n.getInteger("Rejected");NBTTagList list=n.getTagList("Pending",10);for(int i=0;i<Math.min(2048,list.tagCount());i++){NBTTagCompound t=list.getCompoundTagAt(i);if(!BY_ID.containsKey(t.getString("Id")))continue;Job j=new Job(t.getString("Id"),BlockPos.fromLong(t.getLong("Pos")),t.getInteger("Rotation"),t.getBoolean("Mirror"));pending.put(j.key(),j);}}
  public NBTTagCompound writeToNBT(NBTTagCompound n){n.setInteger("Placed",placed);n.setInteger("Rejected",rejected);NBTTagList list=new NBTTagList();for(Job j:pending.values()){NBTTagCompound t=new NBTTagCompound();t.setString("Id",j.id);t.setLong("Pos",j.pos.toLong());t.setInteger("Rotation",j.rotation);t.setBoolean("Mirror",j.mirror);list.appendTag(t);}n.setTag("Pending",list);return n;}
 }
}
