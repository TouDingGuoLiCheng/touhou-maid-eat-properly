package com.touhoumaid.eatproperly.item.reward;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.item.LunchboxItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Single-slot lunchbox bound to whichever maid granted it (milestone reward).
 * Delivery only works on that exact maid; see {@code LunchboxEvents} for the bind check.
 */
public class BoundLunchboxItem extends LunchboxItem {
    public BoundLunchboxItem(Properties properties) {
        super(properties);
    }

    /** False (reject) when the reward tag is missing/unbound, or bound to a different maid. */
    public static boolean matchesBoundMaid(ItemStack stack, EntityMaid maid) {
        CompoundTag tag = stack.getTagElement(RewardTooltipText.REWARD_TAG);
        if (tag == null || !tag.hasUUID(RewardTooltipText.SOURCE_MAID_UUID)) {
            return false;
        }
        return tag.getUUID(RewardTooltipText.SOURCE_MAID_UUID).equals(maid.getUUID());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag rewardTag = stack.getTagElement(RewardTooltipText.REWARD_TAG);
        String sourceMaidName = rewardTag != null ? rewardTag.getString(RewardTooltipText.SOURCE_MAID_NAME) : "";
        tooltip.add(Component.translatable(RewardTooltipText.loreKey(getDescriptionId(), 0),
                        RewardTooltipText.displaySourceName(sourceMaidName))
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
