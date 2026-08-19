package com.touhoumaid.eatproperly.effect;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Ambient heart particles while Happiness is active — same {@link ParticleTypes#HEART}
 * visual as TLM meal favor hearts / vanilla breeding hearts.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID)
public final class HappinessHeartParticles {
    private HappinessHeartParticles() {
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) {
            return;
        }
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (!entity.hasEffect(ModEffects.HAPPINESS.get())) {
            return;
        }
        if (!HappinessTiming.isHeartParticleTick(entity.tickCount)) {
            return;
        }
        double x = entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();
        double y = entity.getY() + 0.4D + entity.getRandom().nextDouble() * entity.getBbHeight() * 0.8D;
        double z = entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * entity.getBbWidth();
        level.sendParticles(ParticleTypes.HEART, x, y, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }
}
