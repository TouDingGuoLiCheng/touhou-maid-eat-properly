package com.touhoumaid.eatproperly.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaidTopTabPlacementTest {
    private static final int LEFT = 10;

    @Test
    void firstExternalTabUses194OffsetWhenRowEmpty() {
        assertEquals(LEFT + 194, MaidTopTabPlacement.nextTopTabX(LEFT, new int[0]));
    }

    @Test
    void appendsAfterExistingTopTabsWithSpacing() {
        int first = LEFT + 194;
        assertEquals(first + 25, MaidTopTabPlacement.nextTopTabX(LEFT, new int[] {first}));
        assertEquals(first + 50, MaidTopTabPlacement.nextTopTabX(LEFT, new int[] {first, first + 25}));
    }
}
