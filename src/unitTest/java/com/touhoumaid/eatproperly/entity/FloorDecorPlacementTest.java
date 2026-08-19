package com.touhoumaid.eatproperly.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FloorDecorPlacementTest {
    @Test
    void yawTurnsModelFrontTowardPlayer() {
        assertEquals(180.0F, FloorDecorPlacement.yawFacingPlayer(0.0F));
        assertEquals(270.0F, FloorDecorPlacement.yawFacingPlayer(90.0F));
    }

    @Test
    void feetSitOnTopCenterOfSupportBlock() {
        assertArrayEquals(
                new double[]{3.5D, 5.0D, -1.5D},
                FloorDecorPlacement.feetOnBlockTop(3, 4, -2)
        );
    }
}
