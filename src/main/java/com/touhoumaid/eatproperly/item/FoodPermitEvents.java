package com.touhoumaid.eatproperly.item;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.snackraid.SnackRaidContainers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Holding Food Permit in the main hand must mark containers, not open their GUI.
 * Chest {@code Block.use} otherwise wins over {@link FoodPermitItem#useOn}.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class FoodPermitEvents {
    private FoodPermitEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!(event.getItemStack().getItem() instanceof FoodPermitItem)) {
            return;
        }
        BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos());
        if (blockEntity == null || !SnackRaidContainers.isMarkableContainer(blockEntity)) {
            return;
        }
        event.setUseBlock(Event.Result.DENY);
    }
}
