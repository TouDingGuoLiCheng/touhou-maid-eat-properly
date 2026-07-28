package com.touhoumaid.eatproperly.inventory;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.item.LunchboxItem;
import com.touhoumaid.eatproperly.item.reward.DualLunchboxItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Two-slot dual lunchbox menu (layout aligned with {@link LunchboxContainer}).
 */
public class DualLunchboxContainer extends AbstractContainerMenu {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EatProperlyMod.MOD_ID);

    public static final RegistryObject<MenuType<DualLunchboxContainer>> TYPE = MENUS.register(
            "dual_lunchbox",
            () -> IForgeMenuType.create((id, inv, buf) -> {
                ItemStack fromBuf = buf.readItem();
                ItemStack live = findHeldDualLunchbox(inv, fromBuf);
                return new DualLunchboxContainer(id, inv, live);
            })
    );

    private static final int FOOD_SLOTS = DualLunchboxItem.SLOTS;

    private final ItemStack dualLunchbox;
    private final ItemStackHandler container;
    private final int hotbarStart;

    public DualLunchboxContainer(int id, Inventory playerInv, ItemStack dualLunchbox) {
        super(TYPE.get(), id);
        this.dualLunchbox = dualLunchbox;
        this.container = DualLunchboxItem.getContainer(dualLunchbox);

        // Two food slots stacked vertically: same center X as single LunchboxContainer (80, 20),
        // plus one slot directly below. Player inventory is shifted down by 18px for clearance.
        this.addSlot(foodSlot(0, 80, 20));
        this.addSlot(foodSlot(1, 80, 38));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 69 + row * 18));
            }
        }
        this.hotbarStart = this.slots.size();
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 127) {
                @Override
                public boolean mayPickup(Player player) {
                    ItemStack stack = this.getItem();
                    return stack != DualLunchboxContainer.this.dualLunchbox && super.mayPickup(player);
                }
            });
        }
    }

    private SlotItemHandler foodSlot(int index, int x, int y) {
        return new SlotItemHandler(this.container, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return LunchboxItem.isFood(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                DualLunchboxItem.setContainer(
                        DualLunchboxContainer.this.dualLunchbox, DualLunchboxContainer.this.container);
            }
        };
    }

    private static ItemStack findHeldDualLunchbox(Inventory inv, ItemStack hint) {
        ItemStack main = inv.player.getMainHandItem();
        if (main.getItem() instanceof DualLunchboxItem) {
            return main;
        }
        ItemStack off = inv.player.getOffhandItem();
        if (off.getItem() instanceof DualLunchboxItem) {
            return off;
        }
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof DualLunchboxItem && ItemStack.isSameItemSameTags(stack, hint)) {
                return stack;
            }
        }
        return hint.isEmpty() ? main : hint;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == this.hotbarStart + player.getInventory().selected) {
            return;
        }
        if (clickType == ClickType.SWAP) {
            return;
        }
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot != null && slot.getItem() == this.dualLunchbox) {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
        DualLunchboxItem.setContainer(this.dualLunchbox, this.container);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < FOOD_SLOTS) {
                if (!this.moveItemStackTo(stack, FOOD_SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!LunchboxItem.isFood(stack)
                        || !this.moveItemStackTo(stack, 0, FOOD_SLOTS, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        DualLunchboxItem.setContainer(this.dualLunchbox, this.container);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main == this.dualLunchbox
                || off == this.dualLunchbox
                || (main.getItem() instanceof DualLunchboxItem && ItemStack.isSameItemSameTags(main, this.dualLunchbox))
                || (off.getItem() instanceof DualLunchboxItem && ItemStack.isSameItemSameTags(off, this.dualLunchbox));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        DualLunchboxItem.setContainer(this.dualLunchbox, this.container);
    }

    public static final class Provider implements MenuProvider {
        private final ItemStack dualLunchbox;

        public Provider(ItemStack dualLunchbox) {
            this.dualLunchbox = dualLunchbox;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("item.touhou_maid_eat_properly.dual_lunchbox");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new DualLunchboxContainer(id, inventory, this.dualLunchbox);
        }
    }
}
