package com.touhoumaid.eatproperly.food;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderBookIntersectionTest {
    private static final String APPLE = "minecraft:apple";
    private static final String BREAD = "minecraft:bread";
    private static final String BEEF = "minecraft:cooked_beef";

    @Test
    void noBooksAllowsEverything() {
        assertTrue(OrderBookIntersection.allowsAll(List.of(), APPLE));
        assertTrue(OrderBookIntersection.allowsAll(null, BREAD));
    }

    @Test
    void singleWhitelistBook() {
        OrderBookSnapshot book = new OrderBookSnapshot(OrderBookFilterMode.WHITELIST, Set.of(APPLE, BREAD));
        assertTrue(OrderBookIntersection.allowsAll(List.of(book), APPLE));
        assertFalse(OrderBookIntersection.allowsAll(List.of(book), BEEF));
    }

    @Test
    void twoWhitelistsTakeIntersection() {
        OrderBookSnapshot a = new OrderBookSnapshot(OrderBookFilterMode.WHITELIST, Set.of(APPLE, BREAD));
        OrderBookSnapshot b = new OrderBookSnapshot(OrderBookFilterMode.WHITELIST, Set.of(BREAD, BEEF));
        assertTrue(OrderBookIntersection.allowsAll(List.of(a, b), BREAD));
        assertFalse(OrderBookIntersection.allowsAll(List.of(a, b), APPLE));
        assertFalse(OrderBookIntersection.allowsAll(List.of(a, b), BEEF));
    }

    @Test
    void foodBookPlusEmptyWhitelistYieldsEmptyPool() {
        OrderBookSnapshot food = new OrderBookSnapshot(OrderBookFilterMode.WHITELIST, Set.of(APPLE, BREAD));
        OrderBookSnapshot emptyMenu = new OrderBookSnapshot(OrderBookFilterMode.WHITELIST, Set.of());
        assertFalse(OrderBookIntersection.allowsAll(List.of(food, emptyMenu), APPLE));
        assertFalse(OrderBookIntersection.allowsAll(List.of(food, emptyMenu), BREAD));
    }

    @Test
    void twoBlacklistsRejectUnionOfBannedIds() {
        OrderBookSnapshot a = new OrderBookSnapshot(OrderBookFilterMode.BLACKLIST, Set.of(APPLE));
        OrderBookSnapshot b = new OrderBookSnapshot(OrderBookFilterMode.BLACKLIST, Set.of(BREAD));
        assertFalse(OrderBookIntersection.allowsAll(List.of(a, b), APPLE));
        assertFalse(OrderBookIntersection.allowsAll(List.of(a, b), BREAD));
        assertTrue(OrderBookIntersection.allowsAll(List.of(a, b), BEEF));
    }

    @Test
    void emptyBlacklistDoesNotShrinkIntersection() {
        OrderBookSnapshot emptyBlack = OrderBookSnapshot.EMPTY_BLACKLIST;
        OrderBookSnapshot white = new OrderBookSnapshot(OrderBookFilterMode.WHITELIST, Set.of(APPLE));
        assertTrue(OrderBookIntersection.allowsAll(List.of(emptyBlack, white), APPLE));
        assertFalse(OrderBookIntersection.allowsAll(List.of(emptyBlack, white), BREAD));
    }
}
