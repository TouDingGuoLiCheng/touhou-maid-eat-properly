package com.touhoumaid.eatproperly.item.reward;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Right-click an owned maid with an active request to make her forget it: clears the current
 * request without completing it (no favor gain, no thank-you line). See {@code CareItemEvents}.
 */
public class HypnosisNoteItem extends Item {
    public HypnosisNoteItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag rewardTag = stack.getTagElement(RewardTooltipText.REWARD_TAG);
        String sourceMaidName = rewardTag != null ? rewardTag.getString(RewardTooltipText.SOURCE_MAID_NAME) : "";
        tooltip.add(Component.translatable(RewardTooltipText.loreKey(getDescriptionId(), 0),
                        RewardTooltipText.displaySourceName(sourceMaidName))
                .withStyle(ChatFormatting.GRAY));
    }
}
