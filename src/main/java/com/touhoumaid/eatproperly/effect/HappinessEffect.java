package com.touhoumaid.eatproperly.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * "幸福" (Happiness): granted by eating milestone-reward tokens (thanks candy / half cake).
 * Steadily tops up <b>player</b> hunger while active; stacking is vanilla-default
 * "unique" (re-applying with a fresh full duration always overwrites a shorter remaining
 * duration at the same amplifier).
 * <p>
 * Maid satiety restore is handled separately by {@link HappinessMaidSatietyHandler} so it
 * does not depend on custom MobEffect ticks on {@code EntityMaid}.
 */
public class HappinessEffect extends MobEffect {
    public HappinessEffect() {
        // Pale yellow status-bar / particle tint to match the happiness icon.
        super(MobEffectCategory.BENEFICIAL, 0xFFE896);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return HappinessTiming.isHungerRestoreTick(duration);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            return;
        }
        // Player only — maid satiety is restored in HappinessMaidSatietyHandler.
        if (entity instanceof Player player) {
            // eat() already caps foodLevel at 20 internally; no saturation added.
            player.getFoodData().eat(HappinessTiming.HUNGER_RESTORE_AMOUNT, 0.0F);
        }
    }
}
