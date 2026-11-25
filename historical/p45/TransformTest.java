import org.sonicraft.backport.FeatureMath45;
public class TransformTest {
 public static void main(String[] args){int tests=0;for(int w:new int[]{1,3,5,9,13,15,35})for(int d:new int[]{1,3,5,9,13,15,35})for(int[] off:new int[][]{{0,0,0},{0,-7,0},{-7,-6,-7},{-2,-6,-2},{0,16,0}})for(int r=0;r<4;r++)for(boolean m:new boolean[]{false,true}){
 int[] o=FeatureMath45.origin(w,d,off,r,m);
 for(int x=0;x<w;x++)for(int z=0;z<d;z++){
  int xx=x,zz=m?d-1-z:z,bx=xx,bz=zz;
  switch(r){case 1:bx=d-1-zz;bz=xx;break;case 2:bx=w-1-xx;bz=d-1-zz;break;case 3:bx=zz;bz=w-1-xx;break;}
  int sx=x+off[0],sz=(m?-1:1)*(z+off[2]),tx=sx,tz=sz;
  for(int i=0;i<r;i++){int v=tx;tx=-tz;tz=v;}
  if(bx+o[0]!=tx||bz+o[2]!=tz||o[1]!=off[1])throw new AssertionError("Transform mismatch");tests++;
 }
 }System.out.println("PASS "+tests+" source/port transform comparisons, all rotations and mirrors");}
}
