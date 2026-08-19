package com.touhoumaid.eatproperly.snackraid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SnackRaidPathing {
    static final float APPROACH_SPEED = SnackRaidFleePolicy.APPROACH_SPEED;
    static final float FLEE_SPEED = SnackRaidFleePolicy.FLEE_SPEED;
    /** Close-enough radius handed to the brain's walk target, in blocks. */
    static final int WALK_TARGET_CLOSE_ENOUGH = 1;
    /** Ticks after arriving at the chest, before playing chest-open sound (1 s). */
    static final int WAIT_BEFORE_SOUND_TICKS = 20;
    static final int WAIT_TOTAL_TICKS = 80;
    static final int APPROACH_TIMEOUT_TICKS = 600;
    static final int FLEE_TIMEOUT_TICKS = SnackRaidFleePolicy.FLEE_TIMEOUT_TICKS;
    static final int REPATH_INTERVAL_TICKS = 10;

    private static final BlockPos[] HORIZONTAL_OFFSETS = {
            new BlockPos(1, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, 1),
            new BlockPos(0, 0, -1)
    };

    private SnackRaidPathing() {
    }

    public static BlockPos pickStandPos(ServerLevel level, EntityMaid maid, BlockPos containerPos) {
        List<BlockPos> candidates = new ArrayList<>(4);
        for (BlockPos offset : HORIZONTAL_OFFSETS) {
            BlockPos stand = containerPos.offset(offset);
            if (canStand(level, maid, stand)) {
                candidates.add(stand);
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        candidates.sort(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition())));
        for (BlockPos stand : candidates) {
            if (hasPath(maid, stand)) {
                return stand;
            }
        }
        return null;
    }

    public static BlockPos pickFleePos(ServerLevel level, EntityMaid maid, BlockPos containerPos) {
        Vec3 chestCenter = Vec3.atCenterOf(containerPos);
        double[] away = SnackRaidFleePolicy.awayDirection(
                maid.getX(), maid.getY(), maid.getZ(),
                chestCenter.x, chestCenter.y, chestCenter.z);
        Vec3 awayVec = new Vec3(away[0], away[1], away[2]);

        BlockPos preferred = findStandNear(level, maid, maid.blockPosition(), awayVec, 8, containerPos);
        if (preferred != null) {
            return preferred;
        }

        // Prefer cardinal directions that continue away from the chest (dot > 0), then others.
        List<Direction> dirs = new ArrayList<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            dirs.add(dir);
        }
        dirs.sort(Comparator.comparingDouble((Direction dir) -> {
            Vec3 n = Vec3.atLowerCornerOf(dir.getNormal());
            return -(n.x * awayVec.x + n.z * awayVec.z);
        }));
        for (Direction dir : dirs) {
            Vec3 dirVec = Vec3.atLowerCornerOf(dir.getNormal());
            BlockPos fallback = findStandNear(level, maid, maid.blockPosition(), dirVec, 6, containerPos);
            if (fallback != null) {
                return fallback;
            }
        }
        return null;
    }

    static boolean hasPath(EntityMaid maid, BlockPos stand) {
        Path path = maid.getNavigation().createPath(
                stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D, 0);
        return path != null && path.canReach();
    }

    static boolean canStand(Level level, EntityMaid maid, BlockPos stand) {
        if (!hasSolidSupport(level, stand)) {
            return false;
        }
        if (!level.isEmptyBlock(stand) || !level.isEmptyBlock(stand.above())) {
            return false;
        }
        AABB box = maid.getBoundingBox().move(
                stand.getX() + 0.5D - maid.getX(),
                stand.getY() - maid.getY(),
                stand.getZ() + 0.5D - maid.getZ());
        return level.noCollision(maid, box);
    }

    static boolean hasSolidSupport(Level level, BlockPos stand) {
        BlockState below = level.getBlockState(stand.below());
        return below.isSolid() && !below.getFluidState().is(FluidTags.LAVA);
    }

    static boolean isSafeSpot(Level level, EntityMaid maid, BlockPos stand) {
        return canStand(level, maid, stand);
    }

    static void faceBlock(EntityMaid maid, BlockPos pos) {
        maid.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(pos));
    }

    /**
     * Walking goes through the maid's brain memories rather than {@code getNavigation().moveTo}.
     * The brain's own {@code MoveToTargetSink} owns the navigator and runs later in the same tick,
     * so a directly issued path gets overwritten. Holding WALK_TARGET also keeps idle behaviours
     * such as {@code RandomStroll} from starting, since those require the memory to be absent.
     */
    static void walkToStand(EntityMaid maid, BlockPos stand, BlockPos container) {
        walkToStand(maid, stand, container, APPROACH_SPEED);
    }

    static void walkToStand(EntityMaid maid, BlockPos stand, BlockPos container, float speed) {
        BehaviorUtils.setWalkAndLookTargetMemories(maid, stand, speed, WALK_TARGET_CLOSE_ENOUGH);
        faceBlock(maid, container);
    }

    static void walkToFlee(EntityMaid maid, BlockPos flee) {
        BehaviorUtils.setWalkAndLookTargetMemories(maid, flee, FLEE_SPEED, WALK_TARGET_CLOSE_ENOUGH);
    }

    /** Pin the maid down for the wait: drop the walk target, then cancel whatever path the brain queued. */
    static void holdStill(EntityMaid maid) {
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getNavigation().stop();
    }

    /** Hand movement back to the brain when the raid ends. */
    static void releaseControl(EntityMaid maid) {
        SnackRaidPathLock.release(maid);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        maid.getNavigation().stop();
    }

    /**
     * True when something else replaced our walk target. Absent memory is not a hijack: the brain
     * drops it on arrival or on a failed path, and the regular refresh re-issues it.
     */
    static boolean isWalkTargetHijacked(EntityMaid maid, BlockPos ours) {
        return maid.getBrain().getMemory(MemoryModuleType.WALK_TARGET)
                .map(target -> !target.getTarget().currentBlockPosition().equals(ours))
                .orElse(false);
    }

    /** Human-readable walk target for the debug command. */
    public static String describeWalkTarget(EntityMaid maid) {
        return maid.getBrain().getMemory(MemoryModuleType.WALK_TARGET)
                .map(target -> target.getTarget().currentBlockPosition().toShortString()
                        + " speed=" + target.getSpeedModifier())
                .orElse("(无)");
    }

    static double distanceSqTo(EntityMaid maid, BlockPos pos) {
        double dx = maid.getX() - (pos.getX() + 0.5D);
        double dy = maid.getY() - pos.getY();
        double dz = maid.getZ() - (pos.getZ() + 0.5D);
        return dx * dx + dy * dy + dz * dz;
    }

    private static BlockPos findStandNear(ServerLevel level, EntityMaid maid, BlockPos origin, Vec3 direction,
                                          int distance, BlockPos chestPos) {
        Vec3 target = Vec3.atCenterOf(origin).add(direction.scale(distance));
        BlockPos base = BlockPos.containing(target);
        List<BlockPos> candidates = new ArrayList<>();
        for (int radius = 0; radius <= 3; radius++) {
            for (BlockPos candidate : spiralAround(base, radius)) {
                if (isSafeSpot(level, maid, candidate)) {
                    candidates.add(candidate.immutable());
                }
            }
        }
        candidates.sort(Comparator.comparingDouble(
                (BlockPos pos) -> -SnackRaidFleePolicy.distSqr(
                        pos.getX(), pos.getY(), pos.getZ(),
                        chestPos.getX(), chestPos.getY(), chestPos.getZ())));
        for (BlockPos candidate : candidates) {
            if (hasPath(maid, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static List<BlockPos> spiralAround(BlockPos center, int radius) {
        List<BlockPos> list = new ArrayList<>();
        if (radius == 0) {
            list.add(center);
            return list;
        }
        for (int x = -radius; x <= radius; x++) {
            list.add(center.offset(x, 0, -radius));
            list.add(center.offset(x, 0, radius));
        }
        for (int z = -radius + 1; z < radius; z++) {
            list.add(center.offset(-radius, 0, z));
            list.add(center.offset(radius, 0, z));
        }
        return list;
    }
}
