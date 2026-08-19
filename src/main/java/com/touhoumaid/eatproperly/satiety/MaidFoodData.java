package com.touhoumaid.eatproperly.satiety;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.GameRules;

/**
 * Maid analogue of player {@link net.minecraft.world.food.FoodData}:
 * foodLevel on {@code maid.getHunger()}, saturation/exhaustion/tickTimer in persistent NBT.
 */
public final class MaidFoodData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String SATURATION = "SaturationLevel";
    private static final String EXHAUSTION = "ExhaustionLevel";
    private static final String TICK_TIMER = "FoodTickTimer";

    private MaidFoodData() {
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    public static float getSaturation(EntityMaid maid) {
        SatietyData.ensureInitialized(maid);
        return root(maid).getFloat(SATURATION);
    }

    public static void setSaturation(EntityMaid maid, float value) {
        root(maid).putFloat(SATURATION, MaidFoodMath.clampSaturation(value, SatietyData.getSatiety(maid)));
    }

    public static float getExhaustion(EntityMaid maid) {
        return root(maid).getFloat(EXHAUSTION);
    }

    public static void setExhaustion(EntityMaid maid, float value) {
        root(maid).putFloat(EXHAUSTION, Math.max(0.0F, value));
    }

    public static void addExhaustion(EntityMaid maid, float amount) {
        if (amount <= 0.0F) {
            return;
        }
        setExhaustion(maid, getExhaustion(maid) + amount);
    }

    public static int getTickTimer(EntityMaid maid) {
        return root(maid).getInt(TICK_TIMER);
    }

    public static void setTickTimer(EntityMaid maid, int timer) {
        root(maid).putInt(TICK_TIMER, Math.max(0, timer));
    }

    /** Synced GUI value: saturation * 10 rounded. */
    public static int getSaturationSync(EntityMaid maid) {
        return Math.round(getSaturation(maid) * 10.0F);
    }

    public static void eat(EntityMaid maid, int nutrition, float saturationModifier) {
        if (nutrition <= 0) {
            return;
        }
        SatietyData.ensureInitialized(maid);
        MaidFoodMath.EatResult result = MaidFoodMath.eat(
                SatietyData.getSatiety(maid),
                getSaturation(maid),
                nutrition,
                saturationModifier);
        applyEatResult(maid, result);
        if (result.foodLevel() >= EatProperlyConfig.SATIETY_LOW_THRESHOLD.get()) {
            SatietyData.setLowSatietyDays(maid, 0);
        }
    }

    public static void eatNutritionOnly(EntityMaid maid, int nutrition) {
        eat(maid, nutrition, 0.0F);
    }

    private static void applyEatResult(EntityMaid maid, MaidFoodMath.EatResult result) {
        SatietyData.setSatiety(maid, result.foodLevel());
        setSaturation(maid, result.saturationLevel());
    }

    public static void drainSaturationFirst(EntityMaid maid, int points) {
        if (points <= 0) {
            return;
        }
        MaidFoodMath.EatResult result = MaidFoodMath.drainSaturationFirst(
                SatietyData.getSatiety(maid), getSaturation(maid), points);
        applyEatResult(maid, result);
    }

    /** Called every server tick for natural regen + exhaustion settlement. */
    public static void tick(EntityMaid maid) {
        SatietyData.ensureInitialized(maid);
        boolean naturalRegen = maid.level().getGameRules().getBoolean(GameRules.RULE_NATURAL_REGENERATION);
        boolean injured = maid.getHealth() < maid.getMaxHealth();
        MaidFoodRegen.TickResult regen = MaidFoodRegen.tick(
                SatietyData.getSatiety(maid),
                getSaturation(maid),
                injured,
                getTickTimer(maid),
                naturalRegen);
        setTickTimer(maid, regen.tickTimer());
        if (regen.shouldHeal()) {
            float missing = Math.max(0.0F, maid.getMaxHealth() - maid.getHealth());
            float healed = Math.min(regen.healAmount(), missing);
            if (healed > 0.0F) {
                maid.heal(healed);
            }
            addExhaustion(maid, regen.exhaustionAdded());
        }
        settleExhaustion(maid);
    }

    private static void settleExhaustion(EntityMaid maid) {
        MaidFoodMath.FoodState state = new MaidFoodMath.FoodState(
                SatietyData.getSatiety(maid),
                getSaturation(maid),
                getExhaustion(maid));
        while (state.exhaustion() > MaidFoodMath.EXHAUSTION_DROP) {
            state = MaidFoodMath.settleExhaustionOnce(state);
        }
        SatietyData.setSatiety(maid, state.foodLevel());
        setSaturation(maid, state.saturationLevel());
        setExhaustion(maid, state.exhaustion());
    }

    /** Initialize saturation when maid is first set up. */
    public static void initializeDefaults(EntityMaid maid) {
        CompoundTag tag = root(maid);
        if (!tag.contains(SATURATION)) {
            tag.putFloat(SATURATION, 5.0F);
        }
        if (!tag.contains(EXHAUSTION)) {
            tag.putFloat(EXHAUSTION, 0.0F);
        }
        if (!tag.contains(TICK_TIMER)) {
            tag.putInt(TICK_TIMER, 0);
        }
        setSaturation(maid, getSaturation(maid));
    }

    /** Clamp saturation after food level changes externally. */
    public static void clampSaturationToFood(EntityMaid maid) {
        setSaturation(maid, Mth.clamp(getSaturation(maid), 0.0F, SatietyData.getSatiety(maid)));
    }
}
