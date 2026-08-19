package com.touhoumaid.eatproperly.favorability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RequestFavorPolicyTest {

    @Test
    void normalAndDualFlatGrants() {
        assertEquals(192, RequestFavorPolicy.NORMAL_POINTS);
        assertEquals(384, RequestFavorPolicy.DUAL_POINTS);
    }

    @Test
    void clampDoesNotExceedMaxPoint() {
        assertEquals(192, RequestFavorPolicy.clampedGrant(0, 192, 10000));
        assertEquals(50, RequestFavorPolicy.clampedGrant(9950, 192, 10000));
        assertEquals(0, RequestFavorPolicy.clampedGrant(10000, 192, 10000));
        assertEquals(0, RequestFavorPolicy.clampedGrant(10000, 384, 10000));
        assertEquals(384, RequestFavorPolicy.clampedGrant(0, 384, 10000));
    }

    @Test
    void nonPositiveGrantIsZero() {
        assertEquals(0, RequestFavorPolicy.clampedGrant(10, 0, 100));
        assertEquals(0, RequestFavorPolicy.clampedGrant(10, -5, 100));
    }
}
