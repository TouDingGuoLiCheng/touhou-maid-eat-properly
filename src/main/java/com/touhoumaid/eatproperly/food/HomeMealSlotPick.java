package com.touhoumaid.eatproperly.food;

/**
 * Pure home-meal slot pick: when any edible picnic slot matches the current request,
 * restrict the random pick to those slots.
 */
public final class HomeMealSlotPick {
    private HomeMealSlotPick() {
    }

    /**
     * @param edibleMatchesRequest parallel flags for the edible-slot list (index into that list)
     * @return indices into the edible-slot list to choose among
     */
    public static int[] candidateIndices(boolean[] edibleMatchesRequest) {
        if (edibleMatchesRequest == null || edibleMatchesRequest.length == 0) {
            return new int[0];
        }
        int matchCount = 0;
        for (boolean match : edibleMatchesRequest) {
            if (match) {
                matchCount++;
            }
        }
        if (matchCount == 0) {
            int[] all = new int[edibleMatchesRequest.length];
            for (int i = 0; i < all.length; i++) {
                all[i] = i;
            }
            return all;
        }
        int[] preferred = new int[matchCount];
        int w = 0;
        for (int i = 0; i < edibleMatchesRequest.length; i++) {
            if (edibleMatchesRequest[i]) {
                preferred[w++] = i;
            }
        }
        return preferred;
    }

    /** Maps a random candidate index to the picnic-handler slot id. */
    public static int pickHandlerSlot(int[] handlerSlotsForCandidates, int randomCandidateIndex) {
        if (handlerSlotsForCandidates == null || handlerSlotsForCandidates.length == 0) {
            return -1;
        }
        int i = Math.floorMod(randomCandidateIndex, handlerSlotsForCandidates.length);
        return handlerSlotsForCandidates[i];
    }
}
