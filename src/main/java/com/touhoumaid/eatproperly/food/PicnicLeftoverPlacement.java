package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * After a picnic/home meal, prefer inserting leftovers into nearby containers
 * centered on the picnic mat (radius {@link PicnicLeftoverPolicy#SEARCH_RADIUS}).
 */
public final class PicnicLeftoverPlacement {
    private PicnicLeftoverPlacement() {
    }

    /**
     * Tries nearby containers first. Returns whatever could not be inserted
     * (caller should fall back to maid inventory / drop).
     */
    public static ItemStack tryPlaceNearPicnicMat(EntityMaid maid, ItemStack leftover) {
        if (leftover.isEmpty()) {
            return leftover;
        }
        if (!(maid.level() instanceof ServerLevel level)) {
            return leftover;
        }
        BlockPos matPos = findPicnicMatPos(maid);
        boolean onHomeMealSit = isOnHomeMealSit(maid);
        if (!PicnicLeftoverPolicy.shouldAttemptPlacement(matPos != null || onHomeMealSit, leftover.isEmpty())) {
            return leftover;
        }
        ItemStack remaining = leftover.copy();
        if (matPos != null) {
            remaining = placeAround(level, matPos, remaining);
        }
        // Fallback: bad sit association / multi-block center — search around the maid while seated.
        if (!remaining.isEmpty() && onHomeMealSit) {
            remaining = placeAround(level, maid.blockPosition(), remaining);
        }
        return remaining;
    }

    private static ItemStack placeAround(ServerLevel level, BlockPos center, ItemStack leftover) {
        List<BlockEntity> containers = collectContainers(level, center, PicnicLeftoverPolicy.SEARCH_RADIUS);
        containers.sort(Comparator.comparingDouble(be -> be.getBlockPos().distSqr(center)));
        ItemStack remaining = leftover.copy();
        for (BlockEntity blockEntity : containers) {
            if (remaining.isEmpty()) {
                break;
            }
            if (blockEntity instanceof TileEntityPicnicMat) {
                continue;
            }
            remaining = insertInto(blockEntity, remaining);
        }
        return remaining;
    }

    static boolean isOnHomeMealSit(EntityMaid maid) {
        Entity vehicle = maid.getVehicle();
        if (!(vehicle instanceof EntitySit sit)) {
            return false;
        }
        return Type.ON_HOME_MEAL.getTypeName().equals(sit.getJoyType());
    }

    static BlockPos findPicnicMatPos(EntityMaid maid) {
        if (!isOnHomeMealSit(maid)) {
            return null;
        }
        EntitySit sit = (EntitySit) maid.getVehicle();
        BlockPos associated = sit.getAssociatedBlockPos();
        BlockPos fromAssociated = resolvePicnicMatCenter(maid.level(), associated);
        if (fromAssociated != null) {
            return fromAssociated;
        }
        return findNearbyPicnicMatCenter(maid.level(), maid.blockPosition(), PicnicLeftoverPolicy.MAT_LOOKUP_RADIUS);
    }

    static BlockPos resolvePicnicMatCenter(Level level, BlockPos pos) {
        if (pos == null || level == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityPicnicMat mat) {
            BlockPos center = mat.getCenterPos();
            if (PicnicLeftoverPolicy.shouldUseTileCenter(center != null)) {
                return center;
            }
            return pos;
        }
        return null;
    }

    static BlockPos findNearbyPicnicMatCenter(Level level, BlockPos origin, int radius) {
        if (level == null || origin == null) {
            return null;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        int bestDist = Integer.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (!PicnicLeftoverPolicy.isWithinRadius(dx, dy, dz, radius)) {
                        continue;
                    }
                    cursor.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockEntity blockEntity = level.getBlockEntity(cursor);
                    if (!(blockEntity instanceof TileEntityPicnicMat mat)) {
                        continue;
                    }
                    BlockPos center = mat.getCenterPos() != null ? mat.getCenterPos() : cursor.immutable();
                    int dist = Math.max(Math.max(Math.abs(dx), Math.abs(dy)), Math.abs(dz));
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = center;
                    }
                }
            }
        }
        return best;
    }

    private static List<BlockEntity> collectContainers(ServerLevel level, BlockPos center, int radius) {
        List<BlockEntity> list = new ArrayList<>();
        BlockPos.betweenClosedStream(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius)
        ).forEach(pos -> {
            BlockPos immutable = pos.immutable();
            int dx = immutable.getX() - center.getX();
            int dy = immutable.getY() - center.getY();
            int dz = immutable.getZ() - center.getZ();
            if (!PicnicLeftoverPolicy.isWithinRadius(dx, dy, dz, radius)) {
                return;
            }
            BlockEntity blockEntity = level.getBlockEntity(immutable);
            if (blockEntity != null && isContainer(blockEntity)) {
                list.add(blockEntity);
            }
        });
        return list;
    }

    private static boolean isContainer(BlockEntity blockEntity) {
        if (blockEntity instanceof Container) {
            return true;
        }
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent();
    }

    private static ItemStack insertInto(BlockEntity blockEntity, ItemStack stack) {
        IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        if (handler != null) {
            ItemStack afterCap = ItemHandlerHelper.insertItemStacked(handler, stack, false);
            if (afterCap.getCount() < stack.getCount()) {
                return afterCap;
            }
            if (afterCap.isEmpty()) {
                return afterCap;
            }
        }
        if (blockEntity instanceof Container container) {
            return insertIntoContainer(container, stack);
        }
        return stack;
    }

    private static ItemStack insertIntoContainer(Container container, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (int slot = 0; slot < container.getContainerSize() && !remaining.isEmpty(); slot++) {
            ItemStack inSlot = container.getItem(slot);
            if (inSlot.isEmpty()) {
                if (!container.canPlaceItem(slot, remaining)) {
                    continue;
                }
                container.setItem(slot, remaining.copy());
                container.setChanged();
                return ItemStack.EMPTY;
            }
            if (!ItemStack.isSameItemSameTags(inSlot, remaining)) {
                continue;
            }
            int max = Math.min(container.getMaxStackSize(), inSlot.getMaxStackSize());
            int space = max - inSlot.getCount();
            if (space <= 0) {
                continue;
            }
            int move = Math.min(space, remaining.getCount());
            inSlot.grow(move);
            remaining.shrink(move);
            container.setChanged();
        }
        return remaining;
    }
}
