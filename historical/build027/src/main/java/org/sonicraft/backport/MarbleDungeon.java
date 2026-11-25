package org.sonicraft.backport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Small, bounded, deterministic subset of the 1.20.1 Marble Cave jigsaw pools.
 * Operator-invoked only in this version; never loads chunks or mutates the world
 * during planning. All assembled pieces undergo a full preflight prior to paste.
 * No foreign entity NBT, unfinished mechanisms or jigsaw tile entities are loaded.
 */
final class MarbleDungeon {
 private MarbleDungeon(){}
 static final String ROOT="marble_cave_1";
 static final String ENTRANCE="marble_cave_1_spawner";
 static final int MAX_PARTS=10,MAX_DEPTH=4;
 private static final Map<String,List<String>> POOLS=new HashMap<>();
 static {
  POOLS.put("sonicraft:marble_cave_1_dungeon",Arrays.asList(ROOT));
  POOLS.put("sonicraft:marble_cave_1_tunnel",Arrays.asList("marble_cave_1_tunnel_1","marble_cave_1_tunnel_2"));
  POOLS.put("sonicraft:marble_cave_1_hallway",Arrays.asList("marble_cave_1_hallway_1","marble_cave_1_hallway_2","marble_cave_1_hallway_3","marble_cave_1_hallway_4"));
  POOLS.put("sonicraft:marble_cave_1_hallway_end",Arrays.asList("marble_cave_1_hallway_end"));
 }
 static final class Piece {
  final StructurePort.Template template;
  final BlockPos origin;final Rotation rotation;final int depth;
  final Set<StructurePort.Connector> consumed=new HashSet<>();
  Piece(StructurePort.Template template,BlockPos origin,Rotation rotation,int depth){
   this.template=template;this.origin=origin;this.rotation=rotation;this.depth=depth;
  }
  int minX(){return origin.getX();}
  int maxX(){return origin.getX()+StructurePort.rotatedWidth(template,rotation)-1;}
  int minY(){return origin.getY();}
  int maxY(){return origin.getY()+template.height-1;}
  int minZ(){return origin.getZ();}
  int maxZ(){return origin.getZ()+StructurePort.rotatedDepth(template,rotation)-1;}
  boolean intersects(Piece b){return minX()<=b.maxX()&&maxX()>=b.minX()
   &&minY()<=b.maxY()&&maxY()>=b.minY()&&minZ()<=b.maxZ()&&maxZ()>=b.minZ();}
  BlockPos connectorPosition(StructurePort.Connector c){return StructurePort.at(template,origin,c,rotation);}
 }
 static final class Plan {
  final BlockPos entrance;final long seed;final List<Piece> pieces=new ArrayList<>();
  Plan(BlockPos entrance,long seed){this.entrance=entrance;this.seed=seed;}
  String summary(){
   StringBuilder result=new StringBuilder("seed="+seed+", segments="+pieces.size());
   for(Piece p:pieces)result.append("; ").append(p.template.name).append(" @ ").append(p.origin.getX()).append(",").append(p.origin.getY()).append(",").append(p.origin.getZ());
   return result.toString();
  }
 }
 private static final class Open {
  final Piece piece;final StructurePort.Connector port;
  Open(Piece piece,StructurePort.Connector port){this.piece=piece;this.port=port;}
 }
 static Plan plan(BlockPos entrance,long seed){
  Random random=new Random(seed);
  Plan result=new Plan(entrance,seed);
  StructurePort.Template cave=StructurePort.get(ROOT);
  Rotation caveRotation=Rotation.values()[random.nextInt(4)];
  Piece root=new Piece(cave,entrance.down(29),caveRotation,0);
  result.pieces.add(root);
  Deque<Open> pending=new ArrayDeque<>();
  // The 1.20.1 generator starts from the surface entrance (spawner template).
  // Its DOWN jigsaw must meet the cave's UP jigsaw one block BELOW the entrance.
  for(StructurePort.Connector c:cave.connectors){
   if(c.front==EnumFacing.UP){
    Piece top=attach(result,root,c,ENTRANCE,new Random(0),0);
    if(top!=null){root.consumed.add(c);for(StructurePort.Connector tc:top.template.connectors)top.consumed.add(tc);}
   }
  }
  for(StructurePort.Connector c:cave.connectors)if(!root.consumed.contains(c))pending.addLast(new Open(root,c));
  while(!pending.isEmpty()&&result.pieces.size()<MAX_PARTS){
   Open open=pending.removeFirst();
   if(open.piece.depth>=MAX_DEPTH||open.piece.consumed.contains(open.port))continue;
   List<String> pool=POOLS.get(open.port.pool);
   if(pool==null||pool.isEmpty())continue;
   List<String> shuffled=new ArrayList<>(pool);Collections.shuffle(shuffled,random);
   Piece next=null;
   for(String id:shuffled){
    next=attach(result,open.piece,open.port,id,random,open.piece.depth+1);
    if(next!=null)break;
   }
   if(next!=null){
    open.piece.consumed.add(open.port);
    for(StructurePort.Connector c:next.template.connectors)if(!next.consumed.contains(c))pending.addLast(new Open(next,c));
   }
  }
  return result;
 }
 private static Piece attach(Plan plan,Piece parent,StructurePort.Connector port,String candidate,Random random,int depth){
  StructurePort.Template child=StructurePort.get(candidate);
  if(child==null||!child.usable())return null;
  List<Rotation> rotations=new ArrayList<>(Arrays.asList(Rotation.values()));Collections.shuffle(rotations,random);
  EnumFacing out=parent.rotation.rotate(port.front);
  BlockPos target=parent.connectorPosition(port).offset(out);
  for(Rotation rot:rotations)for(StructurePort.Connector input:child.connectors){
   if(!input.name.equals(port.target)||rot.rotate(input.front)!=out.getOpposite())continue;
   BlockPos offset=StructurePort.at(child,BlockPos.ORIGIN,input,rot);
   Piece placed=new Piece(child,target.subtract(offset),rot,depth);
   if(placed.minY()<1||placed.maxY()>=255)continue;
   boolean collision=false;
   for(Piece used:plan.pieces)if(placed.intersects(used)){collision=true;break;}
   if(collision)continue;
   placed.consumed.add(input);
   plan.pieces.add(placed);
   return placed;
  }
  return null;
 }
 /** Atomic with respect to normal preflight failure: either ALL pieces pass or NONE are changed. */
 static boolean place(World world,Plan plan){
  if(world.isRemote)return false;
  for(Piece piece:plan.pieces){
   if(!piece.template.usable()||!StructurePort.canPlace(world,piece.origin,piece.template,piece.rotation,true))return false;
  }
  // Pieces have disjoint bounding boxes, so pasting one cannot invalidate another.
  for(Piece piece:plan.pieces){
   if(!StructurePort.place(world,piece.origin,piece.template,piece.rotation,true))
    throw new IllegalStateException("Marble Cave preflight violated while placing "+piece.template.name);
  }
  return true;
 }
}
