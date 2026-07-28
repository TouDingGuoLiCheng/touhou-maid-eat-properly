package com.touhoumaid.eatproperly.mixin;

import com.touhoumaid.eatproperly.trinket.PlayerTrinketData;
import com.touhoumaid.eatproperly.trinket.PlayerTrinketPanel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Appends this mod's 3 trinket slots to the survival inventory menu (left rail when panel open).
 */
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin extends AbstractContainerMenu {
    protected InventoryMenuMixin(@Nullable MenuType<?> type, int id) {
        super(type, id);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void eatproperly$addTrinketSlots(Inventory playerInv, boolean active, Player player, CallbackInfo ci) {
        ItemStackHandler trinkets = PlayerTrinketData.createHandler(player);
        for (int i = 0; i < PlayerTrinketData.SLOT_COUNT; i++) {
            this.addSlot(new SlotItemHandler(trinkets, i,
                    PlayerTrinketPanel.SLOT_X,
                    PlayerTrinketPanel.SLOT_Y0 + i * PlayerTrinketPanel.SLOT_STRIDE) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return isActive() && PlayerTrinketData.isTrinket(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public boolean isActive() {
                    return PlayerTrinketPanel.isSlotActive(player);
                }

                @Override
                public boolean mayPickup(Player p) {
                    return isActive() && super.mayPickup(p);
                }
            });
        }
    }
}
