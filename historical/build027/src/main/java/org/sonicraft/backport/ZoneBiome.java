package org.sonicraft.backport;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
public final class ZoneBiome extends Biome {
 final Content.BiomeDef definition;
 public ZoneBiome(Content.BiomeDef d){super(properties(d));definition=d;if(d.industrial){spawnableCreatureList.clear();spawnableMonsterList.clear();spawnableWaterCreatureList.clear();spawnableCaveCreatureList.clear();}topBlock=Content.BLOCKS.get(d.top).getDefaultState();fillerBlock=Content.BLOCKS.get(d.filler).getDefaultState();decorator.treesPerChunk=-999;decorator.grassPerChunk=0;decorator.flowersPerChunk=0;}
 private static BiomeProperties properties(Content.BiomeDef d){BiomeProperties p=new BiomeProperties(d.id.replace('_',' ')).setTemperature(d.temperature).setRainfall(d.rain).setBaseHeight(d.height).setHeightVariation(d.variation).setWaterColor(d.water);if(d.climate.equals("DESERT"))p.setRainDisabled();if(d.climate.equals("ICY"))p.setSnowEnabled();return p;}
 @Override public int getGrassColorAtPos(BlockPos p){return definition.grass;}
 @Override public int getFoliageColorAtPos(BlockPos p){return definition.foliage;}
 @Override public int getSkyColorByTemp(float temp){return definition.sky;}
 @Override public void decorate(World w,Random r,BlockPos chunk){ZoneOres.generate(w,r,chunk);String id=definition.id;
  if(id.equals("marble_zone")){ZoneStructures.decorate(w,r,chunk);StructurePort.decorate(w,r,chunk,id);return;}
  if(id.equals("green_hill_zone"))GreenHillLandmark.decorate(w,r,chunk);
  StructurePort.decorate(w,r,chunk,id);
  ZoneFlora.decorate(w,r,chunk,id);
  if(definition.industrial)return;
 if(id.equals("sandopolis_zone")||id.equals("mirage_saloon_zone")||id.equals("marble_zone")||id.equals("press_garden_zone"))return;int n=id.equals("angel_island_jungle")?5:2;for(int i=0;i<n;i++){BlockPos p=chunk.add(8+r.nextInt(16),0,8+r.nextInt(16));if(!w.isBlockLoaded(p))continue;p=w.getHeight(p);if(w.getBlockState(p.down()).getBlock()!=topBlock.getBlock())continue;ZoneTrees.generate(w,r,p,id.equals("mushroom_hill_zone")?"mushroom":id.equals("ice_cap_zone")||id.equals("hill_top_zone")?"pine":id.equals("seaside_hill_zone")?"beach":"green");}}
}
