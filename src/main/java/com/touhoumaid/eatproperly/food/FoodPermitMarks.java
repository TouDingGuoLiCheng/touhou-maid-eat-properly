package com.touhoumaid.eatproperly.food;

import java.util.List;

/**
 * Pure list operations for Food Permit marks (max {@link #MAX}).
 */
public final class FoodPermitMarks {
    public static final int MAX = 8;

    public enum ToggleResult {
        ADDED,
        REMOVED,
        FULL
    }

    private FoodPermitMarks() {
    }

    public static ToggleResult toggle(List<FoodPermitMark> marks, FoodPermitMark mark) {
        int index = marks.indexOf(mark);
        if (index >= 0) {
            marks.remove(index);
            return ToggleResult.REMOVED;
        }
        if (marks.size() >= MAX) {
            return ToggleResult.FULL;
        }
        marks.add(mark);
        return ToggleResult.ADDED;
    }

    public static void clear(List<FoodPermitMark> marks) {
        marks.clear();
    }

    /** Keep only when the marked spot still has a markable container. */
    public static boolean shouldKeepMark(boolean markableContainerPresent) {
        return markableContainerPresent;
    }

    /**
     * Removes marks whose parallel {@code validByIndex} entry is false.
     * @return true if the list changed
     */
    public static boolean removeInvalid(List<FoodPermitMark> marks, boolean[] validByIndex) {
        if (marks == null || validByIndex == null || marks.size() != validByIndex.length) {
            return false;
        }
        boolean changed = false;
        for (int i = marks.size() - 1; i >= 0; i--) {
            if (!validByIndex[i]) {
                marks.remove(i);
                changed = true;
            }
        }
        return changed;
    }
}
