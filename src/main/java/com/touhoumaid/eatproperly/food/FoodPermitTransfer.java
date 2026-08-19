package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.util.HandUtils;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Food Permit container extraction into the maid's available backpack and hands.
 * When the maid is full, uses a call-stack buffer swap; whole-stack take clears a chest food
 * slot when the dump item cannot fit without it. Failed swaps roll back — items never vanish.
 * <p>Only {@link EntityMaid#getAvailableBackpackInv()} plus hands are used — not the full
 * hidden {@code getMaidInv()} grid, which can absorb items outside the visible GUI.
 */
public final class FoodPermitTransfer {
    private FoodPermitTransfer() {
    }

    private static IItemHandler availableBackpack(EntityMaid maid) {
        return maid.getAvailableBackpackInv();
    }

    public static int extractToBackpack(BlockEntity blockEntity, EntityMaid maid, int maxTake) {
        ChestAccess chest = ChestAccess.open(blockEntity);
        if (chest == null) {
            return 0;
        }
        int moved = 0;
        while (moved < maxTake) {
            int step = moveStep(chest, maid, maxTake - moved);
            if (step <= 0) {
                break;
            }
            moved += step;
            markChanged(blockEntity);
        }
        return moved;
    }

    public static int extractToItemHandler(BlockEntity blockEntity, IItemHandler target, EntityMaid maid, int maxTake) {
        ChestAccess chest = ChestAccess.open(blockEntity);
        if (chest == null) {
            return 0;
        }
        int moved = 0;
        while (moved < maxTake) {
            int foodSlot = chest.findFirstEffectivePoolFoodSlot(maid);
            if (foodSlot < 0) {
                break;
            }
            ItemStack food = chest.getStack(foodSlot);
            if (food.isEmpty()) {
                break;
            }
            int take = Math.min(food.getCount(), maxTake - moved);
            ItemStack toMove = food.copyWithCount(take);
            if (!ItemHandlerHelper.insertItemStacked(target, toMove.copy(), true).isEmpty()) {
                break;
            }
            ItemStack extracted = chest.extract(foodSlot, take);
            if (extracted.isEmpty()) {
                break;
            }
            ItemStack leftover = ItemHandlerHelper.insertItemStacked(target, extracted, false);
            if (!leftover.isEmpty()) {
                chest.insertAnywhere(leftover);
                break;
            }
            moved += extracted.getCount();
            markChanged(blockEntity);
        }
        return moved;
    }

    public static int moveFromBackpackToItemHandler(IItemHandler target, EntityMaid maid, int maxTake) {
        IItemHandler backpack = availableBackpack(maid);
        int moved = 0;
        for (int slot = 0; slot < backpack.getSlots() && moved < maxTake; slot++) {
            while (moved < maxTake) {
                ItemStack stack = backpack.getStackInSlot(slot);
                if (stack.isEmpty() || !FoodPermitTargets.isEffectivePoolFood(stack, maid)) {
                    break;
                }
                ItemStack one = stack.copyWithCount(1);
                ItemStack leftover = ItemHandlerHelper.insertItemStacked(target, one, false);
                if (!leftover.isEmpty()) {
                    return moved;
                }
                backpack.extractItem(slot, 1, false);
                moved++;
            }
        }
        return moved;
    }

    private static int moveStep(ChestAccess chest, EntityMaid maid, int remaining) {
        int foodSlot = chest.findFirstEffectivePoolFoodSlot(maid);
        if (foodSlot < 0) {
            return 0;
        }
        int direct = tryDirectInsert(chest, foodSlot, maid, remaining);
        if (direct > 0) {
            return direct;
        }
        return trySwap(chest, foodSlot, maid, remaining);
    }

    private static int tryDirectInsert(ChestAccess chest, int foodSlot, EntityMaid maid, int remaining) {
        ItemStack food = chest.getStack(foodSlot);
        if (food.isEmpty() || remaining <= 0) {
            return 0;
        }
        int take = Math.min(food.getCount(), remaining);
        ItemStack toMove = food.copyWithCount(take);
        IItemHandler backpack = availableBackpack(maid);

        ItemStack simLeftover = ItemHandlerHelper.insertItemStacked(backpack, toMove.copy(), true);
        if (simLeftover.getCount() < toMove.getCount()) {
            int accepted = toMove.getCount() - simLeftover.getCount();
            ItemStack extracted = chest.extract(foodSlot, accepted);
            if (extracted.isEmpty()) {
                return 0;
            }
            ItemStack leftover = ItemHandlerHelper.insertItemStacked(backpack, extracted, false);
            if (!leftover.isEmpty()) {
                chest.insertAnywhere(leftover);
                return accepted - leftover.getCount();
            }
            return extracted.getCount();
        }

        for (InteractionHand hand : HandUtils.NATIVE_HANDS) {
            if (!maid.getItemInHand(hand).isEmpty()) {
                continue;
            }
            int handTake = Math.min(take, toMove.getMaxStackSize());
            ItemStack extracted = chest.extract(foodSlot, handTake);
            if (extracted.isEmpty()) {
                return 0;
            }
            maid.setItemInHand(hand, extracted);
            return extracted.getCount();
        }
        return 0;
    }

    private static int trySwap(ChestAccess chest, int foodSlot, EntityMaid maid, int remaining) {
        ItemStack foodStack = chest.getStack(foodSlot);
        if (foodStack.isEmpty() || remaining <= 0) {
            return 0;
        }
        List<DumpCandidate> dumps = listDumpCandidates(maid);
        dumps.sort(Comparator.comparingInt(c -> c.tier().ordinal()));

        for (DumpCandidate dump : dumps) {
            ItemStack dumpStack = dump.stack();
            if (dumpStack.isEmpty()) {
                continue;
            }
            boolean dumpFits = chest.canStash(dumpStack.copy(), foodSlot);
            boolean needWhole = FoodPermitSwapPolicy.needWholeStackToFitDump(dumpFits);
            int take = FoodPermitSwapPolicy.decideTakeCount(foodStack.getCount(), remaining, needWhole);
            if (take <= 0) {
                continue;
            }

            TransferSnapshot snapshot = TransferSnapshot.capture(chest, maid);
            ItemStack buffer = chest.extract(foodSlot, take);
            if (buffer.isEmpty()) {
                snapshot.restore(chest, maid);
                continue;
            }
            ItemStack removedDump = clearMaidSlot(maid, dump);
            if (removedDump.isEmpty()) {
                snapshot.restore(chest, maid);
                continue;
            }
            ItemStack dumpLeftover = chest.stash(removedDump, needWhole ? -1 : foodSlot);
            if (!dumpLeftover.isEmpty()) {
                snapshot.restore(chest, maid);
                continue;
            }
            if (!placeInMaidSlot(maid, dump, buffer)) {
                snapshot.restore(chest, maid);
                continue;
            }
            return buffer.getCount();
        }
        return 0;
    }

    private static List<DumpCandidate> listDumpCandidates(EntityMaid maid) {
        List<DumpCandidate> out = new ArrayList<>();
        IItemHandler backpack = availableBackpack(maid);
        for (int slot = 0; slot < backpack.getSlots(); slot++) {
            ItemStack stack = backpack.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                out.add(new DumpCandidate(MaidSlot.backpack(slot), stack, dumpTier(stack, maid)));
            }
        }
        addHandDump(out, maid, InteractionHand.MAIN_HAND);
        addHandDump(out, maid, InteractionHand.OFF_HAND);
        return out;
    }

    private static void addHandDump(List<DumpCandidate> out, EntityMaid maid, InteractionHand hand) {
        ItemStack stack = maid.getItemInHand(hand);
        if (!stack.isEmpty()) {
            out.add(new DumpCandidate(MaidSlot.hand(hand), stack, dumpTier(stack, maid)));
        }
    }

    private static FoodPermitSwapPolicy.DumpTier dumpTier(ItemStack stack, EntityMaid maid) {
        return FoodPermitSwapPolicy.classifyDump(
                stack.isEdible(),
                FoodPermitTargets.isEffectivePoolFood(stack, maid),
                isTool(stack));
    }

    private static boolean isTool(ItemStack stack) {
        return stack.getItem() instanceof DiggerItem
                || stack.getItem() instanceof SwordItem
                || stack.getItem() instanceof BowItem
                || stack.getItem() instanceof CrossbowItem
                || stack.getItem() instanceof TridentItem
                || stack.getItem() instanceof ShieldItem
                || stack.getItem() instanceof ShearsItem;
    }

    private static ItemStack clearMaidSlot(EntityMaid maid, DumpCandidate dump) {
        return switch (dump.slot().kind()) {
            case BACKPACK -> {
                IItemHandler backpack = availableBackpack(maid);
                int slot = dump.slot().index();
                ItemStack stack = backpack.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    backpack.extractItem(slot, stack.getCount(), false);
                }
                yield stack;
            }
            case HAND -> {
                InteractionHand hand = dump.slot().hand();
                ItemStack stack = maid.getItemInHand(hand);
                maid.setItemInHand(hand, ItemStack.EMPTY);
                yield stack;
            }
        };
    }

    private static boolean placeInMaidSlot(EntityMaid maid, DumpCandidate dump, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return switch (dump.slot().kind()) {
            case BACKPACK -> {
                IItemHandler backpack = availableBackpack(maid);
                ItemStack leftover = backpack.insertItem(dump.slot().index(), stack.copy(), false);
                yield leftover.isEmpty();
            }
            case HAND -> {
                maid.setItemInHand(dump.slot().hand(), stack.copy());
                yield true;
            }
        };
    }

    private static void markChanged(BlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity instanceof RandomizableContainerBlockEntity randomizable) {
            randomizable.setChanged();
        }
        if (blockEntity.getLevel() != null) {
            blockEntity.getLevel().sendBlockUpdated(
                    blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }

    private enum MaidSlotKind {
        BACKPACK,
        HAND
    }

    private record MaidSlot(MaidSlotKind kind, int index, InteractionHand hand) {
        static MaidSlot backpack(int index) {
            return new MaidSlot(MaidSlotKind.BACKPACK, index, InteractionHand.MAIN_HAND);
        }

        static MaidSlot hand(InteractionHand hand) {
            return new MaidSlot(MaidSlotKind.HAND, -1, hand);
        }
    }

    private record DumpCandidate(MaidSlot slot, ItemStack stack, FoodPermitSwapPolicy.DumpTier tier) {
    }

    private record TransferSnapshot(List<ItemStack> chestSlots, List<ItemStack> maidBackpack,
                                    ItemStack mainHand, ItemStack offHand) {
        static TransferSnapshot capture(ChestAccess chest, EntityMaid maid) {
            IItemHandler backpack = availableBackpack(maid);
            List<ItemStack> maidSlots = new ArrayList<>();
            for (int i = 0; i < backpack.getSlots(); i++) {
                maidSlots.add(backpack.getStackInSlot(i).copy());
            }
            return new TransferSnapshot(
                    chest.copyAllStacks(),
                    maidSlots,
                    maid.getMainHandItem().copy(),
                    maid.getOffhandItem().copy());
        }

        void restore(ChestAccess chest, EntityMaid maid) {
            chest.restoreAllStacks(chestSlots);
            restoreBackpack(maid, maidBackpack);
            maid.setItemInHand(InteractionHand.MAIN_HAND, mainHand.copy());
            maid.setItemInHand(InteractionHand.OFF_HAND, offHand.copy());
        }

        private static void restoreBackpack(EntityMaid maid, List<ItemStack> slots) {
            IItemHandler backpack = availableBackpack(maid);
            if (backpack instanceof IItemHandlerModifiable modifiable) {
                for (int i = 0; i < slots.size() && i < modifiable.getSlots(); i++) {
                    modifiable.setStackInSlot(i, slots.get(i).copy());
                }
                return;
            }
            for (int i = 0; i < backpack.getSlots(); i++) {
                ItemStack current = backpack.getStackInSlot(i);
                if (!current.isEmpty()) {
                    backpack.extractItem(i, current.getCount(), false);
                }
                if (i < slots.size() && !slots.get(i).isEmpty()) {
                    backpack.insertItem(i, slots.get(i).copy(), false);
                }
            }
        }
    }

    /**
     * Unified read/write view of a marked chest block entity.
     */
    private static final class ChestAccess {
        private final BlockEntity blockEntity;
        private final IItemHandlerModifiable handler;
        private final Container container;
        private final int size;

        private ChestAccess(BlockEntity blockEntity, IItemHandlerModifiable handler, Container container, int size) {
            this.blockEntity = blockEntity;
            this.handler = handler;
            this.container = container;
            this.size = size;
        }

        static ChestAccess open(BlockEntity blockEntity) {
            IItemHandler raw = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
            if (raw instanceof IItemHandlerModifiable modifiable) {
                return new ChestAccess(blockEntity, modifiable, null, modifiable.getSlots());
            }
            if (blockEntity instanceof Container chest) {
                return new ChestAccess(blockEntity, null, chest, chest.getContainerSize());
            }
            return null;
        }

        int findFirstEffectivePoolFoodSlot(EntityMaid maid) {
            for (int slot = 0; slot < size; slot++) {
                if (FoodPermitTargets.isEffectivePoolFood(getStack(slot), maid)) {
                    return slot;
                }
            }
            return -1;
        }

        ItemStack getStack(int slot) {
            if (handler != null) {
                return handler.getStackInSlot(slot);
            }
            return container.getItem(slot);
        }

        ItemStack extract(int slot, int count) {
            if (count <= 0) {
                return ItemStack.EMPTY;
            }
            if (handler != null) {
                return handler.extractItem(slot, count, false);
            }
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            return stack.split(count);
        }

        ItemStack insertAnywhere(ItemStack stack) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack remaining = stack.copy();
            if (handler != null) {
                for (int slot = 0; slot < size && !remaining.isEmpty(); slot++) {
                    remaining = handler.insertItem(slot, remaining, false);
                }
                return remaining;
            }
            for (int slot = 0; slot < size && !remaining.isEmpty(); slot++) {
                remaining = insertIntoContainerSlot(slot, remaining);
            }
            return remaining;
        }

        boolean canStash(ItemStack stack, int skipSlot) {
            return stashSimulate(stack.copy(), skipSlot).isEmpty();
        }

        ItemStack stash(ItemStack stack, int skipSlot) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (skipSlot < 0) {
                // Whole-stack path: food slot was cleared; any slot including it is fine.
                return insertAnywhere(stack.copy());
            }
            if (!stashSimulate(stack.copy(), skipSlot).isEmpty()) {
                return stack;
            }
            return applyStash(stack.copy(), skipSlot);
        }

        private ItemStack applyStash(ItemStack stack, int skipSlot) {
            ItemStack remaining = stack;
            if (handler != null) {
                for (int slot = 0; slot < size && !remaining.isEmpty(); slot++) {
                    if (slot == skipSlot) {
                        continue;
                    }
                    remaining = handler.insertItem(slot, remaining, false);
                }
                return remaining;
            }
            for (int slot = 0; slot < size && !remaining.isEmpty(); slot++) {
                if (slot == skipSlot) {
                    continue;
                }
                remaining = insertIntoContainerSlot(slot, remaining);
            }
            return remaining;
        }

        private ItemStack stashSimulate(ItemStack stack, int skipSlot) {
            ItemStack remaining = stack.copy();
            if (handler != null) {
                for (int slot = 0; slot < size && !remaining.isEmpty(); slot++) {
                    if (slot == skipSlot) {
                        continue;
                    }
                    remaining = handler.insertItem(slot, remaining, true);
                }
                return remaining;
            }
            ItemStack[] sim = copyAllStacks().toArray(ItemStack[]::new);
            for (int slot = 0; slot < size && !remaining.isEmpty(); slot++) {
                if (slot == skipSlot) {
                    continue;
                }
                remaining = insertIntoSimSlot(sim, slot, remaining);
            }
            return remaining;
        }

        private ItemStack insertIntoContainerSlot(int slot, ItemStack stack) {
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty()) {
                container.setItem(slot, stack.copy());
                return ItemStack.EMPTY;
            }
            if (!ItemStack.isSameItemSameTags(existing, stack)) {
                return stack;
            }
            int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (move <= 0) {
                return stack;
            }
            existing.grow(move);
            stack.shrink(move);
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        }

        private static ItemStack insertIntoSimSlot(ItemStack[] sim, int slot, ItemStack stack) {
            ItemStack existing = sim[slot];
            if (existing.isEmpty()) {
                sim[slot] = stack.copy();
                return ItemStack.EMPTY;
            }
            if (!ItemStack.isSameItemSameTags(existing, stack)) {
                return stack;
            }
            int move = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
            if (move <= 0) {
                return stack;
            }
            existing.grow(move);
            stack.shrink(move);
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        }

        List<ItemStack> copyAllStacks() {
            List<ItemStack> stacks = new ArrayList<>(size);
            for (int slot = 0; slot < size; slot++) {
                stacks.add(getStack(slot).copy());
            }
            return stacks;
        }

        void restoreAllStacks(List<ItemStack> stacks) {
            for (int slot = 0; slot < size && slot < stacks.size(); slot++) {
                ItemStack stack = stacks.get(slot).copy();
                if (handler != null) {
                    handler.setStackInSlot(slot, stack);
                } else {
                    container.setItem(slot, stack);
                }
            }
        }
    }
}
