package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.touhoumaid.eatproperly.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Reads/writes Order Book filter NBT; finds all equipped books on a maid. */
public final class OrderBookData {
    private static final String ROOT_TAG = "OrderBook";
    private static final String MODE_TAG = "Mode";
    private static final String IDS_TAG = "Ids";

    private OrderBookData() {
    }

    public static OrderBookSnapshot read(ItemStack stack) {
        CompoundTag root = stack.getTagElement(ROOT_TAG);
        if (root == null) {
            return OrderBookSnapshot.EMPTY_BLACKLIST;
        }
        OrderBookFilterMode mode = OrderBookFilterMode.fromName(root.getString(MODE_TAG), OrderBookFilterMode.BLACKLIST);
        Set<String> ids = new LinkedHashSet<>();
        ListTag list = root.getList(IDS_TAG, Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ids.add(list.getString(i));
        }
        return new OrderBookSnapshot(mode, ids);
    }

    public static void write(ItemStack stack, OrderBookSnapshot snapshot) {
        CompoundTag root = stack.getOrCreateTagElement(ROOT_TAG);
        root.putString(MODE_TAG, snapshot.mode().name());
        ListTag list = new ListTag();
        for (String id : snapshot.listedIds()) {
            list.add(StringTag.valueOf(id));
        }
        root.put(IDS_TAG, list);
    }

    /** Duplicate an Order Book stack, copying all item NBT (mode + listed ids). */
    public static ItemStack copyStack(ItemStack source) {
        ItemStack copy = new ItemStack(ModItems.ORDER_BOOK.get());
        CompoundTag tag = source.getTag();
        if (tag != null) {
            copy.setTag(tag.copy());
        }
        return copy;
    }

    /** Every equipped Order Book, in bauble slot order (may be empty). */
    public static List<OrderBookSnapshot> findAllEquipped(EntityMaid maid) {
        List<OrderBookSnapshot> books = new ArrayList<>();
        BaubleItemHandler baubles = maid.getMaidBauble();
        for (int slot = 0; slot < baubles.getSlots(); slot++) {
            ItemStack stack = baubles.getStackInSlot(slot);
            if (stack.is(ModItems.ORDER_BOOK.get())) {
                books.add(read(stack));
            }
        }
        return books;
    }

    /**
     * First equipped Order Book, if any.
     * Prefer {@link #findAllEquipped} + {@link OrderBookIntersection} for request pooling.
     */
    public static Optional<OrderBookSnapshot> findEquipped(EntityMaid maid) {
        List<OrderBookSnapshot> books = findAllEquipped(maid);
        return books.isEmpty() ? Optional.empty() : Optional.of(books.get(0));
    }
}
