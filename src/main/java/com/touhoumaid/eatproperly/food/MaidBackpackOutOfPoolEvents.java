package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.inventory.container.AbstractMaidContainer;
import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Detects player inserts into the maid backpack across an open maid GUI session
 * (open snapshot → close diff). Counts / refuse / anger / forgive; reverts angry
 * out-of-pool inserts back to the player.
 * <p>Only tracks {@link EntityMaid#getAvailableBackpackInv()} — not hidden maidInv cells.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class MaidBackpackOutOfPoolEvents {
    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    private MaidBackpackOutOfPoolEvents() {
    }

    private record Session(int maidId, Map<String, Integer> beforeCounts) {
    }

    @SubscribeEvent
    public static void onOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(event.getContainer() instanceof AbstractMaidContainer maidMenu)) {
            return;
        }
        EntityMaid maid = maidMenu.getMaid();
        if (maid == null || !maid.isOwnedBy(player)) {
            return;
        }
        SESSIONS.put(player.getUUID(), new Session(maid.getId(), countEdible(maid.getAvailableBackpackInv())));
    }

    /**
     * Runs before {@link FoodRequestDeliveryEvents} so out-of-pool revert / anger settle
     * before request completion scans the backpack.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        // Only consume the session when closing a maid container — other menus must not wipe it.
        if (!(event.getContainer() instanceof AbstractMaidContainer maidMenu)) {
            return;
        }
        Session session = SESSIONS.remove(player.getUUID());
        if (session == null) {
            return;
        }
        EntityMaid maid = maidMenu.getMaid();
        if (maid == null || maid.getId() != session.maidId()) {
            return;
        }
        settleBackpackSession(player, maid, session.beforeCounts());
    }

    private static void settleBackpackSession(Player player, EntityMaid maid, Map<String, Integer> before) {
        IItemHandler backpack = maid.getAvailableBackpackInv();
        Map<String, Integer> after = countEdible(backpack);
        List<ItemStack> outDeltas = new ArrayList<>();
        List<ItemStack> inDeltas = new ArrayList<>();
        for (Map.Entry<String, Integer> e : after.entrySet()) {
            String id = e.getKey();
            int delta = e.getValue() - before.getOrDefault(id, 0);
            if (delta <= 0) {
                continue;
            }
            ItemStack sample = FoodPool.fromId(id);
            if (!OutOfPoolDelivery.isEdible(sample)) {
                continue;
            }
            ItemStack withCount = sample.copyWithCount(delta);
            if (OutOfPoolDelivery.isInEffectivePool(maid, sample)) {
                inDeltas.add(withCount);
            } else {
                outDeltas.add(withCount);
            }
        }

        boolean blockedOnce = false;
        boolean anyOutStowed = false;
        for (ItemStack stack : outDeltas) {
            if (OutOfPoolDelivery.shouldBlockStow(maid, stack)) {
                ItemStack taken = extractById(backpack, FoodPool.toId(stack), stack.getCount());
                if (!taken.isEmpty() && player instanceof ServerPlayer) {
                    ItemHandlerHelper.giveItemToPlayer(player, taken);
                }
                if (!blockedOnce) {
                    OutOfPoolDelivery.onBlockedAngry(maid);
                    blockedOnce = true;
                }
                continue;
            }
            // Keep food in backpack; one strike for the whole close session (all out-of-pool kinds).
            anyOutStowed = true;
        }
        if (anyOutStowed) {
            ItemStack first = outDeltas.get(0);
            OutOfPoolDelivery.afterSuccessfulStow(maid, first, Math.max(1, first.getCount()));
        }

        for (ItemStack stack : inDeltas) {
            OutOfPoolDelivery.afterSuccessfulStow(maid, stack, stack.getCount());
        }
    }

    private static Map<String, Integer> countEdible(IItemHandler inv) {
        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < inv.getSlots(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!OutOfPoolDelivery.isEdible(stack)) {
                continue;
            }
            String id = FoodPool.toId(stack);
            if (id.isEmpty()) {
                continue;
            }
            counts.merge(id, stack.getCount(), Integer::sum);
        }
        return counts;
    }

    private static ItemStack extractById(IItemHandler inv, String id, int amount) {
        ItemStack collected = ItemStack.EMPTY;
        int remaining = amount;
        for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!OutOfPoolDelivery.isEdible(stack) || !id.equals(FoodPool.toId(stack))) {
                continue;
            }
            ItemStack taken = inv.extractItem(i, remaining, false);
            if (taken.isEmpty()) {
                continue;
            }
            remaining -= taken.getCount();
            if (collected.isEmpty()) {
                collected = taken;
            } else {
                collected.grow(taken.getCount());
            }
        }
        return collected;
    }
}
