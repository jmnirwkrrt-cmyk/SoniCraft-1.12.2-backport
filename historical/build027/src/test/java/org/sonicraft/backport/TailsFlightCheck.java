package org.sonicraft.backport;

import java.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.*;

/** Runs real Entity.move collision resolution against a small in-memory block world. */
public class TailsFlightCheck {
    static final class FlatWorld extends CharacterSmokeCheck.TestWorld {
        boolean ceiling, unloaded, water;
        Set<BlockPos> obstacles = new HashSet<>();
        @Override protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
            return !unloaded && x >= -2 && x <= 2 && z >= -2 && z <= 2;
        }
        @Override public IBlockState getBlockState(BlockPos p) {
            if (p.getY() < 64) return Blocks.STONE.getDefaultState();
            if (water && p.getY() == 64) return Blocks.WATER.getDefaultState();
            if ((ceiling && p.getY() == 66) || obstacles.contains(p)) return Blocks.STONE.getDefaultState();
            return Blocks.AIR.getDefaultState();
        }
        @Override public List<AxisAlignedBB> getCollisionBoxes(Entity entity, AxisAlignedBB box) {
            List<AxisAlignedBB> collisions = new ArrayList<>();
            for (BlockPos p : BlockPos.getAllInBox(new BlockPos(box.minX, box.minY - 1, box.minZ),
                    new BlockPos(box.maxX, box.maxY + 1, box.maxZ))) {
                IBlockState state = getBlockState(p);
                state.addCollisionBoxToList(this, p, box, collisions, entity, false);
            }
            return collisions;
        }
        @Override public boolean isRainingAt(BlockPos pos) { return false; }
        @Override public List<Entity> getEntitiesInAABBexcluding(Entity e,AxisAlignedBB box,com.google.common.base.Predicate<? super Entity> filter) { return Collections.emptyList(); }
        @Override public int getLightFromNeighbors(BlockPos p) { return 15; }
        @Override public float getLightBrightness(BlockPos p) { return 1; }
        @Override public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity e,AxisAlignedBB box) { return Collections.emptyList(); }
        @Override public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> cls,AxisAlignedBB box,com.google.common.base.Predicate<? super T> filter) { return Collections.emptyList(); }
    }
    static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static LegacyCharacter.Tails tails(FlatWorld w) {
        LegacyCharacter.Tails t = new LegacyCharacter.Tails(w);
        t.setPosition(.5, 64, .5); t.onGround = true; t.getRNG().setSeed(55L);
        return t;
    }
    static NBTTagCompound save(LegacyCharacter.Tails t) {
        NBTTagCompound n = new NBTTagCompound(); t.writeToNBT(n); return n;
    }
    static LegacyCharacter.Tails reload(FlatWorld w, LegacyCharacter.Tails t) {
        LegacyCharacter.Tails copy = new LegacyCharacter.Tails(w); copy.readFromNBT(save(t)); return copy;
    }
    public static void main(String[] args) {
        Bootstrap.register();
        FlatWorld world = new FlatWorld();
        LegacyCharacter.Tails t = tails(world);
        require(t.flight.planFlight(), "Open air must permit flight");
        t.flight.startExecuting();
        boolean ascent = false, cruise = false, descent = false, reloaded = false;
        double high = 64;
        for (int tick = 0; tick < 300 && t.flight.airborne(); ++tick) {
            t.flight.tick(); t.flight.updateTask();
            ascent |= t.flight.phase() == TailsFlightAI.ASCEND;
            cruise |= t.flight.phase() == TailsFlightAI.CRUISE;
            descent |= t.flight.phase() == TailsFlightAI.LAND;
            if (!t.flight.airborne()) break;
            t.travel(0, 0, 0); high = Math.max(high, t.posY);
            require(world.getCollisionBoxes(t, t.getEntityBoundingBox().shrink(.001)).isEmpty(), "Flight intersects blocks");
            if (!reloaded && t.flight.phase() == TailsFlightAI.CRUISE) {
                int oldPhase = t.flight.phase();
                t = reload(world, t);
                require(t.flight.phase() == oldPhase && t.hasNoGravity() && t.controlledFlight(), "Mid-flight persistence/synced phase");
                t.flight.startExecuting(); reloaded = true;
            }
        }
        require(ascent && cruise && descent && reloaded && high > 66.5, "All flight phases must run");
        require(!t.flight.airborne() && t.onGround && !t.hasNoGravity() && Math.abs(t.posY - 64) < .01, "Must land and restore gravity");
        require(!t.flight.shouldExecute(), "Cooldown must prevent immediate relaunch");
        require(t.getHealth() == t.getMaxHealth(), "Normal landing must not hurt");

        FlatWorld low = new FlatWorld(); low.ceiling = true;
        require(!tails(low).flight.planFlight(), "Do not launch under low ceilings");
        FlatWorld missing = new FlatWorld(); missing.unloaded = true;
        require(!tails(missing).flight.planFlight(), "Do not plan in unloaded chunks");
        FlatWorld flooded = new FlatWorld(); flooded.water = true;
        require(!tails(flooded).flight.planFlight(), "Do not choose water landings");
        LegacyCharacter.Tails blocked = tails(world);
        world.obstacles.add(new BlockPos(2,65,0));
        require(!blocked.flight.clearRoute(new Vec3d(.5,65,.5),new Vec3d(4.5,65,.5)), "Body-width route must reject walls");
        world.obstacles.clear();

        LegacyCharacter.Tails disabled = tails(world);
        require(disabled.flight.planFlight(), "Plan before disabling"); disabled.flight.startExecuting();
        SonicraftLegacy.tailsFlight = false; disabled.flight.tick();
        require(!disabled.flight.airborne() && !disabled.hasNoGravity(), "Config disable must release gravity");
        disabled.onGround=true; disabled.flight.tick();
        require(!disabled.flight.protectsFall(), "Fall protection must expire on ground");
        SonicraftLegacy.tailsFlight = true;
        LegacyCharacter.Tails noAI = tails(world);
        require(noAI.flight.planFlight(), "Plan before NoAI"); noAI.flight.startExecuting(); noAI.setNoAI(true); noAI.flight.tick();
        require(!noAI.flight.airborne() && !noAI.hasNoGravity(), "NoAI must not leave a hovering NPC");

        LegacyCharacter.Tails damaged = tails(world);
        require(damaged.flight.planFlight(), "Plan for invalid NBT");
        NBTTagCompound invalid = save(damaged); invalid.getCompoundTag("SonicraftFlight").setDouble("CruiseY",Double.NaN);
        LegacyCharacter.Tails safe = new LegacyCharacter.Tails(world); safe.readFromNBT(invalid);
        require(!safe.flight.airborne() && !safe.hasNoGravity(), "Reject non-finite saved plans");
        NBTTagCompound old = save(tails(world)); old.removeTag("SonicraftFlight");
        safe.readFromNBT(old); require(!safe.flight.airborne(), "Load 0.1.0 saves");
        LegacyCharacter.Tails scheduled = tails(new FlatWorld());
        NBTTagCompound ready = new NBTTagCompound();
        ready.setTag("SonicraftFlight",new NBTTagCompound());
        scheduled.flight.read(ready);
        boolean tookOff = false;
        for(int i=0;i<300;i++) {
            scheduled.onLivingUpdate();
            tookOff |= scheduled.flight.airborne();
            if(tookOff && !scheduled.flight.airborne()) break;
        }
        require(tookOff && !scheduled.flight.airborne() && !scheduled.hasNoGravity() && Math.abs(scheduled.posY-64)<.01, "Normal AI scheduler must complete a flight");
        System.out.println("PASS: AI scheduler; Tails takeoff/cruise/landing with vanilla collision resolution; mid-flight NBT; gravity/cooldown; ceilings, walls, water, unloaded chunks; NoAI/config disable; invalid and old NBT");
    }
}
