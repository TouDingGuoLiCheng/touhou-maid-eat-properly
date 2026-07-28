package com.touhoumaid.eatproperly.inventory;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.item.LunchboxItem;
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
 * Single-slot lunchbox menu, layout aligned with vanilla hopper / TLM picnic basket.
 */
public class LunchboxContainer extends AbstractContainerMenu {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EatProperlyMod.MOD_ID);

    public static final RegistryObject<MenuType<LunchboxContainer>> TYPE = MENUS.register(
            "lunchbox",
            () -> IForgeMenuType.create((id, inv, buf) -> {
                ItemStack fromBuf = buf.readItem();
                ItemStack live = findHeldLunchbox(inv, fromBuf);
                return new LunchboxContainer(id, inv, live);
            })
    );

    private final ItemStack lunchbox;
    private final ItemStackHandler container;
    private final int hotbarStart;

    public LunchboxContainer(int id, Inventory playerInv, ItemStack lunchbox) {
        super(TYPE.get(), id);
        this.lunchbox = lunchbox;
        this.container = LunchboxItem.getContainer(lunchbox);

        // Single food slot centered (hopper middle slot position).
        this.addSlot(new SlotItemHandler(this.container, 0, 80, 20) {
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
                LunchboxItem.setContainer(LunchboxContainer.this.lunchbox, LunchboxContainer.this.container);
            }
        });

        // Player inventory (hopper Y offsets)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        this.hotbarStart = this.slots.size();
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 109) {
                @Override
                public boolean mayPickup(Player player) {
                    ItemStack stack = this.getItem();
                    return stack != LunchboxContainer.this.lunchbox && super.mayPickup(player);
                }
            });
        }
    }

    private static ItemStack findHeldLunchbox(Inventory inv, ItemStack hint) {
        ItemStack main = inv.player.getMainHandItem();
        if (main.getItem() instanceof LunchboxItem) {
            return main;
        }
        ItemStack off = inv.player.getOffhandItem();
        if (off.getItem() instanceof LunchboxItem) {
            return off;
        }
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof LunchboxItem && ItemStack.isSameItemSameTags(stack, hint)) {
                return stack;
            }
        }
        return hint.isEmpty() ? main : hint;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // Mirror picnic basket: block interacting with the open lunchbox / SWAP.
        if (slotId == this.hotbarStart + player.getInventory().selected) {
            return;
        }
        if (clickType == ClickType.SWAP) {
            return;
        }
        if (slotId >= 0 && slotId < this.slots.size()) {
            Slot slot = this.slots.get(slotId);
            if (slot != null && slot.getItem() == this.lunchbox) {
                return;
            }
        }
        super.clicked(slotId, button, clickType, player);
        LunchboxItem.setContainer(this.lunchbox, this.container);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index == 0) {
                if (!this.moveItemStackTo(stack, 1, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!LunchboxItem.isFood(stack) || !this.moveItemStackTo(stack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        LunchboxItem.setContainer(this.lunchbox, this.container);
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main == this.lunchbox
                || off == this.lunchbox
                || (main.getItem() instanceof LunchboxItem && ItemStack.isSameItemSameTags(main, this.lunchbox))
                || (off.getItem() instanceof LunchboxItem && ItemStack.isSameItemSameTags(off, this.lunchbox));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        LunchboxItem.setContainer(this.lunchbox, this.container);
    }

    public static final class Provider implements MenuProvider {
        private final ItemStack lunchbox;

        public Provider(ItemStack lunchbox) {
            this.lunchbox = lunchbox;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("item.touhou_maid_eat_properly.lunchbox");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new LunchboxContainer(id, inventory, this.lunchbox);
        }
    }
}
