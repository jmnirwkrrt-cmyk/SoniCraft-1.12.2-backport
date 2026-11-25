package org.sonicraft.backport;

import java.util.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** A bounded, operator-controlled adapter for the original Casino Night and Spring Yard
 * weighted jigsaw pools. Not a replacement for 1.20.1 structure placement or city worldgen.
 * All references are extracted from the original SoniCraft pool JSON files at build time.
 */
final class UrbanJigsaw {
 private UrbanJigsaw() {}
 static final int MAX_PIECES=10, MAX_DEPTH=3;
 static final class Entry {String id;int weight;}
 static final class Pool {String id;Entry[] entries;}
 static final class Data {Map<String,String> roots;Pool[] pools;}
 static final Data DATA=Content.read("city_pools_020.json",Data.class);
 static final Map<String,Pool> POOLS=new HashMap<>();
 static {for(Pool p:DATA.pools)POOLS.put(p.id,p);}
 private static final class Port {
  final MarbleDungeon.Piece piece; final StructurePort.Connector connector;
  Port(MarbleDungeon.Piece piece,StructurePort.Connector connector){this.piece=piece;this.connector=connector;}
 }
 static Set<String> cities(){return Collections.unmodifiableSet(DATA.roots.keySet());}
 static MarbleDungeon.Plan plan(String city,BlockPos pos,long seed){
  String rootName=DATA.roots.get(city);
  if(rootName==null)throw new IllegalArgumentException("Unknown city: "+city);
  StructurePort.Template root=StructurePort.get(rootName);
  if(root==null||!root.usable())throw new IllegalStateException("Unported city center "+rootName);
  Random random=new Random(seed);
  MarbleDungeon.Plan plan=new MarbleDungeon.Plan(pos,seed);
  MarbleDungeon.Piece anchor=new MarbleDungeon.Piece(root,pos,Rotation.values()[random.nextInt(4)],0);
  plan.pieces.add(anchor);
  Deque<Port> queue=new ArrayDeque<>();
  for(StructurePort.Connector c:root.connectors)queue.addLast(new Port(anchor,c));
  while(!queue.isEmpty()&&plan.pieces.size()<MAX_PIECES){
   Port next=queue.removeFirst();
   if(next.piece.depth>=MAX_DEPTH||next.piece.consumed.contains(next.connector))continue;
   Pool pool=POOLS.get(next.connector.pool);
   if(pool==null||pool.entries==null)continue;
   // Weighted sampling *without* replacement avoids repeatedly choosing the same blocked road.
   ArrayList<Entry> choices=new ArrayList<>(Arrays.asList(pool.entries));
   MarbleDungeon.Piece added=null;
   while(!choices.isEmpty()&&added==null){
    int sum=0;for(Entry e:choices)sum+=Math.max(1,e.weight);
    int pick=random.nextInt(sum),index=0;
    for(;index<choices.size()-1;index++){pick-=Math.max(1,choices.get(index).weight);if(pick<0)break;}
    Entry candidate=choices.remove(index);
    added=attach(plan,next.piece,next.connector,candidate.id,random);
   }
   if(added==null)continue;
   next.piece.consumed.add(next.connector);
   for(StructurePort.Connector c:added.template.connectors)
    if(!added.consumed.contains(c))queue.addLast(new Port(added,c));
  }
  return plan;
 }
 private static MarbleDungeon.Piece attach(MarbleDungeon.Plan plan,MarbleDungeon.Piece parent,
  StructurePort.Connector port,String name,Random rng){
  StructurePort.Template child=StructurePort.get(name);
  if(child==null||!child.usable()||child.blocks.size()>50000)return null;
  EnumFacing outward=parent.rotation.rotate(port.front);
  BlockPos at=parent.connectorPosition(port).offset(outward);
  ArrayList<Rotation> rotations=new ArrayList<>(Arrays.asList(Rotation.values()));
  Collections.shuffle(rotations,rng);
  for(Rotation rot:rotations)for(StructurePort.Connector candidate:child.connectors){
   if(!candidate.name.equals(port.target)||rot.rotate(candidate.front)!=outward.getOpposite())continue;
   BlockPos offset=StructurePort.at(child,BlockPos.ORIGIN,candidate,rot);
   MarbleDungeon.Piece piece=new MarbleDungeon.Piece(child,at.subtract(offset),rot,parent.depth+1);
   if(piece.minY()<1||piece.maxY()>=255)continue;
   boolean overlapping=false;
   for(MarbleDungeon.Piece placed:plan.pieces)if(piece.intersects(placed)){overlapping=true;break;}
   if(overlapping)continue;
   piece.consumed.add(candidate);
   plan.pieces.add(piece);
   return piece;
  }
  return null;
 }
 /** Never starts loading unloaded chunks; requires all sections to pass preflight. */
 static boolean place(World world,MarbleDungeon.Plan plan){return MarbleDungeon.place(world,plan);}
}
