package com.touhoumaid.eatproperly.item.reward;

import com.touhoumaid.eatproperly.client.HandwrittenLetterClient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Milestone reward letter (§3.6). Right-click opens a read-only, written-book-style UI built
 * from the {@code pages} NBT baked in by {@link HandwrittenLetterBook} (see
 * {@code MilestoneRewards#createBoundReward}, which pre-fills it on claim).
 *
 * <p>Kept as a custom item (not {@code Items.WRITTEN_BOOK}) so the milestone pool id / creative
 * tab / lore stay stable; {@link HandwrittenLetterClient} opens {@code BookViewScreen} with a
 * custom {@code BookAccess} instead of relying on {@code WrittenBookItem}, which requires the
 * stack to literally be a vanilla written book.
 */
public class HandwrittenLetterItem extends BoundRewardItem {
    public HandwrittenLetterItem(Properties properties, int loreLineCount) {
        super(properties, loreLineCount);
    }

    /** Bakes page NBT if missing; safe to call repeatedly (e.g. lazily on first use). */
    public static void ensurePages(ItemStack stack) {
        HandwrittenLetterBook.ensurePages(stack);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ensurePages(stack);
        if (level.isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> HandwrittenLetterClient.open(stack));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
