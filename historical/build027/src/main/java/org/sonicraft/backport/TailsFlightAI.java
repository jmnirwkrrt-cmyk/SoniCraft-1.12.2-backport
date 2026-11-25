package org.sonicraft.backport;

import java.util.List;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.state.IBlockState;

/** Short, server-authoritative flights between two dry, unobstructed landing sites. */
public final class TailsFlightAI extends EntityAIBase {
    static final int GROUND = 0, ASCEND = 1, CRUISE = 2, LAND = 3;
    private final LegacyCharacter.Tails tails;
    private Vec3d departure, destination;
    private double cruiseY;
    private int phase, elapsed, cooldown = 80;
    private boolean softLanding;

    public TailsFlightAI(LegacyCharacter.Tails tails) {
        this.tails = tails;
        setMutexBits(3); // Own movement and facing until touchdown.
    }

    public boolean airborne() { return phase != GROUND; }
    boolean protectsFall() { return softLanding; }
    int phase() { return phase; }

    public void tick() {
        if (tails.world.isRemote) return;
        if (!airborne() && tails.onGround) softLanding = false;
        if (cooldown > 0) --cooldown;
        if (airborne() && (!SonicraftLegacy.tailsFlight || tails.isAIDisabled()
                || tails.isRiding() || tails.isInWater() || tails.isInLava()
                || !tails.isEntityAlive())) abort();
    }

    @Override public boolean shouldExecute() {
        if (airborne()) return true;
        if (CharacterFoodAI.findPlayer(tails) != null) return false;
        if (!SonicraftLegacy.tailsFlight || cooldown > 0 || !tails.onGround
                || tails.isAIDisabled() || tails.isRiding() || tails.isInWater()
                || tails.isInLava() || tails.getAttackTarget() != null) return false;
        cooldown = 80; // A low ceiling must not cause a search every tick.
        return planFlight();
    }

    @Override public boolean shouldContinueExecuting() { return airborne(); }
    @Override public boolean isInterruptible() { return true; }

    boolean planFlight() {
        Vec3d start = findLanding(tails.posX, tails.posZ, tails.posY);
        if (start == null || Math.abs(start.y - tails.posY) > .15) return false;
        for (int attempt = 0; attempt < 8; ++attempt) {
            double angle = tails.getRNG().nextDouble() * Math.PI * 2;
            double distance = 3 + tails.getRNG().nextDouble() * 4;
            Vec3d end = findLanding(start.x + Math.cos(angle) * distance,
                    start.z + Math.sin(angle) * distance, start.y);
            if (end == null) continue;
            double height = Math.max(start.y, end.y) + 3;
            Vec3d up = new Vec3d(start.x, height, start.z);
            Vec3d across = new Vec3d(end.x, height, end.z);
            if (!clearRoute(start, up) || !clearRoute(up, across) || !clearRoute(across, end)) continue;
            departure = start;
            destination = end;
            cruiseY = height;
            elapsed = 0;
            phase = ASCEND;
            softLanding = true;
            tails.setFlightPhase(phase);
            return true;
        }
        return false;
    }

    @Override public void startExecuting() {
        tails.getNavigator().clearPath();
        tails.getMoveHelper().setMoveTo(tails.posX, tails.posY, tails.posZ, 0);
        tails.setAIMoveSpeed(0);
        tails.moveForward = tails.moveStrafing = 0;
        tails.setJumping(false);
        tails.setNoGravity(true);
        tails.fallDistance = 0;
    }

    @Override public void updateTask() {
        if (!airborne()) return;
        // Revalidate saved plans and live changes before using their coordinates.
        if (!validPlan() || ++elapsed > 260) { abort(); return; }
        tails.setNoGravity(true);
        tails.fallDistance = 0;
        tails.moveForward = tails.moveStrafing = 0;
        tails.setJumping(false);
        if ((tails.getAttackTarget() != null || CharacterFoodAI.findPlayer(tails) != null) && phase != LAND) {
            if (!landBelow()) { abort(); return; }
        }
        Vec3d target = phase == ASCEND ? new Vec3d(departure.x, cruiseY, departure.z)
                : phase == CRUISE ? new Vec3d(destination.x, cruiseY, destination.z)
                : destination.addVector(0, -.12, 0);
        double dx = target.x - tails.posX, dy = target.y - tails.posY, dz = target.z - tails.posZ;
        if (phase == LAND && tails.onGround && Math.abs(tails.posY - destination.y) < .3) {
            finish(); return;
        }
        if (phase != LAND && dx * dx + dy * dy + dz * dz < .09) {
            phase = phase == ASCEND ? CRUISE : LAND;
            tails.setFlightPhase(phase);
        }
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double speed = horizontal > .01 ? Math.min(.16, horizontal * .3) / horizontal : 0;
        double mx = dx * speed, mz = dz * speed, my = MathHelper.clamp(dy * .3, -.14, .14);
        Vec3d next = new Vec3d(tails.posX + mx, tails.posY + my, tails.posZ + mz);
        // Touching the floor during the final descent is intentional; move() resolves it.
        boolean touchdown = phase == LAND && my < 0 && horizontal < .3
                && tails.posY - destination.y < .25 && safeFloor(destination);
        if (!clearBody(next) && !touchdown) {
            tails.motionX = tails.motionY = tails.motionZ = 0;
            if (phase == LAND || !landBelow()) abort();
            return;
        }
        tails.motionX = mx; tails.motionY = my; tails.motionZ = mz;
        if (horizontal > .1) {
            tails.rotationYaw = (float)(Math.atan2(dz, dx) * 180 / Math.PI) - 90;
            tails.renderYawOffset = tails.rotationYaw;
            tails.getLookHelper().setLookPosition(target.x, target.y + tails.getEyeHeight(), target.z, 30, 30);
        }
    }

    private boolean landBelow() {
        Vec3d landing = findLanding(tails.posX, tails.posZ, tails.posY);
        if (landing == null || landing.y > tails.posY + .1
                || !clearRoute(new Vec3d(tails.posX, tails.posY, tails.posZ), landing)) return false;
        destination = landing;
        phase = LAND;
        tails.setFlightPhase(phase);
        return true;
    }

    private boolean validPlan() {
        return departure != null && destination != null && finite(departure) && finite(destination)
                && Double.isFinite(cruiseY) && cruiseY >= 1 && cruiseY <= 250
                && departure.squareDistanceTo(tails.posX, tails.posY, tails.posZ) < 400
                && destination.squareDistanceTo(tails.posX, tails.posY, tails.posZ) < 400;
    }
    private static boolean finite(Vec3d v) { return Double.isFinite(v.x) && Double.isFinite(v.y) && Double.isFinite(v.z); }

    Vec3d findLanding(double x, double z, double aroundY) {
        for (int y = Math.min(248, MathHelper.floor(aroundY) + 3); y >= Math.max(1, MathHelper.floor(aroundY) - 7); --y) {
            Vec3d feet = new Vec3d(x, y, z);
            if (safeFloor(feet) && clearBody(feet)) return feet;
        }
        return null;
    }

    private boolean safeFloor(Vec3d feet) {
        BlockPos floor = new BlockPos(feet.x, feet.y - .01, feet.z);
        if (!tails.world.isBlockLoaded(floor)) return false;
        IBlockState state = tails.world.getBlockState(floor);
        return state.isNormalCube() && state.getBlock() != Blocks.MAGMA
                && state.getBlock() != Blocks.CACTUS && state.getBlock() != Blocks.FIRE;
    }

    boolean clearBody(Vec3d feet) {
        double half = tails.width / 2.0 + .03;
        AxisAlignedBB box = new AxisAlignedBB(feet.x - half, feet.y + .001, feet.z - half,
                feet.x + half, feet.y + tails.height + .03, feet.z + half);
        for (BlockPos pos : BlockPos.getAllInBox(new BlockPos(box.minX, box.minY, box.minZ),
                new BlockPos(box.maxX, box.maxY, box.maxZ))) {
            if (!tails.world.isBlockLoaded(pos)) return false;
            if (tails.world.getBlockState(pos).getMaterial().isLiquid()) return false;
        }
        List<AxisAlignedBB> collisions = tails.world.getCollisionBoxes(tails, box);
        return collisions.isEmpty();
    }

    boolean clearRoute(Vec3d from, Vec3d to) {
        int steps = Math.max(1, (int)Math.ceil(from.distanceTo(to) * 4));
        if (steps > 100) return false;
        for (int i = 0; i <= steps; ++i) {
            double f = (double)i / steps;
            if (!clearBody(new Vec3d(from.x + (to.x - from.x) * f,
                    from.y + (to.y - from.y) * f, from.z + (to.z - from.z) * f))) return false;
        }
        return true;
    }

    private void finish() {
        abort();
        softLanding = false;
        cooldown = 200 + tails.getRNG().nextInt(201);
    }
    private void abort() {
        phase = GROUND;
        tails.setFlightPhase(GROUND);
        tails.setNoGravity(false);
        tails.motionX = tails.motionZ = 0;
        tails.fallDistance = 0;
        cooldown = Math.max(cooldown, 100);
    }
    @Override public void resetTask() { if (airborne()) abort(); }

    public void write(NBTTagCompound entityTag) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Phase", phase); tag.setInteger("Ticks", elapsed);
        tag.setInteger("Cooldown", cooldown); tag.setBoolean("SoftLanding", softLanding);
        if (departure != null && destination != null) {
            tag.setDouble("StartX", departure.x); tag.setDouble("StartY", departure.y); tag.setDouble("StartZ", departure.z);
            tag.setDouble("EndX", destination.x); tag.setDouble("EndY", destination.y); tag.setDouble("EndZ", destination.z);
            tag.setDouble("CruiseY", cruiseY);
        }
        entityTag.setTag("SonicraftFlight", tag);
    }
    public void read(NBTTagCompound entityTag) {
        if (!entityTag.hasKey("SonicraftFlight", 10)) return; // Old 0.1.0 saves.
        NBTTagCompound tag = entityTag.getCompoundTag("SonicraftFlight");
        phase = MathHelper.clamp(tag.getInteger("Phase"), GROUND, LAND);
        elapsed = MathHelper.clamp(tag.getInteger("Ticks"), 0, 260);
        cooldown = MathHelper.clamp(tag.getInteger("Cooldown"), 0, 1200);
        softLanding = tag.getBoolean("SoftLanding") || airborne();
        departure = new Vec3d(tag.getDouble("StartX"), tag.getDouble("StartY"), tag.getDouble("StartZ"));
        destination = new Vec3d(tag.getDouble("EndX"), tag.getDouble("EndY"), tag.getDouble("EndZ"));
        cruiseY = tag.getDouble("CruiseY");
        if (airborne() && (!tag.hasKey("CruiseY", 6) || !validPlan())) abort();
        tails.setFlightPhase(phase);
        tails.setNoGravity(airborne());
    }
}
