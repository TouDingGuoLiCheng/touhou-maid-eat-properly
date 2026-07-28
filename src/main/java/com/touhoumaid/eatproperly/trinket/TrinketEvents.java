package com.touhoumaid.eatproperly.trinket;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Mirrors vanilla inventory death/respawn rules for the player trinket slots:
 * dropped and cleared on death unless {@code keepInventory}; otherwise carried over via
 * {@link PlayerEvent.Clone} (also covers the vanilla end-portal player-instance swap).
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class TrinketEvents {
    private TrinketEvents() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }
        List<ItemStack> equipped = PlayerTrinketData.getEquipped(player);
        if (equipped.isEmpty()) {
            return;
        }
        for (ItemStack stack : equipped) {
            player.spawnAtLocation(stack);
        }
        PlayerTrinketData.clear(player);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)
                || !(event.getOriginal() instanceof ServerPlayer oldPlayer)) {
            return;
        }
        PlayerTrinketData.copyToNewPlayer(oldPlayer, newPlayer);
    }
}
