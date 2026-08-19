package com.touhoumaid.eatproperly.item.reward;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.food.MaidFoodReceive;
import com.touhoumaid.eatproperly.inventory.DualLunchboxContainer;
import com.touhoumaid.eatproperly.item.LunchboxItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Two-slot food carrier (not maid-bound). Supports both a dedicated GUI (main-hand right-click)
 * and inventory bundle-style insert/extract. Right-clicking an owned maid delivers both slots
 * into her backpack; see {@code CareItemEvents} for the completion / +2 favor rule.
 */
public class DualLunchboxItem extends Item {
    public static final String TAG_CONTAINER = "DualLunchboxContainer";
    public static final int SLOTS = 2;

    public DualLunchboxItem(Properties properties) {
        super(properties);
    }

    public static ItemStackHandler getContainer(ItemStack stack) {
        ItemStackHandler handler = new ItemStackHandler(SLOTS) {
            @Override
            public boolean isItemValid(int slot, ItemStack itemStack) {
                return LunchboxItem.isFood(itemStack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        if (stack.getItem() instanceof DualLunchboxItem) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(TAG_CONTAINER)) {
                handler.deserializeNBT(tag.getCompound(TAG_CONTAINER));
            }
        }
        return handler;
    }

    public static void setContainer(ItemStack stack, ItemStackHandler handler) {
        if (!(stack.getItem() instanceof DualLunchboxItem)) {
            return;
        }
        stack.getOrCreateTag().put(TAG_CONTAINER, handler.serializeNBT());
    }

    public static ItemStack getStoredFood(ItemStack stack, int slot) {
        return getContainer(stack).getStackInSlot(slot);
    }

    public static boolean isFull(ItemStack stack) {
        ItemStackHandler handler = getContainer(stack);
        for (int i = 0; i < SLOTS; i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static ItemStack insertFood(ItemStack stack, ItemStack food) {
        if (food.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStackHandler handler = getContainer(stack);
        ItemStack remaining = food;
        for (int i = 0; i < SLOTS && !remaining.isEmpty(); i++) {
            remaining = handler.insertItem(i, remaining, false);
        }
        setContainer(stack, handler);
        return remaining;
    }

    public static boolean tryInsertOne(ItemStack stack, ItemStack food) {
        if (food.isEmpty() || !LunchboxItem.isFood(food) || isFull(stack)) {
            return false;
        }
        ItemStack one = food.split(1);
        ItemStack leftover = insertFood(stack, one);
        if (!leftover.isEmpty()) {
            food.grow(leftover.getCount());
            return false;
        }
        return true;
    }

    public static ItemStack extractFood(ItemStack stack) {
        ItemStackHandler handler = getContainer(stack);
        for (int i = SLOTS - 1; i >= 0; i--) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                ItemStack out = handler.extractItem(i, 1, false);
                setContainer(stack, handler);
                return out;
            }
        }
        return ItemStack.EMPTY;
    }

    public static boolean deliverAllTo(ItemStack stack, EntityMaid maid) {
        ItemStackHandler handler = getContainer(stack);
        List<ItemStack> foods = new java.util.ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            ItemStack food = handler.getStackInSlot(i);
            if (!food.isEmpty()) {
                foods.add(food.copy());
            }
        }
        if (!MaidFoodReceive.canFullyReceiveAll(maid, foods)) {
            return false;
        }
        boolean movedAny = false;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack food = handler.getStackInSlot(i);
            if (food.isEmpty()) {
                continue;
            }
            if (!MaidFoodReceive.tryFullyReceive(maid, food)) {
                continue;
            }
            handler.extractItem(i, food.getCount(), false);
            movedAny = true;
        }
        setContainer(stack, handler);
        return movedAny;
    }

    public static boolean deliverAllTo(ItemStack stack, IItemHandler maidInv) {
        ItemStackHandler handler = getContainer(stack);
        List<ItemStack> foods = new java.util.ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            ItemStack food = handler.getStackInSlot(i);
            if (!food.isEmpty()) {
                foods.add(food.copy());
            }
        }
        if (!com.touhoumaid.eatproperly.item.LunchboxInvInsert.canFullyInsertAll(maidInv, foods)) {
            return false;
        }
        boolean movedAny = false;
        for (int i = 0; i < SLOTS; i++) {
            ItemStack food = handler.getStackInSlot(i);
            if (food.isEmpty()) {
                continue;
            }
            ItemStack leftover = ItemHandlerHelper.insertItemStacked(maidInv, food.copy(), false);
            if (!com.touhoumaid.eatproperly.item.LunchboxDeliveryPolicy.wasFullyInserted(
                    food.getCount(), leftover.getCount())) {
                continue;
            }
            handler.extractItem(i, food.getCount(), false);
            movedAny = true;
        }
        setContainer(stack, handler);
        return movedAny;
    }

    public static boolean isMaidInvTooFullFor(ItemStack stack, EntityMaid maid) {
        ItemStackHandler handler = getContainer(stack);
        List<ItemStack> foods = new java.util.ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            ItemStack food = handler.getStackInSlot(i);
            if (!food.isEmpty()) {
                foods.add(food.copy());
            }
        }
        return !foods.isEmpty() && !MaidFoodReceive.canFullyReceiveAll(maid, foods);
    }

    /**
     * {@code true} when stored foods cannot all fully fit into {@code maidInv}
     * (cumulative simulate — use available backpack, not full 36-slot maidInv).
     */
    public static boolean isMaidInvTooFullFor(ItemStack stack, IItemHandler maidInv) {
        ItemStackHandler handler = getContainer(stack);
        List<ItemStack> foods = new java.util.ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; i++) {
            ItemStack food = handler.getStackInSlot(i);
            if (!food.isEmpty()) {
                foods.add(food.copy());
            }
        }
        return !foods.isEmpty()
                && !com.touhoumaid.eatproperly.item.LunchboxInvInsert.canFullyInsertAll(maidInv, foods);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        ItemStack off = player.getOffhandItem();
        if (!isFull(stack) && LunchboxItem.isFood(off) && tryInsertOne(stack, off)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            openGui(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    public static void openGui(ServerPlayer player, ItemStack dualLunchbox) {
        NetworkHooks.openScreen(player, new DualLunchboxContainer.Provider(dualLunchbox),
                buf -> buf.writeItem(dualLunchbox.copy()));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack dual, ItemStack other, Slot slot,
                                            ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || other.isEmpty() || !LunchboxItem.isFood(other)) {
            return false;
        }
        if (isFull(dual)) {
            return false;
        }
        ItemStack one = other.split(1);
        insertFood(dual, one);
        return true;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack dual, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        ItemStack inSlot = slot.getItem();
        if (!isFull(dual)) {
            if (!LunchboxItem.isFood(inSlot)) {
                return false;
            }
            ItemStack one = inSlot.split(1);
            insertFood(dual, one);
            if (inSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            return true;
        }
        if (!inSlot.isEmpty()) {
            return false;
        }
        ItemStack food = extractFood(dual);
        if (food.isEmpty()) {
            return false;
        }
        slot.setByPlayer(food);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag rewardTag = stack.getTagElement(RewardTooltipText.REWARD_TAG);
        String sourceMaidName = rewardTag != null ? rewardTag.getString(RewardTooltipText.SOURCE_MAID_NAME) : "";
        tooltip.add(Component.translatable(RewardTooltipText.loreKey(getDescriptionId(), 0),
                        RewardTooltipText.displaySourceName(sourceMaidName))
                .withStyle(ChatFormatting.GRAY));

        ItemStack a = getStoredFood(stack, 0);
        ItemStack b = getStoredFood(stack, 1);
        if (a.isEmpty() && b.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.touhou_maid_eat_properly.dual_lunchbox.empty"));
        } else {
            Component nameA = a.isEmpty()
                    ? Component.translatable("tooltip.touhou_maid_eat_properly.dual_lunchbox.slot_empty")
                    : a.getHoverName();
            Component nameB = b.isEmpty()
                    ? Component.translatable("tooltip.touhou_maid_eat_properly.dual_lunchbox.slot_empty")
                    : b.getHoverName();
            tooltip.add(Component.translatable("tooltip.touhou_maid_eat_properly.dual_lunchbox.contains", nameA, nameB));
        }
        tooltip.add(Component.translatable("tooltip.touhou_maid_eat_properly.dual_lunchbox.hint"));
    }
}
