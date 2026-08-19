package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutOfPoolAngerPolicyTest {
    @Test
    void firstOutOfPoolOpensWindowAndCounts() {
        OutOfPoolAngerPolicy.State before = OutOfPoolAngerPolicy.State.idle();
        OutOfPoolAngerPolicy.Result r = OutOfPoolAngerPolicy.onOutOfPoolStowed(1000L, 1, before,
                2000, 1000, 10, 12000);
        assertEquals(1, r.state().count());
        assertEquals(1000L + 2000, r.state().windowEnd());
        assertFalse(r.becameAngry());
        assertTrue(r.shouldStow());
    }

    @Test
    void furtherStowExtendsWindowAndAddsOnePerEvent() {
        OutOfPoolAngerPolicy.State before = new OutOfPoolAngerPolicy.State(3000L, 2, 0L);
        OutOfPoolAngerPolicy.Result r = OutOfPoolAngerPolicy.onOutOfPoolStowed(2500L, 3, before,
                2000, 1000, 10, 12000);
        assertEquals(3, r.state().count());
        assertEquals(3000L + 1000, r.state().windowEnd());
        assertFalse(r.becameAngry());
    }

    @Test
    void batchStackStillCountsAsOneEvent() {
        OutOfPoolAngerPolicy.State before = OutOfPoolAngerPolicy.State.idle();
        OutOfPoolAngerPolicy.Result r = OutOfPoolAngerPolicy.onOutOfPoolStowed(1000L, 64, before,
                2000, 1000, 10, 12000);
        assertEquals(1, r.state().count());
        assertFalse(r.becameAngry());
    }

    @Test
    void reachingThresholdEntersAnger() {
        OutOfPoolAngerPolicy.State before = new OutOfPoolAngerPolicy.State(5000L, 9, 0L);
        OutOfPoolAngerPolicy.Result r = OutOfPoolAngerPolicy.onOutOfPoolStowed(4000L, 1, before,
                2000, 1000, 10, 12000);
        assertTrue(r.becameAngry());
        assertEquals(0, r.state().count());
        assertEquals(0L, r.state().windowEnd());
        assertEquals(4000L + 12000, r.state().angerEnd());
        assertTrue(r.shouldStow());
    }

    @Test
    void expiredWindowResetsBeforeCounting() {
        OutOfPoolAngerPolicy.State before = new OutOfPoolAngerPolicy.State(2000L, 9, 0L);
        OutOfPoolAngerPolicy.Result r = OutOfPoolAngerPolicy.onOutOfPoolStowed(2000L, 1, before,
                2000, 1000, 10, 12000);
        // windowEnd == now means expired; treat as fresh window
        assertEquals(1, r.state().count());
        assertEquals(2000L + 2000, r.state().windowEnd());
        assertFalse(r.becameAngry());
    }

    @Test
    void angryOutOfPoolDoesNotStowAndRefreshesAnger() {
        OutOfPoolAngerPolicy.State before = new OutOfPoolAngerPolicy.State(0L, 0, 5000L);
        OutOfPoolAngerPolicy.Result r = OutOfPoolAngerPolicy.onOutOfPoolWhileAngry(4000L, before, 12000);
        assertFalse(r.shouldStow());
        assertEquals(4000L + 12000, r.state().angerEnd());
        assertFalse(r.becameAngry());
        assertFalse(r.forgave());
    }

    @Test
    void forgiveClearsAnger() {
        OutOfPoolAngerPolicy.State before = new OutOfPoolAngerPolicy.State(0L, 0, 9000L);
        OutOfPoolAngerPolicy.Result r = OutOfPoolAngerPolicy.onForgive(8000L, before);
        assertTrue(r.forgave());
        assertEquals(0L, r.state().angerEnd());
        assertTrue(r.shouldStow());
    }

    @Test
    void isAngryRespectsDeadline() {
        assertTrue(OutOfPoolAngerPolicy.isAngry(1000L, 2000L));
        assertFalse(OutOfPoolAngerPolicy.isAngry(2000L, 2000L));
        assertFalse(OutOfPoolAngerPolicy.isAngry(1000L, 0L));
    }

    @Test
    void guiBlockBubbleCooldown() {
        assertTrue(OutOfPoolAngerPolicy.canShowGuiBlockBubble(1000L, 0L, 80));
        assertFalse(OutOfPoolAngerPolicy.canShowGuiBlockBubble(1050L, 1000L, 80));
        assertTrue(OutOfPoolAngerPolicy.canShowGuiBlockBubble(1080L, 1000L, 80));
    }
}
