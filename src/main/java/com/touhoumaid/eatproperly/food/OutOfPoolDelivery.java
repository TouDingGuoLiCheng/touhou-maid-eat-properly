package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import net.minecraft.world.item.ItemStack;

/**
 * Player-explicit edible give hooks for out-of-effective-pool refuse / anger / forgive.
 */
public final class OutOfPoolDelivery {
    private OutOfPoolDelivery() {
    }

    public static boolean isEdible(ItemStack stack) {
        return stack != null && !stack.isEmpty() && stack.isEdible();
    }

    public static boolean isInEffectivePool(EntityMaid maid, ItemStack stack) {
        return FoodPool.isInEffectivePool(stack, OrderBookData.findAllEquipped(maid));
    }

    /** True when angry and this edible is outside the effective pool — caller must not stow. */
    public static boolean shouldBlockStow(EntityMaid maid, ItemStack food) {
        if (!isEdible(food)) {
            return false;
        }
        long now = maid.level().getGameTime();
        if (!OutOfPoolAngerData.isAngry(maid, now)) {
            return false;
        }
        return !isInEffectivePool(maid, food);
    }

    /** Angry + out-of-pool: refresh anger and refuse bubble (no stow). */
    public static void onBlockedAngry(EntityMaid maid) {
        long now = maid.level().getGameTime();
        OutOfPoolAngerPolicy.State before = OutOfPoolAngerData.sync(maid, now);
        OutOfPoolAngerPolicy.Result result = OutOfPoolAngerPolicy.onOutOfPoolWhileAngry(
                now, before, EatProperlyConfig.OUT_OF_POOL_ANGER_DURATION_TICKS.get());
        OutOfPoolAngerData.write(maid, result.state());
        speakRefuse(maid);
    }

    /**
     * After {@code amount} of edible {@code food} was successfully stowed by a player-explicit path.
     * Only edible stacks outside the effective request pool refuse/count (non-food ignored).
     * Each call counts as one anger strike regardless of {@code amount} (batch = 1).
     * In-pool while angry: forgive bubble + clear anger.
     * <p>If already angry and the food is still out-of-pool, treats as blocked refresh
     * (defensive — callers should not stow out-of-pool while angry).
     */
    public static void afterSuccessfulStow(EntityMaid maid, ItemStack food, int amount) {
        if (!isEdible(food) || amount <= 0) {
            return;
        }
        long now = maid.level().getGameTime();
        OutOfPoolAngerPolicy.State before = OutOfPoolAngerData.sync(maid, now);
        boolean inPool = isInEffectivePool(maid, food);
        if (inPool) {
            if (OutOfPoolAngerPolicy.isAngry(now, before.angerEnd())) {
                OutOfPoolAngerPolicy.Result result = OutOfPoolAngerPolicy.onForgive(now, before);
                OutOfPoolAngerData.write(maid, result.state());
                speakForgive(maid);
            }
            return;
        }
        if (OutOfPoolAngerPolicy.isAngry(now, before.angerEnd())) {
            onBlockedAngry(maid);
            return;
        }
        OutOfPoolAngerPolicy.Result result = OutOfPoolAngerPolicy.onOutOfPoolStowed(
                now,
                amount,
                before,
                EatProperlyConfig.OUT_OF_POOL_WINDOW_INITIAL_TICKS.get(),
                EatProperlyConfig.OUT_OF_POOL_WINDOW_EXTEND_TICKS.get(),
                EatProperlyConfig.OUT_OF_POOL_ANGER_THRESHOLD.get(),
                EatProperlyConfig.OUT_OF_POOL_ANGER_DURATION_TICKS.get());
        OutOfPoolAngerData.write(maid, result.state());
        speakRefuse(maid);
        if (result.becameAngry()) {
            speakAngry(maid);
        }
    }

    /** Block opening maid GUI while angry; bubble with cooldown. */
    public static boolean tryBlockGui(EntityMaid maid) {
        long now = maid.level().getGameTime();
        if (!OutOfPoolAngerData.isAngry(maid, now)) {
            return false;
        }
        int cooldown = EatProperlyConfig.OUT_OF_POOL_GUI_BUBBLE_COOLDOWN_TICKS.get();
        if (OutOfPoolAngerPolicy.canShowGuiBlockBubble(now, OutOfPoolAngerData.getGuiBubbleAt(maid), cooldown)) {
            speakAngry(maid);
            OutOfPoolAngerData.setGuiBubbleAt(maid, now);
        }
        return true;
    }

    private static void speakRefuse(EntityMaid maid) {
        String text = MaidSpeech.formatOutOfPoolRefuse(maid.getRandom());
        if (!text.isEmpty()) {
            MaidSpeech.bubbleToOwnerIfNear(maid, text);
        }
    }

    private static void speakAngry(EntityMaid maid) {
        String text = MaidSpeech.formatOutOfPoolAngry(maid.getRandom());
        if (!text.isEmpty()) {
            MaidSpeech.bubbleToOwnerIfNear(maid, text);
        }
    }

    private static void speakForgive(EntityMaid maid) {
        String text = MaidSpeech.formatOutOfPoolForgive(maid.getRandom());
        if (!text.isEmpty()) {
            MaidSpeech.bubbleToOwnerIfNear(maid, text);
        }
    }
}
