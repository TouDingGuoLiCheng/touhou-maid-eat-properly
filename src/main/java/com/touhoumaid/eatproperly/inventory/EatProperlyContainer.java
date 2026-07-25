package com.touhoumaid.eatproperly.inventory;

import com.github.tartaricacid.touhoulittlemaid.inventory.container.AbstractMaidContainer;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.food.FoodRequestData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EatProperlyContainer extends AbstractMaidContainer {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EatProperlyMod.MOD_ID);

    public static final RegistryObject<MenuType<EatProperlyContainer>> TYPE = MENUS.register(
            "eat_properly",
            () -> IForgeMenuType.create(EatProperlyContainer::fromNetwork)
    );

    /** Off-screen slot used to keep the client request icon in sync after completion. */
    private final ItemStackHandler requestSync = new ItemStackHandler(1);

    public EatProperlyContainer(int id, Inventory inventory, int entityId, ItemStack requestedFood) {
        super(TYPE.get(), id, inventory, entityId);
        this.requestSync.setStackInSlot(0, requestedFood.isEmpty() ? ItemStack.EMPTY : requestedFood.copy());
        this.addSlot(new SlotItemHandler(this.requestSync, 0, -999, -999) {
            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
    }

    private static EatProperlyContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
        int entityId = data.readInt();
        ItemStack food = data.readItem();
        return new EatProperlyContainer(windowId, inv, entityId, food);
    }

    public static MenuProvider create(int entityId) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("gui.touhou_maid_eat_properly.title");
            }

            @Override
            public EatProperlyContainer createMenu(int id, Inventory inventory, Player player) {
                ItemStack food = ItemStack.EMPTY;
                if (player.level().getEntity(entityId) instanceof com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid maid) {
                    food = FoodRequestData.getRequestStack(maid);
                }
                return new EatProperlyContainer(id, inventory, entityId, food);
            }
        };
    }

    public static void writeExtraData(FriendlyByteBuf buf, int entityId, ItemStack food) {
        buf.writeInt(entityId);
        buf.writeItem(food);
    }

    public ItemStack getRequestedFood() {
        return this.requestSync.getStackInSlot(0);
    }

    @Override
    public void broadcastChanges() {
        if (this.getMaid() != null && !this.getMaid().level().isClientSide()) {
            ItemStack now = FoodRequestData.getRequestStack(this.getMaid());
            ItemStack shown = this.requestSync.getStackInSlot(0);
            if (!ItemStack.isSameItemSameTags(now, shown) || now.getCount() != shown.getCount()) {
                this.requestSync.setStackInSlot(0, now.isEmpty() ? ItemStack.EMPTY : now.copy());
            }
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack stack1 = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack2 = slot.getItem();
            stack1 = stack2.copy();
            // Last slot is the off-screen request sync — never shift-click it.
            int syncSlot = this.slots.size() - 1;
            if (index == syncSlot) {
                return ItemStack.EMPTY;
            }
            if (index < 27) {
                if (!this.moveItemStackTo(stack2, 27, syncSlot, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack2, 0, 27, true)) {
                return ItemStack.EMPTY;
            }
            if (stack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return stack1;
    }
}
