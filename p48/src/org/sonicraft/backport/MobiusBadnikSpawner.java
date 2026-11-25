/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.state.IBlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityCreature
 *  net.minecraft.util.math.AxisAlignedBB
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.world.EnumDifficulty
 *  net.minecraft.world.World
 *  net.minecraft.world.WorldServer
 *  net.minecraft.world.biome.Biome
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  net.minecraftforge.fml.common.gameevent.TickEvent$Phase
 *  net.minecraftforge.fml.common.gameevent.TickEvent$WorldTickEvent
 *  org.sonicraft.backport.Badniks$Definition
 *  org.sonicraft.backport.Badniks$Spawn
 *  org.sonicraft.backport.Content
 *  org.sonicraft.backport.Content$BiomeDef
 *  org.sonicraft.backport.MobiusBadnikSpawner$Entry
 *  org.sonicraft.backport.ZoneBiome
 */
package org.sonicraft.backport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.sonicraft.backport.Badniks;
import org.sonicraft.backport.Content;
import org.sonicraft.backport.GroundBadnik;
import org.sonicraft.backport.MobiusBadnikSpawner;
import org.sonicraft.backport.SonicraftLegacy;
import org.sonicraft.backport.ZoneBiome;

public final class MobiusBadnikSpawner {
    private static final Map<String, List<Entry>> ZONES = new LinkedHashMap<String, List<Entry>>();
    private static final Map<String, String[]> ADAPTED_ZONES = new LinkedHashMap<String, String[]>();
    private static boolean enabled;
    private static int nearbyCap;
    private static int interval;
    private static int globalCap;
    private static long scanned;
    private static long spawned;
    private static long blocked;
    private static long unmatched;

    static String status(World world, BlockPos blockPos) {
        String string = world.getBiome(blockPos) instanceof ZoneBiome ? ((ZoneBiome)world.getBiome((BlockPos)blockPos)).definition.id : "not_a_sonicraft_biome";
        return "SoniCraft badnik spawning: enabled=" + enabled + ", zone=" + string + ", roster=" + MobiusBadnikSpawner.badniksForZone(string) + ", interval=" + interval + ", nearbyCap=" + nearbyCap + ", loadedCap=" + globalCap + ", scanned=" + scanned + ", spawned=" + spawned + ", blocked=" + blocked + ", unmatched=" + unmatched;
    }

    static void configure(boolean bl) {
        enabled = bl && SonicraftLegacy.config.getBoolean("supplementalWorldSpawns", "badniks", true, "Spawn zone-specific badniks around players in loaded Mobius chunks in daylight and darkness. Restart required.");
        nearbyCap = SonicraftLegacy.config.getInt("nearbyBadnikCap", "badniks", 8, 1, 32, "Maximum badniks within 64 blocks of each player. Restart required.");
        globalCap = SonicraftLegacy.config.getInt("loadedBadnikCap", "badniks", 96, 8, 256, "Safety limit across all loaded entities of one dimension. Restart required.");
        interval = SonicraftLegacy.config.getInt("badnikSpawnIntervalTicks", "badniks", 100, 20, 1200, "How often to attempt supplementary mob spawns. Restart required.");
        MobiusBadnikSpawner.buildZoneTable();
    }

    private static boolean isKnownZone(String string) {
        for (Content.BiomeDef biomeDef : Content.BIOME_DEFS) {
            if (!biomeDef.id.equals(string)) continue;
            return true;
        }
        return false;
    }

    private static void add(String string, String string2, int n) {
        if (!MobiusBadnikSpawner.isKnownZone(string) || n <= 0) {
            return;
        }
        List<Entry> list = ZONES.get(string);
        if (list == null) {
            list = new ArrayList<Entry>();
            ZONES.put(string, list);
        }
        list.add(new Entry(string2, n));
    }

    static void buildZoneTable() {
        ZONES.clear();
        for (Badniks.Definition d : Badniks.DEFINITIONS) {
            if(d.id.equals("egg_crusher"))continue;
            if(d.spawns!=null)for(Badniks.Spawn spawn:d.spawns)add(spawn.biomes.replace("sonicraft:",""),d.id,spawn.weight);
            String[] zones=ADAPTED_ZONES.get(d.id);
            if(zones!=null)for(String zone:zones)add(zone,d.id,6);
        }
    }

    static List<String> badniksForZone(String string) {
        List<Entry> list = ZONES.get(string);
        if (list == null) {
            return Collections.emptyList();
        }
        ArrayList<String> arrayList = new ArrayList<String>();
        for (Entry entry : list) {
            arrayList.add(entry.id);
        }
        return arrayList;
    }

    private static String choose(String string, Random random) {
        List<Entry> list = ZONES.get(string);
        if (list == null || list.isEmpty()) {
            return null;
        }
        int n = 0;
        for (Entry object : list) {
            n += object.weight;
        }
        int n2 = random.nextInt(n);
        for (Entry entry : list) {
            if ((n2 -= entry.weight) >= 0) continue;
            return entry.id;
        }
        return null;
    }

    private static String zoneAt(WorldServer worldServer, BlockPos blockPos) {
        Biome biome = worldServer.getBiome(blockPos);
        if (!(biome instanceof ZoneBiome)) {
            return null;
        }
        return ((ZoneBiome)biome).definition.id;
    }

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent worldTickEvent) {
        if (!enabled || worldTickEvent.phase != TickEvent.Phase.END || worldTickEvent.world.isRemote || !(worldTickEvent.world instanceof WorldServer)) {
            return;
        }
        WorldServer worldServer = (WorldServer)worldTickEvent.world;
        if (worldServer.getTotalWorldTime() % (long)interval != 0L || !worldServer.getGameRules().getBoolean("doMobSpawning")) {
            return;
        }
        if (worldServer.getDifficulty() == EnumDifficulty.PEACEFUL && !Badniks.peacefulBadniks((World)worldServer)) {
            return;
        }
        int n = 0;
        for (Entity entity : worldServer.loadedEntityList) {
            if (!(entity instanceof GroundBadnik)) continue;
            ++n;
        }
        if (n >= globalCap) {
            return;
        }
        for (net.minecraft.entity.player.EntityPlayer entity : worldServer.playerEntities) {
            AxisAlignedBB axisAlignedBB;
            if (entity.isSpectator() || entity.isDead || worldServer.rand.nextInt(3) == 0 || worldServer.getEntitiesWithinAABB(GroundBadnik.class, axisAlignedBB = new AxisAlignedBB(entity.getPosition()).grow(64.0, 48.0, 64.0)).size() >= nearbyCap) continue;
            for (int i = 0; i < 12; ++i) {
                int n2;
                int n3;
                BlockPos blockPos;
                ++scanned;
                int n4 = 24 + worldServer.rand.nextInt(29);
                int n5 = 24 + worldServer.rand.nextInt(29);
                if (worldServer.rand.nextBoolean()) {
                    n4 = -n4;
                }
                if (worldServer.rand.nextBoolean()) {
                    n5 = -n5;
                }
                if (!worldServer.isBlockLoaded(blockPos = new BlockPos(n3 = MathHelper.floor((double)entity.posX) + n4, 64, n2 = MathHelper.floor((double)entity.posZ) + n5))) {
                    ++blocked;
                    continue;
                }
                BlockPos blockPos2 = worldServer.getHeight(blockPos);
                if (blockPos2.getY() < 32 || blockPos2.getY() > 224) continue;
                String string = MobiusBadnikSpawner.zoneAt(worldServer, blockPos2);
                String string2 = MobiusBadnikSpawner.choose(string, worldServer.rand);
                if (string2 == null) {
                    ++unmatched;
                    continue;
                }
                if (!worldServer.isAirBlock(blockPos2) || !worldServer.isAirBlock(blockPos2.up())) {
                    ++blocked;
                    continue;
                }
                IBlockState iBlockState = worldServer.getBlockState(blockPos2.down());
                if (!iBlockState.getMaterial().blocksMovement()) {
                    ++blocked;
                    continue;
                }
                try {
                    EntityCreature entityCreature = Badniks.entityClass(string2).getConstructor(World.class).newInstance(worldServer);
                    entityCreature.setLocationAndAngles((double)n3 + 0.5, (double)blockPos2.getY(), (double)n2 + 0.5, worldServer.rand.nextFloat() * 360.0f, 0.0f);
                    if (!entityCreature.getCanSpawnHere()) {
                        ++blocked;
                        continue;
                    }
                    if (!worldServer.spawnEntity((Entity)entityCreature)) continue;
                    ++spawned;
                    ++n;
                    break;
                }
                catch (ReflectiveOperationException reflectiveOperationException) {
                    throw new IllegalStateException("SoniCraft badnik spawn registration: " + string2, reflectiveOperationException);
                }
            }
            if (n < globalCap) continue;
            break;
        }
    }

    static class Entry { final String id; final int weight; Entry(String id,int weight){this.id=id;this.weight=weight;} }
    static {
        ADAPTED_ZONES.put("burrobot", new String[]{"sandopolis_zone", "mirage_saloon_zone"});
        ADAPTED_ZONES.put("batbrain", new String[]{"marble_zone"});
        ADAPTED_ZONES.put("woodrow", new String[]{"press_garden_zone"});
        ADAPTED_ZONES.put("grabber", new String[]{"chemical_plant_zone"});
        ADAPTED_ZONES.put("newtron", new String[]{"green_hill_zone", "emerald_hill_zone"});
        ADAPTED_ZONES.put("bomb_badnik", new String[]{"scrap_brain_zone"});
        ADAPTED_ZONES.put("egg_flapper", new String[]{"scrap_brain_zone"});
        ADAPTED_ZONES.put("spikes_badnik", new String[]{"marble_zone"});
        ADAPTED_ZONES.put("rexon", new String[]{"aquatic_ruin_zone"});
        ADAPTED_ZONES.put("crabmeat", new String[]{"seaside_hill_zone"});
        ADAPTED_ZONES.put("caterkiller", new String[]{"mushroom_hill_zone"});
        ADAPTED_ZONES.put("buzz_bomber", new String[]{"star_light_zone", "angel_island_jungle"});
        ADAPTED_ZONES.put("buzzer", new String[]{"ice_cap_zone"});
        enabled = true;
        nearbyCap = 8;
        interval = 100;
        globalCap = 96;
    }
}
