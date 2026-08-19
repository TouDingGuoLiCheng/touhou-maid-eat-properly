package com.touhoumaid.eatproperly.effect;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Maid-only Happiness satiety restore via {@link LivingEvent.LivingTickEvent}.
 * <p>
 * Player hunger continues to restore inside {@link HappinessEffect#applyEffectTick}.
 * Maids use this path so restore shares the same tick channel as other maid satiety logic
 * and does not rely on custom {@code MobEffect} ticks alone.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class HappinessMaidSatietyHandler {
    private HappinessMaidSatietyHandler() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }
        MobEffectInstance happiness = maid.getEffect(ModEffects.HAPPINESS.get());
        int duration = happiness == null ? 0 : happiness.getDuration();
        if (!HappinessMaidRestorePolicy.shouldRestoreThisTick(happiness != null, duration)) {
            return;
        }
        SatietyData.addSatiety(maid, HappinessTiming.HUNGER_RESTORE_AMOUNT);
    }
}
