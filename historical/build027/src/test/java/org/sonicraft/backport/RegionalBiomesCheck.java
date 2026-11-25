package org.sonicraft.backport;
import java.util.*;
import java.io.*;
import net.minecraft.block.Block;
import net.minecraft.init.Bootstrap;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
public final class RegionalBiomesCheck {
 static void require(boolean b,String message){if(!b)throw new AssertionError(message);}
 static int group(Biome b){return RegionalBiomes.group(b.getRegistryName().getResourcePath());}
 static long[] edges(Biome[] a,int n){long boundaries=0,sameGroup=0;for(int z=0;z<n;z++)for(int x=0;x<n;x++){int i=x+z*n;for(int j:new int[]{x+1<n?i+1:-1,z+1<n?i+n:-1})if(j>=0&&a[i]!=a[j]){boundaries++;if(group(a[i])==group(a[j]))sameGroup++;}}return new long[]{boundaries,sameGroup};}
 public static void main(String[] args)throws Exception {
  Bootstrap.register();for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d).setRegistryName("sonicraft",d.id);Content.BLOCKS.put(d.id,b);}
  for(Content.BiomeDef d:Content.BIOME_DEFS){ZoneBiome b=new ZoneBiome(d);b.setRegistryName("sonicraft",d.id);ForgeRegistries.BIOMES.register(b);Content.BIOMES.put(d.id,b);RegionalBiomes.group(d.id);}
  long seed=234;int n=2048;MobiusWorld.Provider regional=new MobiusWorld.Provider(seed,MobiusWorld.ZONES,true,7,2),old=new MobiusWorld.Provider(seed,MobiusWorld.ZONES,false,4,2);
  Biome[] a=regional.getBiomesForGeneration(null,-1024,-1024,n,n),b=old.getBiomesForGeneration(null,-1024,-1024,n,n);long[] ne=edges(a,n),oe=edges(b,n);
  Set<Biome> palette=new HashSet<>();for(String id:MobiusWorld.ZONES)palette.add(Content.BIOMES.get(id));Set<Biome> seen=new HashSet<>(Arrays.asList(a));require(palette.containsAll(seen)&&seen.size()>=8,"Valid varied regional sample");System.out.println("Map statistics: old="+oe[0]+", new="+ne[0]+", sameGroup="+ne[1]+", sampleBiomes="+seen.size());
  Set<Biome> wide=new HashSet<>();for(int z=-16384;z<=16384;z+=512)for(int x=-16384;x<=16384;x+=512)wide.add(regional.getBiome(new BlockPos(x,64,z)));require(palette.equals(wide),"All 16 zones across wider region map");require(ne[0]<oe[0]/4,"Larger coherent biomes");require(ne[1]*100/ne[0]>60,"Majority of boundaries inside thematic group");
  for(int x:new int[]{-513,-17,-1,0,17,2047}){int z=-37;Biome[] together=regional.getBiomes(null,x,z,32,16),left=regional.getBiomes(null,x,z,16,16),right=regional.getBiomes(null,x+16,z,16,16);for(int j=0;j<16;j++)for(int i=0;i<32;i++)require(together[i+j*32]==(i<16?left[i+j*16]:right[i-16+j*16]),"Chunk-stable tiling");require(regional.getBiome(new BlockPos(x,64,z))==together[0],"Biome cache matches map");}
  MobiusWorld.Provider copy=new MobiusWorld.Provider(seed,MobiusWorld.ZONES,true,7,2),different=new MobiusWorld.Provider(seed+1,MobiusWorld.ZONES,true,7,2);require(Arrays.equals(regional.getBiomes(null,-2000,-2000,256,256),copy.getBiomes(null,-2000,-2000,256,256)),"Repeatable seed");require(!Arrays.equals(regional.getBiomes(null,-2000,-2000,256,256),different.getBiomes(null,-2000,-2000,256,256)),"Seed changes map");
  MobiusWorld.Provider all=new MobiusWorld.Provider(seed,RegionalBiomes.all(),true,7,2);Set<Biome> allSeen=new HashSet<>();for(int z=-16384;z<=16384;z+=512)for(int x=-16384;x<=16384;x+=512)allSeen.add(all.getBiome(new BlockPos(x,64,z)));require(allSeen.size()==20,"All 20 zones in world-type palette");
  for(Biome target:seen){BlockPos found=regional.findBiomePosition(0,0,4096,Collections.singletonList(target),new Random(1));require(found!=null,"Biome search "+target.getRegistryName());}
  // Downsample generation maps at identical coordinates for a reviewable comparison.
  File out=new File("build/biome-comparison.csv");out.getParentFile().mkdirs();try(PrintWriter writer=new PrintWriter(out)){for(int z=0;z<n;z+=4)for(int x=0;x<n;x+=4){int i=x+z*n;writer.println(x/4+","+z/4+","+b[i].getRegistryName().getResourcePath()+","+a[i].getRegistryName().getResourcePath());}}
  System.out.println("PASS: 16/20 palettes, deterministic negative-coordinate tiling, cache and biome search; boundary count "+oe[0]+" -> "+ne[0]+"; "+(ne[1]*100/ne[0])+"% of new biome boundaries stay inside a thematic group");
 }
}
