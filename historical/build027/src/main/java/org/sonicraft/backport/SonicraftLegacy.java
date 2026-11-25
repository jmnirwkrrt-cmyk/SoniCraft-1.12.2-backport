package org.sonicraft.backport;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.*;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
@Mod(modid=SonicraftLegacy.ID,name="SoniCraft Legacy",version="0.1.26",acceptedMinecraftVersions="[1.12.2]",dependencies="required-after:geckolib3@[3.0.30,)")
@Mod.EventBusSubscriber(modid=SonicraftLegacy.ID)
public class SonicraftLegacy {
 @Mod.Instance(SonicraftLegacy.ID) public static SonicraftLegacy instance;
 @Mod.EventHandler public void serverStarting(FMLServerStartingEvent e){e.registerServerCommand(new FindZoneCommand());e.registerServerCommand(new StructureCommand());}
 public static final String ID="sonicraft";
 static boolean tailsFlight=true;
 static boolean biomeMusic=true;
 static Configuration config;static boolean overworld;static Set<String> enabled;
 @net.minecraftforge.fml.common.SidedProxy(clientSide="org.sonicraft.backport.ClientProxy",serverSide="org.sonicraft.backport.CommonProxy") public static CommonProxy proxy;
 public SonicraftLegacy(){software.bernie.geckolib3.GeckoLib.initialize();}
 public static final CreativeTabs TAB=CreativeCatalog.BLOCKS;
 @Mod.EventHandler public void preInit(FMLPreInitializationEvent e){net.minecraftforge.fml.common.registry.GameRegistry.registerTileEntity(ExtendedStructureBlocks.StateTile.class,new net.minecraft.util.ResourceLocation(ID,"extended_source_state"));config=new Configuration(e.getSuggestedConfigurationFile());config.load();RegionalBiomes.configure();MarbleDungeonWorldgen.configure();biomeMusic=config.getBoolean("biomeMusic","client",true,"Play the original zone tracks on the ambient sound channel. Restart required.");tailsFlight=config.getBoolean("tailsFlight","characters",true,"Allow Tails to make short flights between dry landing sites. Restart required.");overworld=config.getBoolean("enabled","overworld",true,"Add selected zones to the normal Overworld. New chunks only. Restart required.");String[] defaults={"green_hill_zone","emerald_hill_zone","hill_top_zone","aquatic_ruin_zone","seaside_hill_zone","angel_island_jungle","ice_cap_zone","mushroom_hill_zone","sandopolis_zone","mirage_saloon_zone"};enabled=new HashSet<>(Arrays.asList(config.getStringList("biomes","overworld",defaults,"Biome registry paths. Disabled biomes remain registered for saves.")));new ZoneWorldType();EmeraldMenu.register();StructurePort.registerLoot();proxy.preInit();}
 @SubscribeEvent public static void blocks(RegistryEvent.Register<Block> e){for(Content.BlockDef d:Content.BLOCK_DEFS){Block b=LegacyBlocks.create(d);b.setRegistryName(ID,d.id).setUnlocalizedName(ID+"."+d.id).setCreativeTab(MarbleColumn.visible(b)&&!d.kind.equals("source_boss_marker")?CreativeCatalog.forId(d.id):null);Content.BLOCKS.put(d.id,b);e.getRegistry().register(b);}for(Block b:ShapeBlocks.DOUBLES.values())e.getRegistry().register(b);}
 @SubscribeEvent public static void portalBlock(RegistryEvent.Register<Block> e){e.getRegistry().register(MobiusPortal.BLOCK);}
 @SubscribeEvent public static void items(RegistryEvent.Register<Item> e){for(Block b:Content.BLOCKS.values())e.getRegistry().register(ShapeBlocks.item(b).setRegistryName(b.getRegistryName()).setCreativeTab(MarbleColumn.visible(b)&&!(b instanceof ExtendedStructureBlocks.Marker)?CreativeCatalog.forId(b.getRegistryName().getResourcePath()):null));for(Item item:EmeraldProgression.ITEMS.values())e.getRegistry().register(item);for(Item item:FoodContent.ITEMS.values())e.getRegistry().register(item.setCreativeTab(CreativeCatalog.forId(item.getRegistryName().getResourcePath())));e.getRegistry().register(MobiusPortal.IGNITER.setCreativeTab(CreativeCatalog.MISC));}
 @SubscribeEvent public static void contentSounds(RegistryEvent.Register<net.minecraft.util.SoundEvent> e){for(net.minecraft.util.SoundEvent sound:EmeraldProgression.SOUNDS.values())e.getRegistry().register(sound);for(net.minecraft.util.SoundEvent sound:FoodContent.SOUNDS.values())e.getRegistry().register(sound);for(net.minecraft.util.SoundEvent sound:BiomeMusic.SOUNDS.values())e.getRegistry().register(sound);}
 @SubscribeEvent public static void biomes(RegistryEvent.Register<Biome> e){for(Content.BiomeDef d:Content.BIOME_DEFS){ZoneBiome b=new ZoneBiome(d);b.setRegistryName(ID,d.id);Content.BIOMES.put(d.id,b);e.getRegistry().register(b);}}
 @Mod.EventHandler public void init(FMLInitializationEvent e){for(Content.BiomeDef d:Content.BIOME_DEFS){ZoneBiome b=Content.BIOMES.get(d.id);BiomeDictionary.addTypes(b,d.height>=.8?BiomeDictionary.Type.MOUNTAIN:BiomeDictionary.Type.PLAINS);if(d.climate.equals("DESERT"))BiomeDictionary.addTypes(b,BiomeDictionary.Type.HOT,BiomeDictionary.Type.DRY,BiomeDictionary.Type.SANDY);else if(d.climate.equals("ICY"))BiomeDictionary.addTypes(b,BiomeDictionary.Type.COLD,BiomeDictionary.Type.SNOWY);else BiomeDictionary.addTypes(b,BiomeDictionary.Type.LUSH);int weight=config.getInt(d.id,"weights",3,0,100,"Relative climate weight, NOT percent. 0 disables generation. Restart required.");if(overworld&&enabled.contains(d.id)&&weight>0){BiomeManager.addBiome(BiomeManager.BiomeType.valueOf(d.climate),new BiomeManager.BiomeEntry(b,weight));BiomeManager.addSpawnBiome(b);}}
  for(Content.BlockDef d:Content.BLOCK_DEFS){if(d.kind.equals("log"))net.minecraftforge.oredict.OreDictionary.registerOre("logWood",Content.BLOCKS.get(d.id));if(d.kind.equals("wood"))net.minecraftforge.oredict.OreDictionary.registerOre("plankWood",Content.BLOCKS.get(d.id));if(d.kind.equals("sapling"))net.minecraftforge.oredict.OreDictionary.registerOre("treeSapling",Content.BLOCKS.get(d.id));}MinecraftForge.EVENT_BUS.register(new ZoneTerrain());MinecraftForge.EVENT_BUS.register(new MarbleDungeonWorldgen());Characters.addSpawns();Badniks.addSpawns();MobiusWorld.register();config.save();}
}
