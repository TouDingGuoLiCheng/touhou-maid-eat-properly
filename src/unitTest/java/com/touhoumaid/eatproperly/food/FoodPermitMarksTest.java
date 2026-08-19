package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoodPermitMarksTest {
    private static final FoodPermitMark A = new FoodPermitMark("minecraft:overworld", 1, 2, 3);
    private static final FoodPermitMark B = new FoodPermitMark("minecraft:overworld", 4, 5, 6);
    private static final FoodPermitMark OTHER_DIM = new FoodPermitMark("minecraft:the_nether", 1, 2, 3);

    @Test
    void toggleAddsThenRemoves() {
        List<FoodPermitMark> marks = new ArrayList<>();
        assertEquals(FoodPermitMarks.ToggleResult.ADDED, FoodPermitMarks.toggle(marks, A));
        assertTrue(marks.contains(A));
        assertEquals(FoodPermitMarks.ToggleResult.REMOVED, FoodPermitMarks.toggle(marks, A));
        assertFalse(marks.contains(A));
    }

    @Test
    void sameCoordsDifferentDimensionAreDistinct() {
        List<FoodPermitMark> marks = new ArrayList<>();
        assertEquals(FoodPermitMarks.ToggleResult.ADDED, FoodPermitMarks.toggle(marks, A));
        assertEquals(FoodPermitMarks.ToggleResult.ADDED, FoodPermitMarks.toggle(marks, OTHER_DIM));
        assertEquals(2, marks.size());
    }

    @Test
    void ninthMarkRejectedAsFull() {
        List<FoodPermitMark> marks = new ArrayList<>();
        for (int i = 0; i < FoodPermitMarks.MAX; i++) {
            assertEquals(FoodPermitMarks.ToggleResult.ADDED,
                    FoodPermitMarks.toggle(marks, new FoodPermitMark("minecraft:overworld", i, 0, 0)));
        }
        assertEquals(FoodPermitMarks.ToggleResult.FULL,
                FoodPermitMarks.toggle(marks, new FoodPermitMark("minecraft:overworld", 99, 0, 0)));
        assertEquals(FoodPermitMarks.MAX, marks.size());
    }

    @Test
    void clearRemovesAll() {
        List<FoodPermitMark> marks = new ArrayList<>();
        FoodPermitMarks.toggle(marks, A);
        FoodPermitMarks.toggle(marks, B);
        FoodPermitMarks.clear(marks);
        assertTrue(marks.isEmpty());
    }

    @Test
    void removeInvalidDropsGhostsAndKeepsValid() {
        List<FoodPermitMark> marks = new ArrayList<>();
        marks.add(A);
        marks.add(B);
        marks.add(OTHER_DIM);
        // A invalid, B valid, OTHER_DIM valid (other dim not checked this pass)
        boolean changed = FoodPermitMarks.removeInvalid(marks, new boolean[]{false, true, true});
        assertTrue(changed);
        assertEquals(List.of(B, OTHER_DIM), marks);
    }

    @Test
    void removeInvalidNoopWhenAllValid() {
        List<FoodPermitMark> marks = new ArrayList<>();
        marks.add(A);
        assertFalse(FoodPermitMarks.removeInvalid(marks, new boolean[]{true}));
        assertEquals(1, marks.size());
    }

    @Test
    void shouldKeepOnlyWhenMarkableContainerPresent() {
        assertTrue(FoodPermitMarks.shouldKeepMark(true));
        assertFalse(FoodPermitMarks.shouldKeepMark(false));
    }
}
