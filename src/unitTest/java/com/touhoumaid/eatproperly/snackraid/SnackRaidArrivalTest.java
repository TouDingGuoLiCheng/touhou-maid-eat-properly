package com.touhoumaid.eatproperly.snackraid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnackRaidArrivalTest {
    private static double sq(double value) {
        return value * value;
    }

    @Test
    void acceptsTheSpotWherePathfindingActuallyStops() {
        // Vanilla stops within ~0.45 blocks of the last node on each axis; worst case is the diagonal.
        double worstCase = sq(0.45D) + sq(0.45D);
        assertTrue(SnackRaidArrival.arrivedAtStand(worstCase));
        assertTrue(SnackRaidArrival.arrivedAtStand(sq(1.4D)));
    }

    @Test
    void rejectsStandingAFullCellAway() {
        assertFalse(SnackRaidArrival.arrivedAtStand(sq(1.6D)));
        assertFalse(SnackRaidArrival.arrivedAtStand(sq(2.0D)));
    }

    @Test
    void fleeArrivalIsMoreForgivingThanApproach() {
        assertTrue(SnackRaidArrival.arrivedAtFlee(sq(2.0D)));
        assertFalse(SnackRaidArrival.arrivedAtFlee(sq(2.1D)));
        assertFalse(SnackRaidArrival.arrivedAtStand(sq(2.0D)));
    }

    @Test
    void waitIsCancelledOnlyWhenDraggedWellAwayFromTheContainer() {
        assertFalse(SnackRaidArrival.leftContainerDuringWait(sq(1.5D)));
        assertFalse(SnackRaidArrival.leftContainerDuringWait(sq(4.0D)));
        assertTrue(SnackRaidArrival.leftContainerDuringWait(sq(4.5D)));
    }
}
