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
 * Shared placeholder item for rewards whose lore may reference the maid that granted them.
 */
public class BoundRewardItem extends Item {
    private final int loreLineCount;

    public BoundRewardItem(Properties properties, int loreLineCount) {
        super(properties);
        this.loreLineCount = loreLineCount;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag rewardTag = stack.getTagElement(RewardTooltipText.REWARD_TAG);
        String sourceMaidName = rewardTag != null
                ? rewardTag.getString(RewardTooltipText.SOURCE_MAID_NAME)
                : "";
        String displayName = RewardTooltipText.displaySourceName(sourceMaidName);
        for (int line = 0; line < loreLineCount; line++) {
            tooltip.add(Component.translatable(RewardTooltipText.loreKey(getDescriptionId(), line), displayName)
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
