package org.sonicraft.backport;
import net.minecraft.world.World;import net.minecraft.util.math.BlockPos;import net.minecraftforge.event.terraingen.PopulateChunkEvent.Post;import java.util.*;import java.io.*;
public class SiteTest{
 public static void main(String[]args){PrintStream display=System.out;System.setOut(new PrintStream(new OutputStream(){public void write(int n){}}));
 CityComplexWorldgen gen=new CityComplexWorldgen(); Post e=new Post();e.w=new World();int checks=0;
 for(long seed:new long[]{0,-7,23719713}){e.w.seed=seed;
 for(String alias:new String[]{"tailslab","casino",null}){Fixture.biome=alias;int labs=0;
 for(int x=-112;x<112;x++)for(int z=-112;z<112;z++){e.x=x;e.z=z;Fixture.d.pending.clear();gen.chunkPopulated(e);
 boolean expected=false;
 if("tailslab".equals(alias)){int rx=Math.floorDiv(x,56),rz=Math.floorDiv(z,56);Random r=new Random(seed+rx*341873128712L+rz*132897987541L+1820918320L);int sx=rx*56+r.nextInt(8),sz=rz*56+r.nextInt(8);expected=x==sx&&z==sz;}
 if("casino".equals(alias)){int rx=Math.floorDiv(x,3),rz=Math.floorDiv(z,3);Random r=new Random(CityComplexWorldgen.mix(seed,rx,rz));int sx=rx*3+r.nextInt(3),sz=rz*3+r.nextInt(3);expected=x==sx&&z==sz;}
 if(expected!=!Fixture.d.pending.isEmpty()||expected&&!Fixture.d.pending.containsValue(alias))throw new AssertionError(alias+" "+x+" "+z);
 if(expected)labs++;checks++;}
 if("tailslab".equals(alias)&&labs!=16)throw new AssertionError("lab region count");}}
 int[][] hs={{62,62,63,63,97,64,65,65,71},{65,64,64,64,32,64,65,65,65},{-1,-1,70,-1,-1,70,70,70,-1}};int[] ref={64,64,-1};
 for(int i=0;i<hs.length;i++){StructurePort.h=hs[i];if(CityComplexWorldgen.cityTerrain(e.w,new BlockPos(0,64,0),null,false)!=ref[i])throw new AssertionError("bad median");}
 if(10+P32Sites.roadShift("starlight")!=1||3+P32Sites.roadShift("studiopolis")!=1||P32Sites.roadShift("oilocean")!=0)throw new AssertionError("road");display.println("PASS: "+checks+" scheduler comparisons; 48 lab regions; median and road datum cases");}}
