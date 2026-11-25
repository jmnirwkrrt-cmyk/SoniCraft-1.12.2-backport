package org.sonicraft.backport;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.nbt.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Loads unmodified SoniCraft 1.20.1 structure NBT without converting it to a 1.12.2 palette beforehand.
 * Unavailable/unsupported blocks FAIL CLOSED: no partial structures are ever pasted.
 * The MarbleDungeon helper assembles some jigsaw pools; original entities and many mechanics remain deferred. */
final class StructurePort {
 static class Cell { final int x,y,z; final IBlockState block; final NBTTagCompound tileData;
  Cell(int x,int y,int z,IBlockState b,NBTTagCompound tileData){this.x=x;this.y=y;this.z=z;this.block=b;this.tileData=tileData;}
 }
 static class Spawn {
  final String id;final double x,y,z;final float yaw;
  Spawn(String id,double x,double y,double z,float yaw){this.id=id;this.x=x;this.y=y;this.z=z;this.yaw=yaw;}
 }
 static final Set<String> SAFE_ENTITIES=Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
  "minecraft:pig", // One vanilla animal deliberately included in source city NBT.
  "sonicraft:motobug","sonicraft:burrobot","sonicraft:rhinobot",
  "sonicraft:crabmeat","sonicraft:caterkiller","sonicraft:spiny","sonicraft:splats",
  "sonicraft:ball_hog","sonicraft:roller","sonicraft:buzz_bomber","sonicraft:batbrain",
  "sonicraft:jaws_badnik","sonicraft:woodrow","sonicraft:grabber",
  "sonicraft:grounder","sonicraft:coconuts","sonicraft:bumpalo",
  "sonicraft:aquis","sonicraft:buzzer","sonicraft:bomb_badnik","sonicraft:crawl_badnik","sonicraft:egg_flapper","sonicraft:flasher","sonicraft:octus","sonicraft:spikes_badnik","sonicraft:rattlekiller","sonicraft:rexon","sonicraft:shutterbug","sonicraft:iwamodoki",
  "sonicraft:newtron","sonicraft:tubinaut","sonicraft:gohla","sonicraft:egg_pawn_bunny",
  "sonicraft:flicky","sonicraft:pocky")));
 static class Connector {
  final int x,y,z; final EnumFacing front; final String pool,name,target;
  Connector(int x,int y,int z,EnumFacing front,String pool,String name,String target){
   this.x=x;this.y=y;this.z=z;this.front=front;this.pool=pool;this.name=name;this.target=target;
  }
 }
 static class Template {
  final List<Connector> connectors=new ArrayList<>();final List<Spawn> spawns=new ArrayList<>();
  final String name; final int width,height,depth; final List<Cell> blocks=new ArrayList<>();
  final Map<String,Integer> missing=new TreeMap<>();
  final Map<String,Integer> deferred=new TreeMap<>(); int blockEntities,entities,markers;
  Template(String name,int w,int h,int d){this.name=name;width=w;height=h;depth=d;}
  boolean usable(){return missing.isEmpty();}
  String report(){return name+" "+width+"x"+height+"x"+depth+", blocks="+blocks.size()+", unmapped="+missing+", markers="+markers+", NBT block entities="+blockEntities+", entities="+entities+", ported entities="+spawns.size()+", behavior/state caveats="+deferred;}
 }
 static final ResourceLocation MOBIUS_LOOT=new ResourceLocation("sonicraft","mobius_loot");
 static final Set<String> DEFERRED_MECHANISMS=Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
  "sonicraft:air_bubble_spawner","sonicraft:bobsled_launch_panel",
  "sonicraft:invisible_vision_block","sonicraft:invisible_vision_gold_ring",
  "sonicraft:mega_mack","sonicraft:rexon_body","sonicraft:spring_yard_searchlight",
  "sonicraft:carnival_night_barrel","sonicraft:studiopolis_lamppost_rod",
  "sonicraft:night_carnival_switch_platform","sonicraft:night_carnival_neon_bridge_projector",
  "sonicraft:night_carnival_neon_bridge","sonicraft:night_carnival_curtain_light",
  "sonicraft:press_garden_conveyor_belt_axle","sonicraft:egg_crusher_bulb",
  "sonicraft:egg_crusher_spawn_trigger","sonicraft:seesaw_hill_top_fulcrum",
  // 0.1.24 imports graphical assets, but not scripted level events or machine TE logic.
  "sonicraft:special_stage_goal_block","sonicraft:special_stage_gate",
  "sonicraft:color_jewel","sonicraft:blue_color_block",
  "sonicraft:pink_special_stage_border_block",
  "sonicraft:press_garden_redstone_bulb","sonicraft:press_garden_redstone_bulb_detector",
  "sonicraft:item_box_thunder_shield","sonicraft:ice_cube_ice_spikes",
  "sonicraft:launch_base_redstone_block_off","sonicraft:launch_base_redstone_block",
  "sonicraft:night_carnival_switch_wireless_connector","sonicraft:weather_globe_sensor",
  "sonicraft:launch_base_rocket_thruster","sonicraft:magnet_piece","sonicraft:magnet_tip",
  "sonicraft:death_egg_hologram","sonicraft:studiopolis_interior_clapperboard_spring",
  "sonicraft:eggmobile_upgrading_table","sonicraft:flashing_oil_ocean_orb",
  "sonicraft:spinning_oil_ocean_orb","sonicraft:smoke_emitter",
  // Geometry only in 0.1.25: puzzle, item-box, camera and boss logic is deferred.
  "sonicraft:casino_night_slot_lever","sonicraft:casino_night_slot_wheel_rings",
  "sonicraft:casino_night_slot_wheel_shield","sonicraft:casino_night_slot_wheel_shoes",
  "sonicraft:cpz_factory_claw_arm","sonicraft:death_egg_eye",
  "sonicraft:death_egg_magnet_button","sonicraft:giant_mecha_sonic_eye_block",
  "sonicraft:ice_cube_item_box_flame_shield","sonicraft:ice_cube_item_box_rings",
  "sonicraft:item_box_rush_random_rings","sonicraft:item_box_rush_shield",
  "sonicraft:launch_base_redstone_lock","sonicraft:launch_base_redstone_lock_wall_adapter_on",
  "sonicraft:night_carnival_dash_rail","sonicraft:oil_ocean_cannon",
  "sonicraft:press_garden_wood_chipper","sonicraft:press_garden_wood_chipper_saws",
  "sonicraft:seesaw_hill_top_ball","sonicraft:seesaw_hill_top_high_end",
  "sonicraft:seesaw_hill_top_low_end","sonicraft:segasonic_popcorn_shop_arcade_machine",
  "sonicraft:starpost_sonic_2","sonicraft:studiopolis_camera",
  "sonicraft:studiopolis_popcorn_shop_cannon","sonicraft:locked_studiopolis_door",
  "sonicraft:press_garden_factory_door_locked","sonicraft:death_egg_arena_door")));
 // These blocks have 1.12.2 adaptations, not exact original behavior.
 static final Set<String> ADAPTED_MECHANISMS=Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
   "sonicraft:press_garden_conveyor_belt","sonicraft:cpz_factory_conveyor_belt",
   "sonicraft:ice_cap_trampoline","sonicraft:bumper_sonic_3","sonicraft:studiopolis_spring_catapult",
   "sonicraft:dash_ring","sonicraft:sandopolis_quicksand","sonicraft:launch_base_blockade_trapdoor",
   "sonicraft:item_box_rings", "sonicraft:item_box_shield", "sonicraft:item_box_invincibility",
   "sonicraft:yellow_spring", "sonicraft:spikes", "sonicraft:switch_detector", "sonicraft:switch_marble",
   "sonicraft:marble_zone_stone_flamethrower_on", "sonicraft:player_proximity_sensor_on", "sonicraft:marble_crusher_cylinder", "sonicraft:studiopolis_clapperboard_spring",
  // 0.1.24 bounded 1.12.2 physics; original special-stage win/lose loops are NOT present.
  "sonicraft:blue_sphere","sonicraft:red_sphere","sonicraft:yellow_sphere",
  "sonicraft:bumper_sphere","sonicraft:special_stage_up_block",
  "sonicraft:special_stage_down_block","sonicraft:special_stage_reverse_block",
  "sonicraft:bouncy_checkpoint","sonicraft:bouncy_checkpoint_top",
  "sonicraft:industrial_electric_block","sonicraft:death_egg_electric_block")));
 static void registerLoot(){LootTableList.register(MOBIUS_LOOT);}
 static final Set<String> CATALOG=loadCatalog();
 // Bounded lazy cache: especially large structures must not remain permanently loaded.
 private static final Map<String,Template> CACHE=new LinkedHashMap<String,Template>(16,.75f,true){
  @Override protected boolean removeEldestEntry(Map.Entry<String,Template> entry){return size()>5;}
 };
 private static final java.util.Set<String> BOSS_TRIGGER_IDS=java.util.Collections.unmodifiableSet(new java.util.HashSet<String>(java.util.Arrays.asList(
  "big_arms_spawn_trigger","egg_crusher_spawn_trigger","egg_spiker_spawn_trigger","eggman_despawn_trigger",
  "shiversaw_spawn_trigger","silver_sonic_spawn_trigger","weather_globe_spawn_trigger")));
 static Set<String> loadCatalog(){Set<String> result=new TreeSet<>();try(InputStream in=StructurePort.class.getResourceAsStream("/assets/sonicraft/backport/structures_catalog.txt")){
  if(in==null)throw new IllegalStateException("Missing structures catalog");BufferedReader r=new BufferedReader(new InputStreamReader(in,StandardCharsets.UTF_8));String line;while((line=r.readLine())!=null)if(!line.trim().isEmpty())result.add(line.trim());
 }catch(IOException ex){throw new IllegalStateException(ex);}return Collections.unmodifiableSet(result);}
 /** Native 1.12.2 block equivalents for 1.13+ vanilla IDs in the source NBT.
  * Every fallback is surfaced in Template.deferred: these are not byte-for-byte
  * visual/behavioral ports of their newer vanilla counterparts.
  */
 static String legacyVanillaBlock(String source){
  switch(source){
   case "minecraft:azalea_leaves": return "minecraft:leaves";
   case "minecraft:bamboo_wall_sign": case "minecraft:birch_wall_sign": return "minecraft:wall_sign";
   case "minecraft:barrel": return "minecraft:chest";
   case "minecraft:cobweb": return "minecraft:web";
   case "minecraft:big_dripleaf": return "minecraft:leaves";
   case "minecraft:big_dripleaf_stem": return "minecraft:fence";
   case "minecraft:brown_bed": case "minecraft:white_bed": return "minecraft:bed";
   case "minecraft:campfire": return "minecraft:magma";
   case "minecraft:honey_block": return "minecraft:slime";
   case "minecraft:lantern": return "minecraft:end_rod";
   case "minecraft:sculk_vein": return "minecraft:vine";
   case "minecraft:smooth_basalt": return "minecraft:stone";
   case "minecraft:soul_wall_torch": return "minecraft:torch";
   case "minecraft:spruce_log": return "minecraft:log";
   case "minecraft:stone_brick_wall": return "minecraft:cobblestone_wall";
   default: return source;
  }
 }

 static synchronized Template get(String name){
  if(!CATALOG.contains(name))return null;
  Template t=CACHE.get(name);if(t!=null)return t;
  try(InputStream in=StructurePort.class.getResourceAsStream("/assets/sonicraft/structures_1201/"+name+".nbt")){
   if(in==null)throw new IllegalStateException("Missing original NBT: "+name);
   NBTTagCompound root=CompressedStreamTools.readCompressed(in);
   NBTTagList size=root.getTagList("size",3);
   t=new Template(name,size.getIntAt(0),size.getIntAt(1),size.getIntAt(2));
   NBTTagList palette=root.getTagList("palette",10),items=root.getTagList("blocks",10);
   IBlockState[] states=new IBlockState[palette.tagCount()];String[] unavailable=new String[states.length];boolean[] marker=new boolean[states.length];
   for(int i=0;i<states.length;i++){
    NBTTagCompound entry=palette.getCompoundTagAt(i);
    String id=entry.getString("Name");
    if(id.equals("minecraft:structure_block")||id.equals("minecraft:jigsaw")||id.equals("minecraft:structure_void")||id.equals("sonicraft:solid_structure_void")){
     marker[i]=true;continue;
    }
    if(name.equals("marble_ruins_1")&&id.equals("minecraft:bedrock"))id="sonicraft:marble_zone_stone";
    // Small Sandopolis stalagmites use bedrock as a one-block template footing.
    if(name.startsWith("pointed_southite_")&&id.equals("minecraft:bedrock"))id="sonicraft:sandopolis_sandstone";
    // Source boss event scripts are not available yet. Keep their original NBT and
    // registered palette IDs, but deliberately do not place inert unbreakable markers
    // into player worlds. The diagnostic explicitly records every deferred trigger.
    if(id.startsWith("sonicraft:") && BOSS_TRIGGER_IDS.contains(id.substring(10))){
     t.deferred.merge("DISABLED_BOSS_TRIGGER:"+id.substring(10),1,Integer::sum);
     id="minecraft:air";
    }
    if(id.equals("minecraft:wall_torch"))id="minecraft:torch";
    // 1.20 Mangrove wood button used as ordinary redstone switch in Scrap Brain factories.
    if(id.equals("minecraft:mangrove_button"))id="minecraft:wooden_button";
    // Several 1.20-only vanilla IDs changed or had no registry equivalent in 1.12.2.
    if(id.equals("minecraft:chain"))id="sonicraft:cave_chain";
    if(id.equals("minecraft:gray_concrete")||id.equals("minecraft:white_concrete")||id.equals("minecraft:black_concrete")||id.equals("minecraft:lime_concrete"))id="minecraft:concrete";
    if(id.equals("minecraft:cave_air"))id="minecraft:air";
    if(id.equals("minecraft:smooth_stone"))id="minecraft:stone";
    if(id.equals("minecraft:tinted_glass"))id="minecraft:stained_glass";
    if(id.equals("minecraft:smooth_sandstone"))id="minecraft:sandstone";
    if(id.equals("minecraft:poppy"))id="minecraft:red_flower";
    if(id.equals("minecraft:grass"))id="minecraft:tallgrass";
    if(id.equals("minecraft:redstone_wall_torch"))
     id=entry.getCompoundTag("Properties").getString("lit").equals("false")?"minecraft:unlit_redstone_torch":"minecraft:redstone_torch";
    // 1.20-only vanilla decoration would otherwise make ten otherwise-covered
    // building templates fail closed in 1.12. Keep all substitutions auditable.
    String beforeLegacy=id;
    id=legacyVanillaBlock(id);
    if(!id.equals(beforeLegacy))t.deferred.merge("vanilla_1_20_fallback:"+beforeLegacy+"->"+id,1,Integer::sum);
    // Pre-flattening 1.12 uses distinct block registry IDs for powered repeaters.
    NBTTagCompound rawProps=entry.getCompoundTag("Properties");
    if(id.equals("minecraft:repeater"))id=rawProps.getString("powered").equals("true")?
     "minecraft:powered_repeater":"minecraft:unpowered_repeater";
    ResourceLocation rl=new ResourceLocation(id);
    Block block=id.startsWith("sonicraft:")?Content.BLOCKS.get(rl.getResourcePath()):ForgeRegistries.BLOCKS.containsKey(rl)?ForgeRegistries.BLOCKS.getValue(rl):null;
    if(block==null){unavailable[i]=id;continue;}
    NBTTagCompound props=entry.getCompoundTag("Properties");
    if(block instanceof ShapeBlocks.Slab && props.getString("type").equals("double")){
     Block full=ShapeBlocks.DOUBLES.get(rl.getResourcePath());if(full!=null)block=full;
    }
    IBlockState state=block.getDefaultState();
    if(entry.getString("Name").equals("minecraft:gray_concrete"))state=Blocks.CONCRETE.getStateFromMeta(7);
    if(entry.getString("Name").equals("minecraft:white_concrete"))state=Blocks.CONCRETE.getStateFromMeta(0);
    if(entry.getString("Name").equals("minecraft:black_concrete"))state=Blocks.CONCRETE.getStateFromMeta(15);
    if(entry.getString("Name").equals("minecraft:lime_concrete"))state=Blocks.CONCRETE.getStateFromMeta(5);
    if(id.equals("minecraft:tallgrass"))state=Blocks.TALLGRASS.getStateFromMeta(1);
    // 1.12 leaves distinguish naturally-generated (decaying) from player-placed.
    // Modern azalea/big-dripleaf foliage must not silently disappear on chunk ticks.
    if(beforeLegacy.equals("minecraft:azalea_leaves")||beforeLegacy.equals("minecraft:big_dripleaf"))
     state=Blocks.LEAVES.getStateFromMeta(4);
    if(beforeLegacy.equals("minecraft:spruce_log"))state=Blocks.LOG.getStateFromMeta(1);
    if(beforeLegacy.equals("minecraft:smooth_basalt"))state=Blocks.STONE.getStateFromMeta(6);
    if(beforeLegacy.equals("minecraft:lantern")){
     IProperty<?> face=state.getBlock().getBlockState().getProperty("facing");
     IBlockState adjusted=face==null?null:apply(state,face,props.getString("hanging").equals("true")?"down":"up");
     if(adjusted!=null)state=adjusted;
    }
    for(String propName:props.getKeySet()){
     if(propName.equals("waterlogged")||propName.equals("type")&&block instanceof ShapeBlocks.Slab)continue;
     if(entry.getString("Name").equals("sonicraft:egg_crusher_bulb")&&propName.equals("blockstate")){
      if(!props.getString(propName).equals("0"))t.deferred.merge("egg_crusher_bulb:source_animation_frame",1,Integer::sum);
      continue;
     }
     // Modern walls use none/low/tall, while 1.12 walls use boolean arms.
     // Preserve the connection instead of marking the whole template unusable.
     String propValue=props.getString(propName);
     if(beforeLegacy.equals("minecraft:stone_brick_wall") &&
        (propName.equals("north")||propName.equals("east")||propName.equals("south")||propName.equals("west")))
      propValue=propValue.equals("none")?"false":"true";
     // Old logs and end rods preserve their primitive axis/facing properties.
     if(beforeLegacy.equals("minecraft:lantern")&&propName.equals("hanging"))continue;
     IProperty<?> prop=state.getBlock().getBlockState().getProperty(propName);
     if(prop==null){
     // Source block state properties can vanish silently when a 1.20 model was imported
     // as a plain 1.12 block. Record this explicitly instead of reporting full fidelity.
     t.deferred.merge("unmapped_state:"+entry.getString("Name")+"["+propName+"="+props.getString(propName)+"]",1,Integer::sum);
     continue;
    }
     IBlockState next=apply(state,prop,propValue);
     if(next==null){
      // Never claim source state parity for an unsupported modern vanilla value.
      // Fall back to the safe block default and keep a clear diagnostic.
      if(!beforeLegacy.equals(id)){
       t.deferred.merge("unmapped_vanilla_value:"+beforeLegacy+"["+propName+"="+props.getString(propName)+"]",1,Integer::sum);
       continue;
      }
      unavailable[i]=id+"["+propName+"="+props.getString(propName)+"]";break;
     }
     state=next;
    }
    states[i]=state;
   }
   for(int i=0;i<items.tagCount();i++){
    NBTTagCompound item=items.getCompoundTagAt(i);int state=item.getInteger("state");if(state<0||state>=states.length)throw new IllegalStateException(name+" invalid palette index "+state);
    if(marker[state]){
     t.markers++;
     if(palette.getCompoundTagAt(state).getString("Name").equals("minecraft:jigsaw") && item.hasKey("nbt",10)){
      NBTTagList markerPos=item.getTagList("pos",3);
      NBTTagCompound nbt=item.getCompoundTag("nbt");
      String orientation=palette.getCompoundTagAt(state).getCompoundTag("Properties").getString("orientation");
      String face=orientation.contains("_")?orientation.substring(0,orientation.indexOf('_')):orientation;
      EnumFacing direction=EnumFacing.byName(face);
      if(direction!=null)t.connectors.add(new Connector(markerPos.getIntAt(0),markerPos.getIntAt(1),markerPos.getIntAt(2),direction,nbt.getString("pool"),nbt.getString("name"),nbt.getString("target")));
      // 1.20 JigsawManager replaces ALL connectors with final_state, not just air.
      // City roads can require solid stone, GHZ bridge or CPZ iron here; silently
      // discarding those markers produces holes in otherwise continuous roads.
      String finalState=nbt.getString("final_state");
      if(finalState.equals("minecraft:air"))
       t.blocks.add(new Cell(markerPos.getIntAt(0),markerPos.getIntAt(1),markerPos.getIntAt(2),Blocks.AIR.getDefaultState(),null));
      // Original solid structure void means "leave terrain as-is", not a registered
      // material block. Most importantly, it must not abort a Press Garden factory.
      else if(finalState.equals("sonicraft:solid_structure_void")){
       t.deferred.merge("jigsaw_keep_terrain",1,Integer::sum);
      }
      else if(!finalState.isEmpty()){
       // Do not guess property-bearing states. Fail closed until a safe adapter exists.
       if(finalState.indexOf('[')>=0){t.missing.merge("jigsaw_final_state:"+finalState,1,Integer::sum);}
       else{
        ResourceLocation finalId=new ResourceLocation(finalState);
        Block replacement=finalState.startsWith("sonicraft:")?Content.BLOCKS.get(finalId.getResourcePath()):ForgeRegistries.BLOCKS.getValue(finalId);
        if(replacement==null)t.missing.merge("jigsaw_final_state:"+finalState,1,Integer::sum);
        else t.blocks.add(new Cell(markerPos.getIntAt(0),markerPos.getIntAt(1),markerPos.getIntAt(2),replacement.getDefaultState(),null));
       }
      }
     }
     continue;
    }
    if(unavailable[state]!=null){t.missing.merge(unavailable[state],1,Integer::sum);continue;}
    NBTTagList pos=item.getTagList("pos",3);
    NBTTagCompound tile=item.hasKey("nbt",10)?item.getCompoundTag("nbt"):null;
    t.blocks.add(new Cell(pos.getIntAt(0),pos.getIntAt(1),pos.getIntAt(2),states[state],tile));
    if(tile!=null)t.blockEntities++;
    String originalId=palette.getCompoundTagAt(state).getString("Name");
    if(DEFERRED_MECHANISMS.contains(originalId))t.deferred.merge(originalId,1,Integer::sum);
    if(ADAPTED_MECHANISMS.contains(originalId))t.deferred.merge("adapted:"+originalId,1,Integer::sum);
   }
   NBTTagList mobs=root.getTagList("entities",10);
   t.entities=mobs.tagCount();
   for(int i=0;i<mobs.tagCount();i++){
    NBTTagCompound mob=mobs.getCompoundTagAt(i),tag=mob.getCompoundTag("nbt");
    String id=tag.getString("id");
    if(!SAFE_ENTITIES.contains(id))continue;
    NBTTagList p=mob.getTagList("pos",6);
    if(p.tagCount()<3)continue;
    float yaw=0;
    NBTTagList facing=tag.getTagList("Rotation",5);
    if(facing.tagCount()>0)yaw=facing.getFloatAt(0);
    t.spawns.add(new Spawn(id,p.getDoubleAt(0),p.getDoubleAt(1),p.getDoubleAt(2),yaw));
   }
   CACHE.put(name,t);return t;
  }catch(IOException ex){throw new IllegalStateException("Cannot read template "+name,ex);}
 }
 @SuppressWarnings({"rawtypes","unchecked"})
 private static IBlockState apply(IBlockState state,IProperty property,String value){
  for(Object raw:property.getAllowedValues()){
   Comparable allowed=(Comparable)raw;
   if(property.getName(allowed).equals(value))return state.withProperty(property,allowed);
  }
  return null;
 }
 static int rotatedWidth(Template t,Rotation rot){return rot==Rotation.CLOCKWISE_90||rot==Rotation.COUNTERCLOCKWISE_90?t.depth:t.width;}
 static int rotatedDepth(Template t,Rotation rot){return rot==Rotation.CLOCKWISE_90||rot==Rotation.COUNTERCLOCKWISE_90?t.width:t.depth;}
 static BlockPos at(Template t,BlockPos origin,Cell c,Rotation rot){
  switch(rot){case CLOCKWISE_90:return origin.add(t.depth-1-c.z,c.y,c.x);
   case CLOCKWISE_180:return origin.add(t.width-1-c.x,c.y,t.depth-1-c.z);
   case COUNTERCLOCKWISE_90:return origin.add(c.z,c.y,t.width-1-c.x);
   default:return origin.add(c.x,c.y,c.z);}
 }
 static BlockPos at(Template t,BlockPos origin,Connector c,Rotation rot){
  switch(rot){case CLOCKWISE_90:return origin.add(t.depth-1-c.z,c.y,c.x);
   case CLOCKWISE_180:return origin.add(t.width-1-c.x,c.y,t.depth-1-c.z);
   case COUNTERCLOCKWISE_90:return origin.add(c.z,c.y,t.width-1-c.x);
   default:return origin.add(c.x,c.y,c.z);}
 }
 /** Side effect free: every affected position checked BEFORE mutating any block. */
 static boolean canPlace(World world,BlockPos origin,Template template,Rotation rot,boolean allowTerrain){
  if(template==null||!template.usable()||world.isRemote)return false;
  int w=rotatedWidth(template,rot),d=rotatedDepth(template,rot);
  if(origin.getY()<1||origin.getY()+template.height>=255||!world.isAreaLoaded(origin,origin.add(w-1,template.height-1,d-1)))return false;
  if(template.blocks.size()>50000)return false; // avoid a large synchronous server stall
  for(Cell cell:template.blocks){BlockPos p=at(template,origin,cell,rot);IBlockState old=world.getBlockState(p);
   if(old.getBlock()==Blocks.BEDROCK||world.getTileEntity(p)!=null)return false;
   if(allowTerrain){
    // Ground and rock can be cut by the buried part of a structure; never consume built structures or fluids.
    Biome here=world.getBiome(p);Block terrainTop=here instanceof ZoneBiome?Content.BLOCKS.get(((ZoneBiome)here).definition.top):null;
    Block terrainFiller=here instanceof ZoneBiome?Content.BLOCKS.get(((ZoneBiome)here).definition.filler):null;
    if(!world.isAirBlock(p)&&old.getMaterial().isLiquid())return false;
    if(!world.isAirBlock(p)&&old.getMaterial().isSolid()&&old.getBlock()!=terrainTop&&old.getBlock()!=terrainFiller&&old.getBlock()!=Content.BLOCKS.get("marble_zone_grass")&&old.getBlock()!=Content.BLOCKS.get("marble_zone_stone")&&old.getBlock()!=Content.BLOCKS.get("chaostone")&&old.getBlock()!=Content.BLOCKS.get("marble_zone_cave_wall")&&old.getBlock()!=Blocks.STONE&&old.getBlock()!=Blocks.DIRT&&old.getBlock()!=Blocks.GRASS&&old.getBlock()!=Blocks.GRAVEL&&old.getBlock()!=Blocks.SAND&&old.getBlock()!=Blocks.COAL_ORE&&old.getBlock()!=Blocks.IRON_ORE&&old.getBlock()!=Blocks.GOLD_ORE&&old.getBlock()!=Blocks.REDSTONE_ORE&&old.getBlock()!=Blocks.LIT_REDSTONE_ORE&&old.getBlock()!=Blocks.LAPIS_ORE&&old.getBlock()!=Blocks.DIAMOND_ORE&&old.getBlock()!=Blocks.EMERALD_ORE)return false;
   }
  }
  return true;
 }
 /** Validated placement, including 1.12-compatible inventory data. */
 static boolean place(World world,BlockPos origin,Template template,Rotation rot,boolean allowTerrain){
  if(!canPlace(world,origin,template,rot,allowTerrain))return false;
  for(Cell cell:template.blocks){BlockPos p=at(template,origin,cell,rot);IBlockState rotated=cell.block.withRotation(rot);world.setBlockState(p,rotated,2);ExtendedStructureBlocks.remember(world,p,rotated);}
  // Restore only safe 1.12-compatible loot and inventory fields. Never deserialize a foreign
  // 1.20 tile NBT wholesale: IDs/capabilities and coordinates have incompatible schemas.
  for(Cell cell:template.blocks)if(cell.tileData!=null){
   BlockPos p=at(template,origin,cell,rot);TileEntity tile=world.getTileEntity(p);
   if(tile==null)continue;
   // In 1.20, signs have front_text/back_text compounds; in 1.12 there is
   // a single four-line TileEntitySign.signText. Preserve the readable front
   // text (e.g. Eggman's warnings and Casino Night exit signs), not the
   // incompatible modern TileEntity NBT/JSON formatting or click events.
   if(tile instanceof TileEntitySign&&cell.tileData.hasKey("front_text",10)){
    NBTTagList lines=cell.tileData.getCompoundTag("front_text").getTagList("messages",8);
    TileEntitySign sign=(TileEntitySign)tile;
    for(int line=0;line<Math.min(4,lines.tagCount());line++){
     String raw=lines.getStringTagAt(line),plain="";
     try{ITextComponent parsed=ITextComponent.Serializer.jsonToComponent(raw);
      if(parsed!=null)plain=parsed.getUnformattedText();}
     catch(RuntimeException ignored){plain="";}
     // The vanilla 1.12 edit UI and sign renderer expect short lines.
     if(plain.length()>15)plain=plain.substring(0,15);
     sign.signText[line]=new TextComponentString(plain);
    }
    sign.markDirty();world.notifyBlockUpdate(p,world.getBlockState(p),world.getBlockState(p),3);
   }
   if(tile instanceof TileEntityLockableLoot&&cell.tileData.hasKey("LootTable",8)){
    String loot=cell.tileData.getString("LootTable");
    if(loot.equals("sonicraft:mobius_loot"))loot=MOBIUS_LOOT.toString();
    if(loot.startsWith("minecraft:chests/")||loot.equals(MOBIUS_LOOT.toString()))
     ((TileEntityLockableLoot)tile).setLootTable(new ResourceLocation(loot),world.rand.nextLong());
   }else if(tile instanceof IInventory&&cell.tileData.hasKey("Items",9)){
    IInventory inventory=(IInventory)tile;
    NBTTagList saved=cell.tileData.getTagList("Items",10);
    for(int i=0;i<saved.tagCount();i++){
     NBTTagCompound stackTag=saved.getCompoundTagAt(i);int slot=stackTag.getByte("Slot")&255;
     if(slot>=0&&slot<inventory.getSizeInventory()&&stackTag.hasKey("id",8)){
      ItemStack stack=new ItemStack(stackTag);
      if(!stack.isEmpty())inventory.setInventorySlotContents(slot,stack);
     }
    }
    tile.markDirty();
   }
  }
  // Only explicitly ported badniks and freed animals may be spawned from foreign
  // 1.20 NBT. Never deserialize cross-version brain/attributes/equipment.
  if(world instanceof net.minecraft.world.WorldServer){
   for(Spawn mob:template.spawns){
    double x=mob.x,z=mob.z;
    switch(rot){case CLOCKWISE_90:x=template.depth-z;z=mob.x;break;
     case CLOCKWISE_180:x=template.width-x;z=template.depth-z;break;
     case COUNTERCLOCKWISE_90:x=mob.z;z=template.width-mob.x;break;
     default:break;}
    Entity entity=EntityList.createEntityByIDFromName(new ResourceLocation(mob.id),world);
    if(entity!=null){entity.setLocationAndAngles(origin.getX()+x,origin.getY()+mob.y,origin.getZ()+z,mob.yaw+90*rot.ordinal(),0);world.spawnEntity(entity);}
   }
  }
  // Collision/segment state must be refreshed after the whole template has landed.
  for(Cell cell:template.blocks){BlockPos p=at(template,origin,cell,rot);if(world.getBlockState(p).getBlock() instanceof MarbleColumn)MarbleColumn.refresh(world,p);else if(world.getBlockState(p).getBlock() instanceof MarbleCaveBlocks.Detector)MarbleCaveBlocks.Detector.refresh(world,p);}
  return true;
 }
 /** Chunk-contained variants only: large jigsaw structures must use a dedicated multichunk generator. */
 static boolean decorateSmall(World world,Random random,BlockPos chunk,String name,String biome,int yOffset){
  Template t=get(name);if(t==null||!t.usable())return false;
  // Check the ROTATED bounds, not the source bounds: rectangular templates can
  // otherwise spill into an ungenerated neighbouring chunk at 90/270 degrees.
  Rotation rotation=Rotation.values()[random.nextInt(4)];
  int width=rotatedWidth(t,rotation),depth=rotatedDepth(t,rotation);
  if(width>14||depth>14)return false;
  int dx=random.nextInt(17-width),dz=random.nextInt(17-depth);
  BlockPos start=chunk.add(dx,0,dz),center=start.add(width/2,0,depth/2);
  if(!world.isBlockLoaded(center))return false;
  int surface=world.getHeight(center).getY()-1;
  if(surface<8||surface+t.height-yOffset>254)return false;
  Biome desired=Content.BIOMES.get(biome);if(desired==null)return false;
  for(int x=0;x<width;x++)for(int z=0;z<depth;z++){
   BlockPos col=start.add(x,0,z);if(!world.isBlockLoaded(col)||world.getBiome(col)!=desired)return false;
   int top=world.getHeight(col).getY()-1;
   if(Math.abs(top-surface)>(name.equals("marble_ruins_1")?2:1)||world.getBlockState(new BlockPos(col.getX(),top,col.getZ())).getBlock()!=Content.BLOCKS.get(((ZoneBiome)desired).definition.top))return false;
  }
  return place(world,new BlockPos(start.getX(),surface-yOffset,start.getZ()),t,rotation,true);
 }
 static void decorate(World world,Random random,BlockPos chunk,String biome){
  if(biome.equals("marble_zone")){
   if(random.nextInt(3)==0)decorateSmall(world,random,chunk,random.nextBoolean()?"marble_garden_pillar_1":"marble_garden_pillar_2",biome, -1);
   if(random.nextInt(35)==0)decorateSmall(world,random,chunk,"marble_ruins_1",biome,7);
  }else if(biome.equals("green_hill_zone")){
   if(random.nextInt(12)==0)decorateSmall(world,random,chunk,"green_hill_mountain_small_1",biome,0);
   if(random.nextInt(5)==0)decorateSmall(world,random,chunk,"green_hill_neo_palm_tree",biome,-1);
   if(random.nextInt(9)==0)decorateSmall(world,random,chunk,"level_layout_3x3_ring_circle",biome,-1);
   if(random.nextInt(15)==0)decorateSmall(world,random,chunk,random.nextBoolean()?"level_layout_red_spring":"level_layout_modern_spring",biome,-1);
   if(random.nextInt(18)==0)decorateSmall(world,random,chunk,"ghz_tree_sapling",biome,-1);
   // 0.1.18: original three-piece Green Hill stone totem.
   if(random.nextInt(34)==0)decorateSmall(world,random,chunk,"green_hill_totem",biome,-1);
  }else if(biome.equals("emerald_hill_zone")){
   if(random.nextInt(5)==0)decorateSmall(world,random,chunk,"emerald_hill_neo_palm_tree",biome,-1);
  }else if(biome.equals("seaside_hill_zone")){
   if(random.nextInt(5)==0)decorateSmall(world,random,chunk,"beachpalm_tree_"+(1+random.nextInt(4)),biome,-1);
  }else if(biome.equals("angel_island_jungle")){
   if(random.nextInt(5)==0)decorateSmall(world,random,chunk,"angel_island_neo_palm_tree",biome,-1);
  }else if(biome.equals("mushroom_hill_zone")){
   if(random.nextInt(7)==0)decorateSmall(world,random,chunk,"giant_mushroom_1",biome,-1);
  }else if(biome.equals("spring_yard")){
   if(random.nextInt(12)==0)decorateSmall(world,random,chunk,"spring_yard_platform",biome,0);
   if(random.nextInt(10)==0)decorateSmall(world,random,chunk,random.nextBoolean()?"spring_yard_pillar_purple":"spring_yard_pillar_blue",biome,-1);
   if(random.nextInt(14)==0)decorateSmall(world,random,chunk,"spring_yard_searchlight",biome,-1);
   if(random.nextInt(48)==0)decorateSmall(world,random,chunk,"spring_yard_funnel",biome,0);
   if(random.nextInt(65)==0)decorateSmall(world,random,chunk,"spring_yard_bumper_pit",biome,0);
  }else if(biome.equals("ice_cap_zone")){
   if(random.nextInt(9)==0)decorateSmall(world,random,chunk,"ice_cap_icicle_floor",biome,-1);
   if(random.nextInt(18)==0)decorateSmall(world,random,chunk,"level_layout_ice_spring",biome,-1);
  }else if(biome.equals("sandopolis_zone")){
   if(random.nextInt(12)==0)decorateSmall(world,random,chunk,"pointed_southite_up_"+(1+random.nextInt(5)),biome,-1);
  }else if(biome.equals("aquatic_ruin_zone")){
   if(random.nextInt(18)==0)decorateSmall(world,random,chunk,"aquatic_ruin_platform_1",biome,-1);
   if(random.nextInt(55)==0)decorateSmall(world,random,chunk,"aquatic_ruin_big_platform_1",biome,0);
  }else if(biome.equals("press_garden_zone")){
   if(random.nextInt(24)==0)decorateSmall(world,random,chunk,random.nextBoolean()?"press_garden_factory_road_corner":"press_garden_factory_road_crossroad",biome,0);
   if(random.nextInt(24)==0)decorateSmall(world,random,chunk,"small_chilly_blossom_tree_"+(1+random.nextInt(3))+"_woodrow_"+(1+random.nextInt(4)),biome,-1);
   if(random.nextInt(14)==0)decorateSmall(world,random,chunk,"small_chilly_blossom_tree_"+(1+random.nextInt(3))+"_flowers",biome,-1);
  }else if(biome.equals("mirage_saloon_zone")){
   // Cactus templates use original multi-block geometry; keep only chunk-sized variants.
   if(random.nextInt(19)==0)decorateSmall(world,random,chunk,"mirage_saloon_cactus_"+(1+random.nextInt(3)),biome,-1);
   // Buried four-block plinth; only the bird head/pillar remain above sand.
   if(random.nextInt(46)==0)decorateSmall(world,random,chunk,"mirage_saloon_bird_totem_"+(1+random.nextInt(3)),biome,3);
  }else if(biome.equals("studiopolis_zone")){
   if(random.nextInt(29)==0)decorateSmall(world,random,chunk,"studiopolis_lamppost",biome,-1);
  }else if(biome.equals("night_carnival_zone")){
   if(random.nextInt(30)==0)decorateSmall(world,random,chunk,"night_carnival_lamppost",biome,-1);
  }else if(biome.equals("chemical_plant_zone")){
   if(random.nextInt(85)==0)decorateSmall(world,random,chunk,"cpz_tower_1",biome,0);
   if(random.nextInt(36)==0)decorateSmall(world,random,chunk,"cpz_pipes_1",biome,-1);
  }
 }
}
