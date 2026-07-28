package com.touhoumaid.eatproperly.trinket;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.handler.BaubleItemHandler;
import com.touhoumaid.eatproperly.item.ModItems;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only snapshot of this mod's trinkets currently in a maid's TLM bauble inventory.
 * Night sparrow is excluded even if somehow present (player-only).
 */
public final class MaidTrinketData {
    private MaidTrinketData() {
    }

    public static List<ItemStack> getEquipped(EntityMaid maid) {
        BaubleItemHandler baubles = maid.getMaidBauble();
        List<ItemStack> result = new ArrayList<>(baubles.getSlots());
        for (int i = 0; i < baubles.getSlots(); i++) {
            ItemStack stack = baubles.getStackInSlot(i);
            if (stack.isEmpty() || !PlayerTrinketData.isTrinket(stack)) {
                continue;
            }
            if (stack.is(ModItems.NIGHT_SPARROW_WRAPPER.get())) {
                continue;
            }
            result.add(stack);
        }
        return result;
    }
}
