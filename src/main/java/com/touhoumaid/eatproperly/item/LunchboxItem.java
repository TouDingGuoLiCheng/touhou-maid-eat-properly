package com.touhoumaid.eatproperly.item;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.food.MaidFoodReceive;
import com.touhoumaid.eatproperly.inventory.LunchboxContainer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Single-slot food carrier. Right-click opens GUI; right-click a maid delivers the food into her backpack.
 * Inventory right-click (bundle-style) inserts / extracts food without opening the GUI.
 */
public class LunchboxItem extends Item {
    public static final String TAG_CONTAINER = "LunchboxContainer";

    public LunchboxItem(Properties properties) {
        super(properties);
    }

    public static ItemStackHandler getContainer(ItemStack stack) {
        ItemStackHandler handler = new ItemStackHandler(1) {
            @Override
            public boolean isItemValid(int slot, ItemStack itemStack) {
                return isFood(itemStack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        if (stack.getItem() instanceof LunchboxItem) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains(TAG_CONTAINER)) {
                handler.deserializeNBT(tag.getCompound(TAG_CONTAINER));
            }
        }
        return handler;
    }

    public static void setContainer(ItemStack stack, ItemStackHandler handler) {
        if (!(stack.getItem() instanceof LunchboxItem)) {
            return;
        }
        stack.getOrCreateTag().put(TAG_CONTAINER, handler.serializeNBT());
    }

    public static ItemStack getStoredFood(ItemStack lunchbox) {
        return getContainer(lunchbox).getStackInSlot(0);
    }

    public static boolean isFood(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        FoodProperties food = stack.getFoodProperties(null);
        return food != null;
    }

    /**
     * Extracts one food item from the lunchbox. Returns empty if none.
     */
    public static ItemStack extractFood(ItemStack lunchbox) {
        ItemStackHandler handler = getContainer(lunchbox);
        ItemStack food = handler.extractItem(0, 1, false);
        setContainer(lunchbox, handler);
        return food;
    }

    /**
     * Tries to insert leftover food back into the lunchbox.
     */
    public static ItemStack insertFood(ItemStack lunchbox, ItemStack food) {
        if (food.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStackHandler handler = getContainer(lunchbox);
        ItemStack leftover = handler.insertItem(0, food, false);
        setContainer(lunchbox, handler);
        return leftover;
    }

    /**
     * Insert one item from {@code food} into an empty lunchbox. Returns true if inserted.
     */
    public static boolean tryInsertOne(ItemStack lunchbox, ItemStack food) {
        if (food.isEmpty() || !isFood(food) || !getStoredFood(lunchbox).isEmpty()) {
            return false;
        }
        ItemStack one = food.split(1);
        ItemStack leftover = insertFood(lunchbox, one);
        if (!leftover.isEmpty()) {
            food.grow(leftover.getCount());
            return false;
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        // Convenience: offhand food + empty lunchbox → insert without opening GUI.
        ItemStack off = player.getOffhandItem();
        if (getStoredFood(stack).isEmpty() && isFood(off) && tryInsertOne(stack, off)) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            openGui(serverPlayer, stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Cursor food right-clicked onto the lunchbox in an inventory slot.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack lunchbox, ItemStack other, Slot slot,
                                            ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || other.isEmpty() || !isFood(other)) {
            return false;
        }
        if (!getStoredFood(lunchbox).isEmpty()) {
            return false;
        }
        ItemStack one = other.split(1);
        insertFood(lunchbox, one);
        return true;
    }

    /**
     * Lunchbox on the cursor right-clicked onto a food stack in a slot.
     */
    @Override
    public boolean overrideStackedOnOther(ItemStack lunchbox, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        ItemStack inSlot = slot.getItem();
        if (getStoredFood(lunchbox).isEmpty()) {
            if (!isFood(inSlot)) {
                return false;
            }
            ItemStack one = inSlot.split(1);
            insertFood(lunchbox, one);
            if (inSlot.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            return true;
        }
        // Right-click filled lunchbox onto empty/compatible slot → extract
        if (!inSlot.isEmpty()) {
            return false;
        }
        ItemStack food = extractFood(lunchbox);
        if (food.isEmpty()) {
            return false;
        }
        slot.setByPlayer(food);
        return true;
    }

    public static void openGui(ServerPlayer player, ItemStack lunchbox) {
        NetworkHooks.openScreen(player, new LunchboxContainer.Provider(lunchbox), buf -> buf.writeItem(lunchbox.copy()));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemStack food = getStoredFood(stack);
        if (food.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.touhou_maid_eat_properly.lunchbox.empty"));
        } else {
            tooltip.add(Component.translatable("tooltip.touhou_maid_eat_properly.lunchbox.contains", food.getHoverName()));
        }
        tooltip.add(Component.translatable("tooltip.touhou_maid_eat_properly.lunchbox.hint"));
    }

    /**
     * Deliver stored food into the maid's visible backpack, or the first empty hand when it is full.
     * Succeeds only when the entire stack fits; otherwise leaves the lunchbox unchanged.
     */
    public static boolean deliverTo(ItemStack lunchbox, EntityMaid maid) {
        ItemStack food = getStoredFood(lunchbox);
        if (food.isEmpty() || !MaidFoodReceive.canFullyReceive(maid, food)) {
            return false;
        }
        if (!MaidFoodReceive.tryFullyReceive(maid, food)) {
            return false;
        }
        ItemStackHandler handler = getContainer(lunchbox);
        handler.extractItem(0, food.getCount(), false);
        setContainer(lunchbox, handler);
        return true;
    }

    /**
     * Deliver stored food into the maid's available backpack (visible slots).
     * Succeeds only when the entire stack fits; otherwise leaves the lunchbox unchanged.
     */
    public static boolean deliverTo(ItemStack lunchbox, IItemHandler maidInv) {
        ItemStack food = getStoredFood(lunchbox);
        if (food.isEmpty()) {
            return false;
        }
        ItemStack simulated = ItemHandlerHelper.insertItemStacked(maidInv, food.copy(), true);
        if (!LunchboxDeliveryPolicy.canFullyInsert(food.getCount(), simulated.getCount())) {
            return false;
        }
        ItemStack leftover = ItemHandlerHelper.insertItemStacked(maidInv, food.copy(), false);
        if (!LunchboxDeliveryPolicy.wasFullyInserted(food.getCount(), leftover.getCount())) {
            return false;
        }
        ItemStackHandler handler = getContainer(lunchbox);
        handler.extractItem(0, food.getCount(), false);
        setContainer(lunchbox, handler);
        return true;
    }
}
