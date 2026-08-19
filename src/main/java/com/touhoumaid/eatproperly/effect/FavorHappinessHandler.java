package com.touhoumaid.eatproperly.effect;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import com.touhoumaid.eatproperly.food.OwnerProximityPolicy;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * High-favor maids near their owner refresh Happiness. Leaving range does not clear the effect.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class FavorHappinessHandler {
    private static final int MIN_FAVOR_LEVEL = 80;

    private FavorHappinessHandler() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!(event.getEntity() instanceof EntityMaid maid)) {
            return;
        }
        boolean eligible = isEligible(maid);
        MobEffectInstance happiness = maid.getEffect(ModEffects.HAPPINESS.get());
        int duration = happiness == null ? 0 : happiness.getDuration();
        if (!FavorHappinessPolicy.shouldRefresh(eligible, duration)) {
            return;
        }
        maid.addEffect(new MobEffectInstance(
                ModEffects.HAPPINESS.get(),
                HappinessTiming.PROXIMITY_DURATION_TICKS,
                0,
                true,
                false,
                true));
    }

    private static boolean isEligible(EntityMaid maid) {
        if (maid.getFavorabilityManager().getLevel() < MIN_FAVOR_LEVEL) {
            return false;
        }
        LivingEntity ownerEntity = maid.getOwner();
        if (!(ownerEntity instanceof Player owner) || owner.level() != maid.level()) {
            return false;
        }
        return OwnerProximityPolicy.isWithinRange(
                maid.distanceToSqr(owner), MaidSpeech.OWNER_SPEECH_RANGE);
    }
}
