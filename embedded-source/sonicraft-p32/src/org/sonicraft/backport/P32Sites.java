package org.sonicraft.backport;
import java.util.Arrays;
import java.util.Random;
// The source random_spread placement for tails_lab: linear, spacing 56, separation 48.
public final class P32Sites {
 public static boolean lab(long seed,int x,int z){
  int rx=Math.floorDiv(x,56),rz=Math.floorDiv(z,56);
  Random r=new Random(seed+341873128712L*rx+132897987541L*rz+1820918320L);
  int dx=r.nextInt(8),dz=r.nextInt(8);
  return Math.floorMod(x,56)==dx && Math.floorMod(z,56)==dz;
 }
 public static String select(String alias,boolean city,boolean lab){
  return ("tailslab".equals(alias)?lab:city)?alias:null;
 }
 public static int median(int[] h){
  int n=0;for(int v:h)if(v>=1&&v<=254)h[n++]=v;
  if(n<5)return -1;
  Arrays.sort(h,0,n);return h[n/2];
 }
 // Height relative to a normal road's connector at local y=1. Offshore decks retain their elevation.
 public static int roadShift(String alias){return "starlight".equals(alias)?-9:"studiopolis".equals(alias)?-2:0;}
}
