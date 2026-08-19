package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeMealSlotPickTest {
    @Test
    void whenNoSlotMatchesRequest_keepsAllEdibleIndices() {
        assertArrayEquals(new int[]{0, 1, 2}, HomeMealSlotPick.candidateIndices(new boolean[]{false, false, false}));
    }

    @Test
    void whenSomeSlotsMatch_onlyThoseRemain() {
        assertArrayEquals(new int[]{0, 2}, HomeMealSlotPick.candidateIndices(new boolean[]{true, false, true}));
    }

    @Test
    void emptyInput_returnsEmpty() {
        assertEquals(0, HomeMealSlotPick.candidateIndices(new boolean[0]).length);
    }

    @Test
    void pickUsesCandidateAtRandomIndex() {
        int[] candidates = {1, 3, 5};
        assertEquals(3, HomeMealSlotPick.pickHandlerSlot(candidates, 1));
    }
}
