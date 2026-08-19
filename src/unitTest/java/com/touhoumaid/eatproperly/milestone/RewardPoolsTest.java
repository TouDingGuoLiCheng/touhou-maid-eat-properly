package com.touhoumaid.eatproperly.milestone;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RewardPoolsTest {
    @Test
    void level50IsBuff() {
        assertEquals(RewardCategory.BUFF, RewardPoolTables.categoryForLevel(50).orElseThrow());
    }

    @Test
    void level5IsTokenLow() {
        assertEquals(RewardCategory.TOKEN_LOW, RewardPoolTables.categoryForLevel(5).orElseThrow());
    }

    @Test
    void level10IsTokenMid() {
        assertEquals(RewardCategory.TOKEN_MID, RewardPoolTables.categoryForLevel(10).orElseThrow());
    }

    @Test
    void level40IsBuff() {
        assertEquals(RewardCategory.BUFF, RewardPoolTables.categoryForLevel(40).orElseThrow());
    }

    @Test
    void level70IsTokenMid() {
        assertEquals(RewardCategory.TOKEN_MID, RewardPoolTables.categoryForLevel(70).orElseThrow());
    }

    @Test
    void nonMilestoneEmpty() {
        assertTrue(RewardPoolTables.categoryForLevel(7).isEmpty());
    }

    @Test
    void tokenMidContainsBoundLunchbox() {
        assertTrue(RewardPoolTables.poolPaths(RewardCategory.TOKEN_MID).contains("bound_lunchbox"));
    }

    @Test
    void buffContainsDualLunchbox() {
        assertTrue(RewardPoolTables.poolPaths(RewardCategory.BUFF).contains("dual_lunchbox"));
    }

    @Test
    void poolsDoNotContainAltarCareItems() {
        for (RewardCategory cat : RewardCategory.values()) {
            assertFalse(RewardPoolTables.poolPaths(cat).contains("extra_meal_permit"));
            assertFalse(RewardPoolTables.poolPaths(cat).contains("hypnosis_note"));
        }
    }

    @Test
    void pickExcludesObtained() {
        Set<String> exclude = new HashSet<>(RewardPoolTables.poolPaths(RewardCategory.BUFF));
        exclude.remove("heart_seasoning");
        Optional<String> picked = RewardPoolTables.pickPath(RewardCategory.BUFF, exclude, bound -> 0);
        assertEquals(Optional.of("heart_seasoning"), picked);
    }

    @Test
    void pickEmptyWhenAllExcluded() {
        Set<String> exclude = new HashSet<>(RewardPoolTables.poolPaths(RewardCategory.TOKEN_MID));
        assertFalse(RewardPoolTables.pickPath(RewardCategory.TOKEN_MID, exclude, bound -> 0).isPresent());
    }
}
