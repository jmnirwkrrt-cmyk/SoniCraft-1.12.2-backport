package org.sonicraft.backport;

import java.util.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Bounded manual-only jigsaw adapter for authentic SoniCraft 1.20.1 complexes.
 * No automatic worldgen, boss event scripting or block-entity functionality is implied.
 * All references are extracted from the original SoniCraft pool JSON files at build time.
 */
final class SourceComplexes {
 private SourceComplexes() {}
 static final int MAX_PIECES=12, MAX_DEPTH=4, MAX_PIECE_BLOCKS=70000, MAX_TOTAL_BLOCKS=280000;
 static final class Entry {String id;int weight;}
 static final class Pool {String id;Entry[] entries;}
 static final class Data {Map<String,String> roots;Pool[] pools;}
 static final Data DATA=Content.read("complex_pools_022.json",Data.class);
 static final Map<String,Pool> POOLS=new HashMap<>();
 static {for(Pool p:DATA.pools)POOLS.put(p.id,p);}
 private static final class Port {
  final MarbleDungeon.Piece piece; final StructurePort.Connector connector;
  Port(MarbleDungeon.Piece piece,StructurePort.Connector connector){this.piece=piece;this.connector=connector;}
 }
 static Set<String> complexes(){return Collections.unmodifiableSet(DATA.roots.keySet());}
 /** Diagnostic only: a pool reference is not the same as an in-game ready structure. */
 static String audit(String alias){
  String root=DATA.roots.get(alias);
  if(root==null)throw new IllegalArgumentException("Unknown complex: "+alias);
  StructurePort.Template main=StructurePort.get(root);
  if(main==null)return alias+" : original root NBT missing ("+root+")";
  Set<String> seen=new HashSet<>();
  Deque<String> pending=new ArrayDeque<>();pending.add(root);
  int loadable=0,blocked=0,adapted=0,entities=0;
  TreeMap<String,Integer> unknown=new TreeMap<>();
  while(!pending.isEmpty()&&seen.size()<200){
   String name=pending.removeFirst();
   if(!seen.add(name))continue;
   StructurePort.Template t=StructurePort.get(name);
   if(t==null){unknown.merge("missing_template:"+name,1,Integer::sum);blocked++;continue;}
   if(t.usable())loadable++;else {blocked++;for(Map.Entry<String,Integer> e:t.missing.entrySet())unknown.merge(e.getKey(),e.getValue(),Integer::sum);}
   if(!t.deferred.isEmpty())adapted++;
   entities+=Math.max(0,t.entities-t.spawns.size());
   for(StructurePort.Connector c:t.connectors){
    Pool pool=POOLS.get(c.pool);
    if(pool!=null&&pool.entries!=null)for(Entry e:pool.entries)if(!seen.contains(e.id))pending.addLast(e.id);
   }
  }
  StringBuilder out=new StringBuilder(alias+" root="+root+", root="+(main.usable()?"ID-compatible":"BLOCKED")+", templates="+seen.size()+" (ID-compatible="+loadable+", blocked="+blocked+")");
  out.append(", unfinished behaviors=").append(adapted).append(", unported entities=").append(entities);
  if(!unknown.isEmpty()){
   out.append("; first missing IDs: ");int shown=0;
   for(Map.Entry<String,Integer> m:unknown.entrySet()){
    if(shown++>=6){out.append(" ...");break;}
    if(shown>1)out.append(", ");out.append(m.getKey()).append(" (").append(m.getValue()).append(")");
   }
  }
  return out.toString();
 }
 static String auditAll(){
  StringBuilder b=new StringBuilder("Original SoniCraft complex roots (ID coverage only): ");
  for(String name:new TreeSet<>(complexes())){
   StructurePort.Template t=StructurePort.get(DATA.roots.get(name));
   b.append(name).append('=').append(t!=null&&t.usable()?"ready-palette":"blocked").append(' ');
  }
  return b.toString();
 }
 static MarbleDungeon.Plan plan(String city,BlockPos pos,long seed){
  String rootName=DATA.roots.get(city);
  if(rootName==null)throw new IllegalArgumentException("Unknown complex: "+city);
  StructurePort.Template root=StructurePort.get(rootName);
  if(root==null||!root.usable())throw new IllegalStateException("Unported complex root "+rootName+"; run /sonistruct complex "+city+" audit");
  Random random=new Random(seed);
  if(root.blocks.size()>MAX_PIECE_BLOCKS)throw new IllegalStateException("Oversized source root "+rootName);
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
  if(child==null||!child.usable()||child.blocks.size()>MAX_PIECE_BLOCKS)return null;
  int usedBlocks=0;
  for(MarbleDungeon.Piece p:plan.pieces)usedBlocks+=p.template.blocks.size();
  if(usedBlocks+child.blocks.size()>MAX_TOTAL_BLOCKS)return null;
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
