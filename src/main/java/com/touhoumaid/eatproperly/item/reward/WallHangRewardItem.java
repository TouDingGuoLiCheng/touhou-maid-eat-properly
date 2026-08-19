package com.touhoumaid.eatproperly.item.reward;

import com.touhoumaid.eatproperly.entity.WallHangEntity;
import com.touhoumaid.eatproperly.entity.WallHangPlacement;
import com.touhoumaid.eatproperly.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Bound reward that can be hung on a vertical wall like a vanilla painting.
 * Banner (1×2): click the upper of the two support blocks; entity pos is the lower air cell.
 */
public class WallHangRewardItem extends BoundRewardItem {
    public WallHangRewardItem(Properties properties, int loreLineCount) {
        super(properties, loreLineCount);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        BlockPos supportPos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (face.getAxis().isVertical() || (player != null && !player.mayUseItemAt(supportPos.relative(face), face, stack))) {
            return InteractionResult.FAIL;
        }

        boolean banner = stack.is(ModItems.BANNER_HONEST_BOSS.get());
        if (banner) {
            if (!WallHangPlacement.canHangTwoHigh(
                    isHangSupport(context.getLevel(), supportPos),
                    isHangSupport(context.getLevel(), supportPos.below()))) {
                return InteractionResult.FAIL;
            }
        }

        int[] hang = WallHangPlacement.hangPos(
                supportPos.getX(), supportPos.getY(), supportPos.getZ(),
                face.getStepX(), face.getStepY(), face.getStepZ(),
                banner);
        BlockPos hangPos = new BlockPos(hang[0], hang[1], hang[2]);

        Level level = context.getLevel();
        WallHangEntity hanging = new WallHangEntity(level, hangPos, face, stack);
        if (!hanging.survives()) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide) {
            hanging.playPlacementSound();
            level.gameEvent(player, GameEvent.ENTITY_PLACE, hanging.position());
            level.addFreshEntity(hanging);
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean isHangSupport(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isSolid() || DiodeBlock.isDiode(state);
    }
}
