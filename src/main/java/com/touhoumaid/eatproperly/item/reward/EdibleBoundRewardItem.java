package com.touhoumaid.eatproperly.item.reward;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Edible milestone token that, once eaten, leaves behind a bound derived item copying the same
 * {@code EatProperlyReward} tag (e.g. thanks candy -&gt; crumpled wrapper, half cake -&gt; cake note).
 * Mirrors vanilla's stew/honey-bottle leftover pattern: in creative mode the original stack is
 * left untouched (no leftover forced on the player), matching {@code MushroomStewItem} /
 * {@code HoneyBottleItem}.
 */
public class EdibleBoundRewardItem extends Item {
    private final int loreLineCount;
    private final Supplier<? extends Item> leftoverItem;

    public EdibleBoundRewardItem(Properties properties, int loreLineCount, Supplier<? extends Item> leftoverItem) {
        super(properties);
        this.loreLineCount = loreLineCount;
        this.leftoverItem = leftoverItem;
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
        tooltip.add(Component.translatable("effect.touhou_maid_eat_properly.happiness.desc")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        CompoundTag rewardTag = stack.getTagElement(RewardTooltipText.REWARD_TAG);
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (entity instanceof Player player && player.getAbilities().instabuild) {
            return result;
        }
        ItemStack leftover = new ItemStack(leftoverItem.get());
        if (rewardTag != null) {
            leftover.addTagElement(RewardTooltipText.REWARD_TAG, rewardTag.copy());
        }
        return leftover;
    }
}
