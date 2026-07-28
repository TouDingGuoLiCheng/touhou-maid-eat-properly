package com.touhoumaid.eatproperly.item.reward;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Bakes written-book-compatible {@code pages}/{@code title}/{@code author} NBT onto a
 * {@code handwritten_letter} stack, one §3.6 lore line per page, so the client can render it in a
 * {@code BookViewScreen} (see {@code HandwrittenLetterItem#use} / {@code HandwrittenLetterClient}).
 * Reuses the same {@code lore.0..4} keys as the tooltip so wording only lives in one place.
 */
final class HandwrittenLetterBook {
    static final String TAG_PAGES = "pages";
    static final String TAG_TITLE = "title";
    static final String TAG_AUTHOR = "author";
    private static final int LINE_COUNT = 5;
    private static final int SIGNATURE_LINE = LINE_COUNT - 1;

    private HandwrittenLetterBook() {
    }

    /** Idempotent: does nothing if the stack already has non-empty pages. */
    static void ensurePages(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_PAGES, Tag.TAG_LIST) && !tag.getList(TAG_PAGES, Tag.TAG_STRING).isEmpty()) {
            return;
        }
        CompoundTag rewardTag = stack.getTagElement(RewardTooltipText.REWARD_TAG);
        String maidName = rewardTag != null ? rewardTag.getString(RewardTooltipText.SOURCE_MAID_NAME) : "";
        writePages(stack, maidName);
    }

    private static void writePages(ItemStack stack, String maidName) {
        String displayName = RewardTooltipText.displaySourceName(maidName);
        String descriptionId = stack.getItem().getDescriptionId();
        ListTag pages = new ListTag();
        for (int line = 0; line < LINE_COUNT; line++) {
            Component text = line == SIGNATURE_LINE
                    ? Component.translatable(RewardTooltipText.loreKey(descriptionId, line), displayName)
                    : Component.translatable(RewardTooltipText.loreKey(descriptionId, line));
            pages.add(StringTag.valueOf(Component.Serializer.toJson(text)));
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_PAGES, pages);
        tag.putString(TAG_TITLE, "Handwritten Letter");
        tag.putString(TAG_AUTHOR, displayName);
    }
}
