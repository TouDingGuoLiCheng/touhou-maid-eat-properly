package com.touhoumaid.eatproperly.item;

import com.github.tartaricacid.touhoulittlemaid.api.event.InteractMaidEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.advancement.EatProperlyAdvancements;
import com.touhoumaid.eatproperly.food.FoodRequestHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class LunchboxEvents {
    private LunchboxEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onInteractMaid(InteractMaidEvent event) {
        Player player = event.getPlayer();
        ItemStack stack = event.getStack();
        if (!(stack.getItem() instanceof LunchboxItem)) {
            return;
        }
        if (player.level().isClientSide()) {
            event.setCanceled(true);
            return;
        }

        EntityMaid maid = event.getMaid();
        if (!maid.isOwnedBy(player)) {
            return;
        }

        ItemStack food = LunchboxItem.getStoredFood(stack);
        if (food.isEmpty()) {
            // Empty box: open GUI instead of maid panel.
            if (player instanceof ServerPlayer serverPlayer) {
                LunchboxItem.openGui(serverPlayer, stack);
            }
            event.setCanceled(true);
            return;
        }

        // Always put food into the maid backpack; she eats it herself for satiety.
        // If it matches the current request, complete the request without removing the food.
        boolean delivered = LunchboxItem.deliverTo(stack, maid.getMaidInv());
        if (delivered) {
            FoodRequestHandler.tryCompleteFromBackpackNow(maid);
            if (player instanceof ServerPlayer serverPlayer) {
                EatProperlyAdvancements.onLunchboxDelivered(serverPlayer);
            }
            player.getCooldowns().addCooldown(stack.getItem(), 10);
        }
        event.setCanceled(true);
    }
}
