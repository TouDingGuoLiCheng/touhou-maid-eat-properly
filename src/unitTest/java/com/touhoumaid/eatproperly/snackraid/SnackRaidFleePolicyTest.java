package com.touhoumaid.eatproperly.snackraid;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnackRaidFleePolicyTest {
    @Test
    void awayDirectionPointsFromChestTowardMaid() {
        // Maid stands east of chest → flee further east (+X)
        double[] dir = SnackRaidFleePolicy.awayDirection(
                10.5, 64.0, 5.5,
                9.5, 64.5, 5.5);
        assertTrue(dir[0] > 0.9);
        assertTrue(Math.abs(dir[2]) < 0.1);
    }

    @Test
    void awayDirectionFallsBackWhenOverlappingChest() {
        double[] dir = SnackRaidFleePolicy.awayDirection(
                9.5, 64.0, 5.5,
                9.5, 64.5, 5.5);
        assertEquals(1.0, dir[0], 1.0E-6);
        assertEquals(0.0, dir[2], 1.0E-6);
    }

    @Test
    void sortCandidatesPrefersFartherFromChest() {
        int chestX = 0;
        int chestY = 64;
        int chestZ = 0;
        List<int[]> candidates = new ArrayList<>();
        candidates.add(new int[]{3, 64, 0});
        candidates.add(new int[]{8, 64, 0});
        candidates.add(new int[]{5, 64, 0});
        candidates.sort(SnackRaidFleePolicy.fartherFromChestFirst(chestX, chestY, chestZ));
        assertEquals(8, candidates.get(0)[0]);
        assertEquals(5, candidates.get(1)[0]);
        assertEquals(3, candidates.get(2)[0]);
    }

    @Test
    void satietyDoesNotAbortFleePhase() {
        assertFalse(SnackRaidFleePolicy.shouldAbortForSatiety(SnackRaidPhase.FLEE, 20, 18));
        assertTrue(SnackRaidFleePolicy.shouldAbortForSatiety(SnackRaidPhase.APPROACH, 20, 18));
        assertTrue(SnackRaidFleePolicy.shouldAbortForSatiety(SnackRaidPhase.WAIT, 18, 18));
        assertFalse(SnackRaidFleePolicy.shouldAbortForSatiety(SnackRaidPhase.APPROACH, 10, 18));
    }

    @Test
    void repathOnlyWhenNavigationDoneAndOnInterval() {
        assertTrue(SnackRaidFleePolicy.shouldRepathWhileFleeing(true, 1, 10));
        assertTrue(SnackRaidFleePolicy.shouldRepathWhileFleeing(true, 10, 10));
        assertFalse(SnackRaidFleePolicy.shouldRepathWhileFleeing(true, 3, 10));
        assertFalse(SnackRaidFleePolicy.shouldRepathWhileFleeing(false, 1, 10));
        assertFalse(SnackRaidFleePolicy.shouldRepathWhileFleeing(false, 10, 10));
    }

    @Test
    void fleeSpeedIsModerateNotSprint() {
        assertTrue(SnackRaidFleePolicy.FLEE_SPEED > SnackRaidFleePolicy.APPROACH_SPEED);
        assertTrue(SnackRaidFleePolicy.FLEE_SPEED <= 1.0F);
        assertTrue(SnackRaidFleePolicy.FLEE_SPEED >= 0.7F);
    }
}
