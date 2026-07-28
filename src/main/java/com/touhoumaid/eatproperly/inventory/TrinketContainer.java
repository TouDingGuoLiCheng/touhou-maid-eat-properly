package com.touhoumaid.eatproperly.inventory;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.trinket.PlayerTrinketData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Player-only 3-slot trinket menu. Not tied to any held item; opened via {@code /ep trinkets}
 * (debug / fallback). Primary UX is the survival-inventory left panel.
 */
public class TrinketContainer extends AbstractContainerMenu {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EatProperlyMod.MOD_ID);

    public static final RegistryObject<MenuType<TrinketContainer>> TYPE = MENUS.register(
            "trinkets",
            () -> IForgeMenuType.create((id, inv, buf) -> new TrinketContainer(id, inv))
    );

    private static final int SLOT_SIZE = 18;
    private static final int TRINKET_ROW_X = 61;
    private static final int TRINKET_ROW_Y = 20;

    private final ItemStackHandler trinkets;

    public TrinketContainer(int id, Inventory playerInv) {
        super(TYPE.get(), id);
        this.trinkets = PlayerTrinketData.createHandler(playerInv.player);

        for (int i = 0; i < PlayerTrinketData.SLOT_COUNT; i++) {
            int x = TRINKET_ROW_X + i * SLOT_SIZE;
            this.addSlot(new SlotItemHandler(this.trinkets, i, x, TRINKET_ROW_Y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return PlayerTrinketData.isTrinket(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }

        // Player inventory (hopper Y offsets, same layout as LunchboxContainer).
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 109));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < PlayerTrinketData.SLOT_COUNT) {
                if (!this.moveItemStackTo(stack, PlayerTrinketData.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!PlayerTrinketData.isTrinket(stack)
                        || !this.moveItemStackTo(stack, 0, PlayerTrinketData.SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static void open(ServerPlayer player) {
        NetworkHooks.openScreen(player, provider());
    }

    private static MenuProvider provider() {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("gui.touhou_maid_eat_properly.trinkets");
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                return new TrinketContainer(id, inventory);
            }
        };
    }
}
