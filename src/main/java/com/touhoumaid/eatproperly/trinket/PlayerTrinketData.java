package com.touhoumaid.eatproperly.trinket;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Player-owned 3-slot trinket inventory, persisted under the shared "TouhouMaidEatProperly" root
 * (same root used by {@link com.touhoumaid.eatproperly.advancement.EatProperlyAdvancements}).
 * Only storage + tag whitelist live here; buff tick effects that read these slots are Task 10.
 */
public final class PlayerTrinketData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String TRINKET_INV = "TrinketInv";
    public static final int SLOT_COUNT = 3;

    public static final TagKey<Item> TRINKETS_TAG = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "trinkets"));

    private PlayerTrinketData() {
    }

    public static boolean isTrinket(ItemStack stack) {
        return !stack.isEmpty() && stack.is(TRINKETS_TAG);
    }

    /**
     * Builds a fresh handler seeded from the player's persisted trinket NBT. Any change made through
     * the returned handler (insert/extract/setStackInSlot) is written back to persistent data immediately.
     */
    public static ItemStackHandler createHandler(Player player) {
        ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                save(player, this);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return isTrinket(stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };
        CompoundTag root = root(player);
        if (root.contains(TRINKET_INV, Tag.TAG_COMPOUND)) {
            handler.deserializeNBT(root.getCompound(TRINKET_INV));
        }
        return handler;
    }

    public static void save(Player player, ItemStackHandler handler) {
        root(player).put(TRINKET_INV, handler.serializeNBT());
    }

    /** Read-only snapshot of currently equipped trinkets (empty slots omitted); for Task 10 buff lookups. */
    public static List<ItemStack> getEquipped(Player player) {
        List<ItemStack> result = new ArrayList<>(SLOT_COUNT);
        CompoundTag root = root(player);
        if (!root.contains(TRINKET_INV, Tag.TAG_COMPOUND)) {
            return result;
        }
        ItemStackHandler handler = new ItemStackHandler(SLOT_COUNT);
        handler.deserializeNBT(root.getCompound(TRINKET_INV));
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                result.add(stack);
            }
        }
        return result;
    }

    /** Clears the persisted trinket inventory (used on death when not keeping inventory). */
    public static void clear(Player player) {
        root(player).remove(TRINKET_INV);
    }

    /** Copies raw trinket NBT from an old player instance to a new one, e.g. {@code PlayerEvent.Clone}. */
    public static void copyToNewPlayer(Player oldPlayer, Player newPlayer) {
        CompoundTag oldRoot = root(oldPlayer);
        if (oldRoot.contains(TRINKET_INV, Tag.TAG_COMPOUND)) {
            root(newPlayer).put(TRINKET_INV, oldRoot.getCompound(TRINKET_INV).copy());
        }
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }
}
