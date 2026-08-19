package com.touhoumaid.eatproperly.inventory;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.AbstractMaidContainer;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.food.FoodRequestData;
import com.touhoumaid.eatproperly.milestone.MilestoneRewards;
import com.touhoumaid.eatproperly.satiety.MaidFoodData;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import com.touhoumaid.eatproperly.snackraid.SnackRaidData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
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

    /** Off-screen: request food sync. */
    private final ItemStackHandler requestSync = new ItemStackHandler(1);
    /** Off-screen: pending milestone reward preview. */
    private final ItemStackHandler milestoneSync = new ItemStackHandler(1);
    /** 0 = UI mode, 1 = pending level or -1, 2 = request forgotten, 3 = local snack-raid allowed, 4 = saturation*10. */
    private final ContainerData milestoneData = new SimpleContainerData(5);

    public EatProperlyContainer(int id, Inventory inventory, int entityId, ItemStack requestedFood,
                                ItemStack pendingReward, int milestoneMode, int pendingLevel,
                                boolean requestForgotten, boolean snackRaidAllowed) {
        super(TYPE.get(), id, inventory, entityId);
        this.requestSync.setStackInSlot(0, requestedFood.isEmpty() ? ItemStack.EMPTY : requestedFood.copy());
        this.milestoneSync.setStackInSlot(0, pendingReward.isEmpty() ? ItemStack.EMPTY : pendingReward.copy());
        this.milestoneData.set(0, milestoneMode);
        this.milestoneData.set(1, pendingLevel);
        this.milestoneData.set(2, requestForgotten ? 1 : 0);
        this.milestoneData.set(3, snackRaidAllowed ? 1 : 0);
        this.milestoneData.set(4, 0);
        this.addDataSlots(this.milestoneData);

        this.addSlot(lockedSyncSlot(this.requestSync, 0));
        this.addSlot(lockedSyncSlot(this.milestoneSync, 0));
    }

    private static SlotItemHandler lockedSyncSlot(ItemStackHandler handler, int index) {
        return new SlotItemHandler(handler, index, -999, -999) {
            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        };
    }

    private static EatProperlyContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
        int entityId = data.readInt();
        ItemStack food = data.readItem();
        ItemStack pending = data.readItem();
        int mode = data.readVarInt();
        int pendingLevel = data.readVarInt();
        boolean forgotten = data.readBoolean();
        boolean snackRaidAllowed = data.readBoolean();
        return new EatProperlyContainer(windowId, inv, entityId, food, pending, mode, pendingLevel,
                forgotten, snackRaidAllowed);
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
                ItemStack pending = ItemStack.EMPTY;
                int mode = MilestoneRewards.MODE_WAITING;
                int pendingLevel = -1;
                boolean forgotten = false;
                boolean snackRaidAllowed = true;
                if (player.level().getEntity(entityId) instanceof EntityMaid maid) {
                    SatietyData.ensureInitialized(maid);
                    food = FoodRequestData.getRequestStack(maid);
                    pending = MilestoneRewards.previewStack(maid);
                    mode = MilestoneRewards.uiMode(maid);
                    pendingLevel = MilestoneRewards.previewLevel(maid);
                    forgotten = FoodRequestData.isForgotten(maid);
                    snackRaidAllowed = SnackRaidData.isAllowedLocally(maid);
                }
                EatProperlyContainer container = new EatProperlyContainer(id, inventory, entityId, food, pending, mode,
                        pendingLevel, forgotten, snackRaidAllowed);
                if (player.level().getEntity(entityId) instanceof EntityMaid maid) {
                    container.milestoneData.set(4, MaidFoodData.getSaturationSync(maid));
                }
                return container;
            }
        };
    }

    public static void writeExtraData(FriendlyByteBuf buf, int entityId, ItemStack food,
                                      ItemStack pending, int mode, int pendingLevel,
                                      boolean forgotten, boolean snackRaidAllowed) {
        buf.writeInt(entityId);
        buf.writeItem(food);
        buf.writeItem(pending);
        buf.writeVarInt(mode);
        buf.writeVarInt(pendingLevel);
        buf.writeBoolean(forgotten);
        buf.writeBoolean(snackRaidAllowed);
    }

    public ItemStack getRequestedFood() {
        return this.requestSync.getStackInSlot(0);
    }

    public ItemStack getPendingMilestoneReward() {
        return this.milestoneSync.getStackInSlot(0);
    }

    public int getMilestoneMode() {
        return this.milestoneData.get(0);
    }

    public int getPendingMilestoneLevel() {
        return this.milestoneData.get(1);
    }

    public boolean isRequestForgotten() {
        return this.milestoneData.get(2) != 0;
    }

    public boolean isSnackRaidAllowed() {
        return this.milestoneData.get(3) != 0;
    }

    /** Saturation level * 10 for GUI sync. */
    public float getSaturationLevel() {
        return this.milestoneData.get(4) / 10.0F;
    }

    @Override
    public void broadcastChanges() {
        if (this.getMaid() != null && !this.getMaid().level().isClientSide()) {
            EntityMaid maid = this.getMaid();
            ItemStack nowFood = FoodRequestData.getRequestStack(maid);
            ItemStack shownFood = this.requestSync.getStackInSlot(0);
            if (!ItemStack.isSameItemSameTags(nowFood, shownFood) || nowFood.getCount() != shownFood.getCount()) {
                this.requestSync.setStackInSlot(0, nowFood.isEmpty() ? ItemStack.EMPTY : nowFood.copy());
            }

            ItemStack nowPending = MilestoneRewards.previewStack(maid);
            ItemStack shownPending = this.milestoneSync.getStackInSlot(0);
            if (!ItemStack.isSameItemSameTags(nowPending, shownPending)
                    || nowPending.getCount() != shownPending.getCount()) {
                this.milestoneSync.setStackInSlot(0, nowPending.isEmpty() ? ItemStack.EMPTY : nowPending.copy());
            }

            int mode = MilestoneRewards.uiMode(maid);
            if (this.milestoneData.get(0) != mode) {
                this.milestoneData.set(0, mode);
            }
            int pendingLevel = MilestoneRewards.previewLevel(maid);
            if (this.milestoneData.get(1) != pendingLevel) {
                this.milestoneData.set(1, pendingLevel);
            }

            int forgotten = FoodRequestData.isForgotten(maid) ? 1 : 0;
            if (this.milestoneData.get(2) != forgotten) {
                this.milestoneData.set(2, forgotten);
            }
            int snackRaidAllowed = SnackRaidData.isAllowedLocally(maid) ? 1 : 0;
            if (this.milestoneData.get(3) != snackRaidAllowed) {
                this.milestoneData.set(3, snackRaidAllowed);
            }
            int saturationSync = MaidFoodData.getSaturationSync(maid);
            if (this.milestoneData.get(4) != saturationSync) {
                this.milestoneData.set(4, saturationSync);
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
            int syncStart = this.slots.size() - 2;
            if (index >= syncStart) {
                return ItemStack.EMPTY;
            }
            if (index < 27) {
                if (!this.moveItemStackTo(stack2, 27, syncStart, false)) {
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
