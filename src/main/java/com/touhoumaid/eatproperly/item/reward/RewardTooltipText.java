package com.touhoumaid.eatproperly.item.reward;

final class RewardTooltipText {
    static final String REWARD_TAG = "EatProperlyReward";
    static final String SOURCE_MAID_NAME = "SourceMaidName";
    static final String SOURCE_MAID_UUID = "SourceMaidUuid";

    private RewardTooltipText() {
    }

    static String loreKey(String descriptionId, int line) {
        return descriptionId + ".lore." + line;
    }

    static String displaySourceName(String sourceMaidName) {
        return sourceMaidName.isBlank() ? "???" : sourceMaidName;
    }
}
