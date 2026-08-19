package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.touhoumaid.eatproperly.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reads/writes Food Permit mark NBT; finds equipped permits on a maid. */
public final class FoodPermitData {
    private static final String ROOT_TAG = "FoodPermit";
    private static final String MARKS_TAG = "Marks";
    private static final String DIM_TAG = "Dim";
    private static final String X_TAG = "X";
    private static final String Y_TAG = "Y";
    private static final String Z_TAG = "Z";

    private FoodPermitData() {
    }

    public static List<FoodPermitMark> read(ItemStack stack) {
        List<FoodPermitMark> marks = new ArrayList<>();
        CompoundTag root = stack.getTagElement(ROOT_TAG);
        if (root == null) {
            return marks;
        }
        ListTag list = root.getList(MARKS_TAG, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size() && marks.size() < FoodPermitMarks.MAX; i++) {
            CompoundTag entry = list.getCompound(i);
            String dim = entry.getString(DIM_TAG);
            if (dim.isEmpty()) {
                continue;
            }
            marks.add(new FoodPermitMark(dim, entry.getInt(X_TAG), entry.getInt(Y_TAG), entry.getInt(Z_TAG)));
        }
        return marks;
    }

    public static void write(ItemStack stack, List<FoodPermitMark> marks) {
        CompoundTag root = stack.getOrCreateTagElement(ROOT_TAG);
        ListTag list = new ListTag();
        int count = Math.min(marks.size(), FoodPermitMarks.MAX);
        for (int i = 0; i < count; i++) {
            FoodPermitMark mark = marks.get(i);
            CompoundTag entry = new CompoundTag();
            entry.putString(DIM_TAG, mark.dimension());
            entry.putInt(X_TAG, mark.x());
            entry.putInt(Y_TAG, mark.y());
            entry.putInt(Z_TAG, mark.z());
            list.add(entry);
        }
        root.put(MARKS_TAG, list);
    }

    public static FoodPermitMark of(Level level, BlockPos pos) {
        ResourceKey<Level> key = level.dimension();
        ResourceLocation id = key.location();
        return new FoodPermitMark(id.toString(), pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean matchesDimension(FoodPermitMark mark, Level level) {
        return mark.dimension().equals(level.dimension().location().toString());
    }

    public static BlockPos toPos(FoodPermitMark mark) {
        return new BlockPos(mark.x(), mark.y(), mark.z());
    }

    /** First equipped Food Permit stack, if any. */
    public static Optional<ItemStack> findEquippedStack(EntityMaid maid) {
        BaubleItemHandler baubles = maid.getMaidBauble();
        for (int slot = 0; slot < baubles.getSlots(); slot++) {
            ItemStack stack = baubles.getStackInSlot(slot);
            if (stack.is(ModItems.FOOD_PERMIT.get())) {
                return Optional.of(stack);
            }
        }
        return Optional.empty();
    }

    public static List<FoodPermitMark> findEquippedMarks(EntityMaid maid) {
        return findEquippedStack(maid).map(stack -> {
            if (maid.level() != null && !maid.level().isClientSide()) {
                FoodPermitPrune.pruneStack(stack, maid.level());
            }
            return read(stack);
        }).orElseGet(List::of);
    }
}
