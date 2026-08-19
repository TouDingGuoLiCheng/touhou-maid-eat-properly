package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.snackraid.SnackRaidPathing;
import com.touhoumaid.eatproperly.snackraid.SnackRaidTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import java.util.Comparator;
import java.util.List;

/**
 * Marked-container lookup for Food Permit. Search radius filtering is handled by the caller.
 */
public final class FoodPermitTargets {
    private FoodPermitTargets() {
    }

    public static SnackRaidTarget findNearestMarkedTarget(ServerLevel level, EntityMaid maid, List<FoodPermitMark> marks,
                                                          int radius, int verticalRange) {
        return marks.stream()
                .filter(mark -> FoodPermitData.matchesDimension(mark, level))
                .map(FoodPermitData::toPos)
                .filter(pos -> com.touhoumaid.eatproperly.snackraid.SnackRaidContainers
                        .isInsideSearchBox(maid.blockPosition(), pos, radius, verticalRange))
                .distinct()
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(maid.blockPosition())))
                .map(pos -> targetAt(level, maid, pos))
                .filter(target -> target != null)
                .findFirst()
                .orElse(null);
    }

    private static SnackRaidTarget targetAt(ServerLevel level, EntityMaid maid, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || !hasEffectivePoolFood(blockEntity, maid)) {
            return null;
        }
        BlockPos stand = SnackRaidPathing.pickStandPos(level, maid, pos);
        if (stand == null) {
            return null;
        }
        return new SnackRaidTarget(pos, stand);
    }

    public static boolean hasEffectivePoolFood(BlockEntity blockEntity, EntityMaid maid) {
        IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                if (isEffectivePoolFood(handler.getStackInSlot(i), maid)) {
                    return true;
                }
            }
        }
        if (blockEntity instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                if (isEffectivePoolFood(container.getItem(i), maid)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isEffectivePoolFood(ItemStack stack, EntityMaid maid) {
        return FoodPool.isInEffectivePool(stack, OrderBookData.findAllEquipped(maid));
    }
}
