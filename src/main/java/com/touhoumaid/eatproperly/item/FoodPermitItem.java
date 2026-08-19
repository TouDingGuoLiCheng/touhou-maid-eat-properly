package com.touhoumaid.eatproperly.item;

import com.touhoumaid.eatproperly.food.FoodPermitData;
import com.touhoumaid.eatproperly.food.FoodPermitMark;
import com.touhoumaid.eatproperly.food.FoodPermitMarks;
import com.touhoumaid.eatproperly.food.FoodPermitPrune;
import com.touhoumaid.eatproperly.food.FoodPermitPrunePolicy;
import com.touhoumaid.eatproperly.snackraid.SnackRaidContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Maid bauble: marks containers for open food fetch; player configures marks while held. */
public class FoodPermitItem extends Item {
    public FoodPermitItem(Properties properties) {
        super(properties);
    }

    /**
     * N3.6g-count: while held (selected), periodically drop marks that are no longer markable
     * containers so tooltip count matches outlines.
     */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!FoodPermitPrunePolicy.shouldPruneWhileHeld(isSelected, !level.isClientSide())) {
            return;
        }
        if (!FoodPermitPrunePolicy.shouldPruneThisTick(
                level.getGameTime(), FoodPermitPrunePolicy.HOLD_PRUNE_INTERVAL_TICKS)) {
            return;
        }
        FoodPermitPrune.pruneStack(stack, level);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        if (player.isShiftKeyDown()) {
            return clearAll(level, player, stack);
        }
        BlockPos pos = context.getClickedPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null || !SnackRaidContainers.isMarkableContainer(blockEntity)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        List<FoodPermitMark> marks = FoodPermitData.read(stack);
        FoodPermitMark mark = FoodPermitData.of(level, pos);
        FoodPermitMarks.ToggleResult result = FoodPermitMarks.toggle(marks, mark);
        switch (result) {
            case ADDED -> {
                FoodPermitData.write(stack, marks);
                playFeedback(level, player, pos);
                player.displayClientMessage(
                        Component.translatable("message.touhou_maid_eat_properly.food_permit.marked"), true);
            }
            case REMOVED -> {
                FoodPermitData.write(stack, marks);
                playFeedback(level, player, pos);
                player.displayClientMessage(
                        Component.translatable("message.touhou_maid_eat_properly.food_permit.unmarked"), true);
            }
            case FULL -> player.displayClientMessage(
                    Component.translatable("message.touhou_maid_eat_properly.food_permit.full"), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        InteractionResult clear = clearAll(level, player, stack);
        if (clear.consumesAction()) {
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    private static InteractionResult clearAll(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        List<FoodPermitMark> marks = FoodPermitData.read(stack);
        if (marks.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.touhou_maid_eat_properly.food_permit.cleared"), true);
            return InteractionResult.CONSUME;
        }
        FoodPermitMarks.clear(marks);
        FoodPermitData.write(stack, marks);
        level.playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.get(),
                SoundSource.PLAYERS, 0.4F, 1.2F);
        player.displayClientMessage(
                Component.translatable("message.touhou_maid_eat_properly.food_permit.cleared"), true);
        return InteractionResult.CONSUME;
    }

    private static void playFeedback(Level level, Player player, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.touhou_maid_eat_properly.food_permit.lore.0"));
        tooltip.add(Component.translatable("item.touhou_maid_eat_properly.food_permit.lore.1"));
        int count = FoodPermitData.read(stack).size();
        tooltip.add(Component.translatable("item.touhou_maid_eat_properly.food_permit.lore.marks",
                count, FoodPermitMarks.MAX));
    }
}
