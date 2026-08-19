package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodRequestScheduleTest {
    @Test
    void rollIntervalStaysInRangeAndAvoidsLastWhenPossible() {
        Random random = new Random(42L);
        for (int i = 0; i < 40; i++) {
            int rolled = FoodRequestSchedule.rollInterval(random, 5, 15, 10);
            assertTrue(rolled >= 5 && rolled <= 15);
            assertNotEquals(10, rolled);
        }
    }

    @Test
    void pickPrefersRolledDayWhenFree() {
        Set<Long> occupied = new HashSet<>();
        Random random = new Random(1L);
        long[] picked = FoodRequestSchedule.pickNextRequestDay(100L, 5, 15, -1, occupied, random);
        assertTrue(picked[0] >= 105L && picked[0] <= 115L);
        assertEquals(picked[0] - 100L, picked[1]);
    }

    @Test
    void pickOverflowsPastWindowWhenFull() {
        Set<Long> occupied = new HashSet<>();
        for (int d = 5; d <= 15; d++) {
            occupied.add(100L + d);
        }
        occupied.add(116L);
        occupied.add(117L);
        Random random = new Random(7L);
        long[] picked = FoodRequestSchedule.pickNextRequestDay(100L, 5, 15, 8, occupied, random);
        assertEquals(118L, picked[0]);
        assertEquals(18L, picked[1]);
    }

    @Test
    void pickUsesOtherFreeDayInWindowWhenPreferredTaken() {
        Set<Long> occupied = new HashSet<>();
        // Occupy every day except 100+7
        for (int d = 5; d <= 15; d++) {
            if (d != 7) {
                occupied.add(100L + d);
            }
        }
        Random random = new Random(99L);
        long[] picked = FoodRequestSchedule.pickNextRequestDay(100L, 5, 15, -1, occupied, random);
        assertEquals(107L, picked[0]);
        assertEquals(7L, picked[1]);
    }
}
