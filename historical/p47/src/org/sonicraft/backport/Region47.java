package org.sonicraft.backport;
import java.util.*;import java.util.function.LongPredicate;
/** Chunk-scale connected biome regions. No radius-based merging of separate islands. */
public final class Region47 {
 public static long key(int x,int z){return ((long)x<<32)^(z&0xffffffffL);}
 public static int connected(long start,Set<Long> occupied,LongPredicate sameBiome,int budget,Set<Long> component){
  ArrayDeque<Long> queue=new ArrayDeque<>();Set<Long> seen=new HashSet<>();queue.add(start);
  while(!queue.isEmpty()){
   long p=queue.removeFirst();if(!seen.add(p))continue;if(seen.size()>budget)return -1;
   if(!sameBiome.test(p))continue;component.add(p);if(occupied.contains(p))return 1;
   int x=(int)(p>>32),z=(int)p;queue.add(key(x+1,z));queue.add(key(x-1,z));queue.add(key(x,z+1));queue.add(key(x,z-1));
  }return 0;
 }
}
