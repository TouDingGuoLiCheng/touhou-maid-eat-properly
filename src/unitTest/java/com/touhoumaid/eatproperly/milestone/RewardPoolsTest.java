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
    void nonMilestoneEmpty() {
        assertTrue(RewardPoolTables.categoryForLevel(7).isEmpty());
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
        Set<String> exclude = new HashSet<>(RewardPoolTables.poolPaths(RewardCategory.CARE));
        assertFalse(RewardPoolTables.pickPath(RewardCategory.CARE, exclude, bound -> 0).isPresent());
    }
}
