package com.touhoumaid.eatproperly.item.reward;

import com.github.tartaricacid.touhoulittlemaid.config.subconfig.MaidConfig;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * Right-click to summon the bound source maid to the player's side. Same-dimension only: the
 * lookup is scoped to the player's current {@link ServerLevel}, so a maid in another dimension
 * (or unloaded/removed/dead) simply won't be found there. Does not consume the bell; 60s cooldown
 * enforced by the vanilla item-cooldown check before {@link #use} is even invoked.
 */
public class MealBellItem extends BoundRewardItem {
    private static final int COOLDOWN_TICKS = 1200; // 60s
    private static final double SUMMON_DISTANCE = 2.0;

    public MealBellItem(Properties properties, int loreLineCount) {
        super(properties, loreLineCount);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        CompoundTag rewardTag = stack.getTagElement(RewardTooltipText.REWARD_TAG);
        if (rewardTag == null || !rewardTag.hasUUID(RewardTooltipText.SOURCE_MAID_UUID)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.touhou_maid_eat_properly.meal_bell_unbound"), true);
            return InteractionResultHolder.fail(stack);
        }

        UUID maidUuid = rewardTag.getUUID(RewardTooltipText.SOURCE_MAID_UUID);
        ServerLevel serverLevel = (ServerLevel) level;
        Entity found = serverLevel.getEntity(maidUuid);
        if (!(found instanceof EntityMaid maid) || !maid.isAlive()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.touhou_maid_eat_properly.meal_bell_maid_unavailable"), true);
            return InteractionResultHolder.fail(stack);
        }

        summonMaid(serverLevel, maid, serverPlayer);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResultHolder.success(stack);
    }

    private static void summonMaid(ServerLevel level, EntityMaid maid, ServerPlayer player) {
        Vec3 spot = summonSpot(level, maid, player);
        maid.getNavigation().stop();
        maid.teleportTo(spot.x, spot.y, spot.z);
        float facePlayerYaw = player.getYRot() + 180.0F;
        maid.setYRot(facePlayerYaw);
        maid.setYHeadRot(facePlayerYaw);
        maid.setXRot(0.0F);
        maid.setOldPosAndRot();
        // Home mode → follow (same as TLM SwitchFollowStateFunction enabling follow).
        if (maid.isHomeModeEnable()) {
            maid.restrictTo(BlockPos.ZERO, MaidConfig.MAID_NON_HOME_RANGE.get());
            maid.setHomeModeEnable(false);
        }
    }

    /**
     * A point a couple blocks in front of the player's current facing, at the player's own height —
     * unless that spot would drop the maid into a wall/lava/void, in which case she lands right on
     * the player's own (necessarily safe, since the player is standing there) position instead.
     */
    private static Vec3 summonSpot(ServerLevel level, EntityMaid maid, ServerPlayer player) {
        Vec3 preferred = preferredSpot(player);
        return isSafeSpot(level, maid, preferred) ? preferred : player.position();
    }

    private static Vec3 preferredSpot(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        double x = look.x;
        double z = look.z;
        double horizontalLenSq = x * x + z * z;
        if (horizontalLenSq < 1.0E-4) {
            x = 0.0;
            z = 1.0;
            horizontalLenSq = 1.0;
        }
        double scale = SUMMON_DISTANCE / Math.sqrt(horizontalLenSq);
        return new Vec3(player.getX() + x * scale, player.getY(), player.getZ() + z * scale);
    }

    /** Rejects spots below the world, colliding with blocks, or sitting in lava. */
    private static boolean isSafeSpot(ServerLevel level, EntityMaid maid, Vec3 pos) {
        if (pos.y <= level.getMinBuildHeight()) {
            return false;
        }
        AABB box = maid.getBoundingBox().move(pos.subtract(maid.position()));
        if (!level.noCollision(maid, box)) {
            return false;
        }
        return !level.getFluidState(BlockPos.containing(pos)).is(FluidTags.LAVA);
    }
}
