package com.touhoumaid.eatproperly.snackraid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnackRaidBegPolicyTest {
    @Test
    void beggingIsAllowedWhenIdleAndNotAngry() {
        assertTrue(SnackRaidBegPolicy.allowBeg(false, true));
    }

    @Test
    void raidingMaidDoesNotBegAtTheChest() {
        assertFalse(SnackRaidBegPolicy.allowBeg(true, true));
    }

    @Test
    void angerStillBlocksBeggingWithoutARaid() {
        assertFalse(SnackRaidBegPolicy.allowBeg(false, false));
    }

    @Test
    void raidAndAngerTogetherStayBlocked() {
        assertFalse(SnackRaidBegPolicy.allowBeg(true, false));
    }
}
