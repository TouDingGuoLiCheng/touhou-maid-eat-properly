package com.touhoumaid.eatproperly.item.reward;

import com.touhoumaid.eatproperly.entity.FloorDecorEntity;
import com.touhoumaid.eatproperly.entity.FloorDecorPlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Places a floor decoration whose front faces the placing player.
 */
public class FloorDecorRewardItem extends BoundRewardItem {
    public FloorDecorRewardItem(Properties properties, int loreLineCount) {
        super(properties, loreLineCount);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }

        BlockPos standOn = context.getClickedPos();
        BlockPos airAt = standOn.above();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();

        if (!level.getBlockState(airAt).canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        if (player != null && !player.mayUseItemAt(airAt, Direction.UP, stack)) {
            return InteractionResult.FAIL;
        }

        float yaw = FloorDecorPlacement.yawFacingPlayer(player != null ? player.getYRot() : 0.0F);

        if (!level.isClientSide) {
            FloorDecorEntity decor = new FloorDecorEntity(level, standOn, yaw, stack);
            level.addFreshEntity(decor);
            level.gameEvent(player, GameEvent.ENTITY_PLACE, decor.position());
            decor.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
