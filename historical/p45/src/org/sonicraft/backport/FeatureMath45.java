package org.sonicraft.backport;
/** Convert source transforms around (0,0) to the port's positive-bounds template origin. */
public final class FeatureMath45 {
 public static int[] rotate(int x,int z,int r){switch(r&3){case 1:return new int[]{-z,x};case 2:return new int[]{-x,-z};case 3:return new int[]{z,-x};default:return new int[]{x,z};}}
 public static int[] origin(int width,int depth,int[] offset,int rotation,boolean mirror){int[] a=rotate(offset[0],mirror?-offset[2]:offset[2],rotation);int[] m=rotate(0,mirror?1-depth:0,rotation);int x=0,z=0;switch(rotation){case 1:x=1-depth;break;case 2:x=1-width;z=1-depth;break;case 3:z=1-width;break;}return new int[]{a[0]+m[0]+x,offset[1],a[1]+m[1]+z};}
}
