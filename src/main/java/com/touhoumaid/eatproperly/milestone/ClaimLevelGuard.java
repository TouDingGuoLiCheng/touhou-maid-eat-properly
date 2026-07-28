package com.touhoumaid.eatproperly.milestone;

/**
 * Pure claim-level check (no Minecraft types) so unit tests can cover stale-click rejection.
 */
public final class ClaimLevelGuard {
    private ClaimLevelGuard() {
    }

    /** True when the client-sent level still matches the current pending queue head. */
    public static boolean matchesPendingHead(int pendingHeadLevel, int expectedLevel) {
        return pendingHeadLevel == expectedLevel;
    }
}
