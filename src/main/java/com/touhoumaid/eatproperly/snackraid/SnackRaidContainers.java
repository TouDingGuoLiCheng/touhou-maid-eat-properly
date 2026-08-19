package com.touhoumaid.eatproperly.snackraid;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class SnackRaidContainers {
    private SnackRaidContainers() {
    }

    public static boolean hasRaidFood(BlockEntity blockEntity, EntityMaid maid) {
        return findFirstRaidFoodSlot(blockEntity, maid) >= 0;
    }

    public static ItemStack extractOneRaidFood(BlockEntity blockEntity, EntityMaid maid) {
        int slot = findFirstRaidFoodSlot(blockEntity, maid);
        if (slot < 0) {
            return ItemStack.EMPTY;
        }
        return extractFromSlot(blockEntity, maid, slot);
    }

    public static SnackRaidTarget findNearestTarget(ServerLevel level, EntityMaid maid, int radius, int verticalRange,
                                                    int maxContainers, BlockPos cachedChest) {
        return findNearestTarget(level, maid, radius, verticalRange, maxContainers, cachedChest, Set.of());
    }

    /**
     * @param excluded containers the maid must not sneak from (Food Permit marks)
     */
    public static SnackRaidTarget findNearestTarget(ServerLevel level, EntityMaid maid, int radius, int verticalRange,
                                                    int maxContainers, BlockPos cachedChest,
                                                    Set<BlockPos> excluded) {
        if (cachedChest != null && !cachedChest.equals(BlockPos.ZERO) && !excluded.contains(cachedChest)) {
            SnackRaidTarget cached = targetAt(level, maid, cachedChest);
            if (cached != null) {
                return cached;
            }
        }

        List<BlockEntity> containers = collectContainers(level, maid, radius, verticalRange);
        containers.sort(Comparator.comparingDouble(be -> be.getBlockPos().distSqr(maid.blockPosition())));

        int inspected = 0;
        for (BlockEntity blockEntity : containers) {
            if (inspected >= maxContainers) {
                break;
            }
            if (!isRaidContainer(blockEntity) || excluded.contains(blockEntity.getBlockPos())) {
                continue;
            }
            inspected++;
            if (!hasRaidFood(blockEntity, maid)) {
                continue;
            }
            SnackRaidTarget target = targetAt(level, maid, blockEntity.getBlockPos());
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    private static SnackRaidTarget targetAt(ServerLevel level, EntityMaid maid, BlockPos containerPos) {
        BlockEntity blockEntity = level.getBlockEntity(containerPos);
        if (blockEntity == null || !isRaidContainer(blockEntity) || !hasRaidFood(blockEntity, maid)) {
            return null;
        }
        BlockPos stand = SnackRaidPathing.pickStandPos(level, maid, containerPos);
        if (stand == null) {
            return null;
        }
        return new SnackRaidTarget(containerPos, stand);
    }

    private static List<BlockEntity> collectContainers(ServerLevel level, EntityMaid maid, int radius, int verticalRange) {
        BlockPos center = maid.blockPosition();
        BlockPos min = center.offset(-radius, -verticalRange, -radius);
        BlockPos max = center.offset(radius, verticalRange, radius);
        List<BlockEntity> list = new ArrayList<>();
        BlockPos.betweenClosedStream(min, max).forEach(pos -> {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null && isRaidContainer(blockEntity)) {
                list.add(blockEntity);
            }
        });
        return list;
    }

    /** Same container recognition used by snack raid and Food Permit marking. */
    public static boolean isMarkableContainer(BlockEntity blockEntity) {
        return isRaidContainer(blockEntity);
    }

    static boolean isRaidContainer(BlockEntity blockEntity) {
        if (blockEntity instanceof Container) {
            return true;
        }
        return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).isPresent();
    }

    private static int findFirstRaidFoodSlot(BlockEntity blockEntity, EntityMaid maid) {
        IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (SnackRaidFood.isRaidFood(handler.getStackInSlot(i), maid)) {
                    return i;
                }
            }
        }
        if (blockEntity instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (SnackRaidFood.isRaidFood(container.getItem(i), maid)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static ItemStack extractFromSlot(BlockEntity blockEntity, EntityMaid maid, int slot) {
        IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        if (handler != null && slot < handler.getSlots()) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (SnackRaidFood.isRaidFood(stack, maid)) {
                ItemStack extracted = handler.extractItem(slot, 1, false);
                if (!extracted.isEmpty()) {
                    markChanged(blockEntity);
                    return extracted;
                }
            }
        }
        if (blockEntity instanceof Container container && slot < container.getContainerSize()) {
            ItemStack stack = container.getItem(slot);
            if (SnackRaidFood.isRaidFood(stack, maid)) {
                ItemStack taken = stack.split(1);
                if (!taken.isEmpty()) {
                    markChanged(blockEntity);
                    return taken;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static void markChanged(BlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity instanceof RandomizableContainerBlockEntity randomizable) {
            randomizable.setChanged();
        }
        if (blockEntity.getLevel() != null) {
            BlockPos pos = blockEntity.getBlockPos();
            blockEntity.getLevel().sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }

    /** Visible for tests: whether a world-space point lies in the maid-centered search box. */
    public static boolean isInsideSearchBox(BlockPos maidPos, BlockPos pos, int radius, int verticalRange) {
        return SnackRaidSearchBounds.contains(
                maidPos.getX(), maidPos.getY(), maidPos.getZ(),
                pos.getX(), pos.getY(), pos.getZ(),
                radius, verticalRange);
    }

    static AABB searchBox(BlockPos center, int radius, int verticalRange) {
        return new AABB(
                center.getX() - radius, center.getY() - verticalRange, center.getZ() - radius,
                center.getX() + radius + 1, center.getY() + verticalRange + 1, center.getZ() + radius + 1);
    }
}
