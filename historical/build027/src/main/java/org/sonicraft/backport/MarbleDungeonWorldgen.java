package org.sonicraft.backport;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

/** Natural Marble Cave 1 generation on Mobius, deliberately deferred until all
 * occupied chunks are loaded. This never forces a neighbouring chunk to generate.
 * Candidate anchors, including uncompleted ones, survive world saves/restarts.
 * A completed/rejected candidate is processed only once; existing saved worlds
 * never receive retroactive anchors unless NEW chunks are populated there.
 */
public final class MarbleDungeonWorldgen {
 private static final String SAVE_ID = "sonicraft_marble_dungeons_v1";
 private static final int REGION = 6; // source structure_set: spacing=6
 private static final int SEPARATION = 4; // source structure_set: separation=4
 private static final int SALT = 47938921;
 private static final int MAX_PENDING = 1024;
 private static final String MARBLE = "marble_zone";
 private static boolean enabled = true;

 static void configure() {
  enabled = SonicraftLegacy.config.getBoolean("naturalMarbleDungeons", "structures", true,
   "Generate large Marble Cave dungeons only in newly populated Mobius Marble Zone chunks. " +
   "Pending structures are saved and placed when their whole footprint is loaded. " +
   "Disable for testing or if another mod edits nearby chunks.");
 }

 private static long fingerprint(long seed,int x,int z) {
  long h=seed ^ (0x9E3779B97F4A7C15L*x) ^ (0xC2B2AE3D27D4EB4FL*z);
  h^=h>>>30;h*=0xBF58476D1CE4E5B9L;
  h^=h>>>27;h*=0x94D049BB133111EBL;
  return h^(h>>>31);
 }
 private static boolean mobius(World world) {
  return world.provider.getDimension()==MobiusWorld.dimensionId;
 }
 private static boolean isMarble(World world,BlockPos pos){return world.getBiome(pos)==Content.BIOMES.get(MARBLE);}
 private static Data data(World world) {
  MapStorage storage=world.getPerWorldStorage();
  Data found=(Data)storage.getOrLoadData(Data.class,SAVE_ID);
  if(found==null){found=new Data(SAVE_ID);storage.setData(SAVE_ID,found);}
  return found;
 }

 @SubscribeEvent public void populated(PopulateChunkEvent.Post event) {
  World world=event.getWorld();
  if(world.isRemote||!enabled||!mobius(world))return;
  int cx=event.getChunkX(),cz=event.getChunkZ();
  // Mirror the 1.20 structure_set random-spread spacing/separation/salt.
  int rx=Math.floorDiv(cx,REGION),rz=Math.floorDiv(cz,REGION);
  Random rng=new Random(world.getSeed()+(long)rx*341873128712L+(long)rz*132897987541L+SALT);
  if(cx!=rx*REGION+rng.nextInt(REGION-SEPARATION)||
     cz!=rz*REGION+rng.nextInt(REGION-SEPARATION))return;
  BlockPos center=new BlockPos((cx<<4)+8,64,(cz<<4)+8);
  if(!isMarble(world,center))return;
  Data d=data(world);
  if(d.pending.size()>=MAX_PENDING||d.done.contains(center.toLong()))return;
  if(d.pending.add(center.toLong()))d.markDirty();
 }

 @SubscribeEvent public void worldTick(TickEvent.WorldTickEvent event) {
  if(event.phase!=TickEvent.Phase.END||event.world.isRemote||!enabled||!mobius(event.world))return;
  WorldServer world=(WorldServer)event.world;
  if(world.getTotalWorldTime()%80!=0)return;
  Data d=data(world);
  if(d.pending.isEmpty())return;
  int tried=0;
  // Rotate through pending anchors so incomplete structures at a chunk frontier
  // cannot indefinitely starve other candidates in the same dimension.
  List<Long> anchors=new ArrayList<>(d.pending);
  int start=(int)((world.getTotalWorldTime()/80)%anchors.size());
  int checked=0;
  while(checked<Math.min(64,anchors.size())&&tried<8){
   long packed=anchors.get((start+checked++)%anchors.size());
   BlockPos surface=BlockPos.fromLong(packed);
   // A pending anchor must itself be loaded. Never request/load neighbouring chunks.
   if(!world.isBlockLoaded(surface))continue;
   tried++;
   int y=world.getHeight(surface).getY();
   boolean accepted=y>=49&&y<=220&&isMarble(world,surface);
   if(accepted){
    BlockPos entry=new BlockPos(surface.getX(),y,surface.getZ());
    MarbleDungeon.Plan plan=MarbleDungeon.plan(entry,fingerprint(world.getSeed(),surface.getX(),surface.getZ()));
    // Check every planned footprint first. If a neighbour is absent, retain
    // the candidate and retry later rather than discarding it.
    boolean unloaded=false;
    for(MarbleDungeon.Piece piece:plan.pieces){
     BlockPos mid=new BlockPos((piece.minX()+piece.maxX())/2,y,(piece.minZ()+piece.maxZ())/2);
     if(!world.isAreaLoaded(new BlockPos(piece.minX(),0,piece.minZ()),new BlockPos(piece.maxX(),0,piece.maxZ()))) {
      unloaded=true;break;
     }
     if(!isMarble(world,mid)){accepted=false;break;}
    }
    if(unloaded)continue; // keep persisted candidate until every piece can be inspected
    if(accepted)MarbleDungeon.place(world,plan); // atomic preflight, may reject obstructed ground
   }
   // Once all nearby chunks are loaded, failed terrain checks are final. No repeated
   // pasting into explored or player-modified chunks, and no forced neighbours.
   d.pending.remove(packed);d.done.add(packed);d.markDirty();
  }
 }

 public static final class Data extends WorldSavedData {
  final Set<Long> pending=new LinkedHashSet<>();
  final Set<Long> done=new LinkedHashSet<>();
  public Data(){super(SAVE_ID);}
  public Data(String name){super(name);}
  @Override public void readFromNBT(NBTTagCompound nbt){
   pending.clear();done.clear();
   load(pending,nbt.getTagList("Pending",4));
   load(done,nbt.getTagList("Done",4));
  }
  @Override public NBTTagCompound writeToNBT(NBTTagCompound nbt){
   nbt.setTag("Pending",asList(pending));
   nbt.setTag("Done",asList(done));
   return nbt;
  }
  private static void load(Set<Long> destination,NBTTagList tags){
   for(int i=0;i<tags.tagCount();i++)destination.add(((NBTTagLong)tags.get(i)).getLong());
  }
  private static NBTTagList asList(Set<Long> entries){
   NBTTagList tags=new NBTTagList();
   for(Long entry:entries)tags.appendTag(new NBTTagLong(entry));
   return tags;
  }
 }
}
