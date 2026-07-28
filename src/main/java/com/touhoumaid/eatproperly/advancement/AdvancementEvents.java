package com.touhoumaid.eatproperly.advancement;

import com.github.tartaricacid.touhoulittlemaid.api.event.MaidFavorabilityLevelChangeEvent;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class AdvancementEvents {
    private AdvancementEvents() {
    }

    @SubscribeEvent
    public static void onFavorLevelChange(MaidFavorabilityLevelChangeEvent event) {
        EntityMaid maid = event.getMaid();
        if (maid.level().isClientSide()) {
            return;
        }
        int newLevel = event.getNewLevel();
        Player owner = maid.getOwner() instanceof Player player ? player : null;
        if (!(owner instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (newLevel > event.getOldLevel()) {
            EatProperlyAdvancements.onFavorLevelReached(serverPlayer, newLevel);
        }
        EatProperlyAdvancements.onMaidFavorForWings(serverPlayer, maid.getUUID(), newLevel);
    }
}
