package com.touhoumaid.eatproperly.item.reward;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BoundRewardItemTest {
    @Test
    void buildsIndexedLoreKeysFromItemDescriptionId() {
        assertEquals(
                "item.touhou_maid_eat_properly.cake_note.lore.1",
                RewardTooltipText.loreKey("item.touhou_maid_eat_properly.cake_note", 1)
        );
    }

    @Test
    void usesQuestionMarksWhenRewardHasNoSourceNameYet() {
        assertEquals("???", RewardTooltipText.displaySourceName(""));
        assertEquals("Koakuma", RewardTooltipText.displaySourceName("Koakuma"));
    }

    @Test
    void usesTheSameNestedSourceNamePathAsRewardGranting() {
        assertEquals("EatProperlyReward", RewardTooltipText.REWARD_TAG);
        assertEquals("SourceMaidName", RewardTooltipText.SOURCE_MAID_NAME);
    }
}
