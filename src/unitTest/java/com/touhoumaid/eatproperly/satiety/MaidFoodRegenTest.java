package com.touhoumaid.eatproperly.satiety;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MaidFoodRegenTest {

    @Test
    void fastRegenStartsAtTenFoodWithSaturation() {
        MaidFoodRegen.TickResult waiting = MaidFoodRegen.tick(10, 6.0F, true, 8, true);
        assertEquals(MaidFoodRegen.RegenKind.NONE, waiting.kind());
        assertEquals(9, waiting.tickTimer());

        MaidFoodRegen.TickResult heal = MaidFoodRegen.tick(10, 6.0F, true, 9, true);
        assertEquals(MaidFoodRegen.RegenKind.FAST, heal.kind());
        assertEquals(1.0F, heal.healAmount(), 0.001F);
        assertEquals(6.0F, heal.exhaustionAdded(), 0.001F);
        assertEquals(0, heal.tickTimer());
    }

    @Test
    void slowRegenBelowTenFood() {
        MaidFoodRegen.TickResult waiting = MaidFoodRegen.tick(9, 0.0F, true, 50, true);
        assertEquals(MaidFoodRegen.RegenKind.NONE, waiting.kind());
        assertEquals(51, waiting.tickTimer());

        MaidFoodRegen.TickResult heal = MaidFoodRegen.tick(9, 0.0F, true, 79, true);
        assertEquals(MaidFoodRegen.RegenKind.SLOW, heal.kind());
        assertEquals(1.0F, heal.healAmount(), 0.001F);
        assertEquals(6.0F, heal.exhaustionAdded(), 0.001F);
        assertEquals(0, heal.tickTimer());
    }

    @Test
    void exactlyTenWithoutSaturationStillFastRegens() {
        MaidFoodRegen.TickResult waiting = MaidFoodRegen.tick(10, 0.0F, true, 8, true);
        assertEquals(MaidFoodRegen.RegenKind.NONE, waiting.kind());
        assertEquals(9, waiting.tickTimer());

        MaidFoodRegen.TickResult heal = MaidFoodRegen.tick(10, 0.0F, true, 9, true);
        assertEquals(MaidFoodRegen.RegenKind.FAST, heal.kind());
        assertEquals(1.0F, heal.healAmount(), 0.001F);
        // 0.4 exhaustion × 20 HP ≈ 8 exhaustion ≈ 2 food (EXHAUSTION_DROP=4)
        assertEquals(MaidFoodRegen.NO_SAT_FAST_EXHAUSTION_PER_HP, heal.exhaustionAdded(), 0.001F);
        assertEquals(0, heal.tickTimer());
    }

    @Test
    void continuesFastRegenAfterSaturationDepletedWhileFoodAtLeastTen() {
        MaidFoodRegen.TickResult heal = MaidFoodRegen.tick(20, 0.0F, true, 9, true);
        assertEquals(MaidFoodRegen.RegenKind.FAST, heal.kind());
        assertEquals(1.0F, heal.healAmount(), 0.001F);
        assertEquals(MaidFoodRegen.NO_SAT_FAST_EXHAUSTION_PER_HP, heal.exhaustionAdded(), 0.001F);
        assertEquals(0, heal.tickTimer());
    }

    @Test
    void twoFoodYieldsTwentyHpWorthOfNoSatFastExhaustion() {
        // 20 HP × 0.4 = 8 exhaustion; 8 / 4 drop = 2 food
        assertEquals(8.0F, 20 * MaidFoodRegen.NO_SAT_FAST_EXHAUSTION_PER_HP, 0.001F);
        assertEquals(2.0F, (20 * MaidFoodRegen.NO_SAT_FAST_EXHAUSTION_PER_HP) / MaidFoodMath.EXHAUSTION_DROP, 0.001F);
    }

    @Test
    void noRegenWhenDisabledOrFullHealth() {
        MaidFoodRegen.TickResult disabled = MaidFoodRegen.tick(20, 6.0F, true, 9, false);
        MaidFoodRegen.TickResult healthy = MaidFoodRegen.tick(20, 6.0F, false, 9, true);
        assertFalse(disabled.shouldHeal());
        assertFalse(healthy.shouldHeal());
        assertEquals(0, disabled.tickTimer());
        assertEquals(0, healthy.tickTimer());
    }
}
