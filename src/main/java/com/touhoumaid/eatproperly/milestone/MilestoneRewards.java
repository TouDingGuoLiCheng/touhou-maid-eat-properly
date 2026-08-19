package com.touhoumaid.eatproperly.milestone;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.food.MaidSpeech;
import com.touhoumaid.eatproperly.item.reward.HandwrittenLetterItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Build claim stacks and query milestone UI state.
 */
public final class MilestoneRewards {
    public static final int MODE_WAITING = 0;
    public static final int MODE_CLAIMABLE = 1;
    public static final int MODE_BOND = 2;

    private static final String CHAT_ANNOUNCED = "MilestoneChatAnnounced";
    private static final String LAST_BUBBLE = "MilestoneLastBubbleGameTime";

    private MilestoneRewards() {
    }

    public static int uiMode(EntityMaid maid) {
        if (!MilestoneData.listPendingClaims(maid).isEmpty()) {
            return MODE_CLAIMABLE;
        }
        if (isBondComplete(maid)) {
            return MODE_BOND;
        }
        return MODE_WAITING;
    }

    public static boolean isBondComplete(EntityMaid maid) {
        for (int level : new int[]{5, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100}) {
            if (!MilestoneData.hasClaimedLevel(maid, level)) {
                return false;
            }
        }
        return true;
    }

    public static ItemStack previewStack(EntityMaid maid) {
        return MilestoneData.listPendingClaims(maid).stream().findFirst()
                .map(claim -> createBoundReward(claim.itemId(), maid))
                .orElse(ItemStack.EMPTY);
    }

    public static int previewLevel(EntityMaid maid) {
        return MilestoneData.listPendingClaims(maid).stream().findFirst()
                .map(MilestoneData.PendingClaim::level)
                .orElse(-1);
    }

    public static ItemStack createBoundReward(ResourceLocation id, EntityMaid maid) {
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null || item == Items.AIR) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(item);
        CompoundTag tag = stack.getOrCreateTagElement("EatProperlyReward");
        tag.putUUID("SourceMaidUuid", maid.getUUID());
        tag.putString("SourceMaidName", maid.getDisplayName().getString());
        if (item instanceof HandwrittenLetterItem) {
            HandwrittenLetterItem.ensurePages(stack);
        }
        return stack;
    }

    public static boolean wasChatAnnounced(EntityMaid maid, ResourceLocation itemId) {
        ListTag list = root(maid).getList(CHAT_ANNOUNCED, Tag.TAG_STRING);
        String key = itemId.toString();
        for (int i = 0; i < list.size(); i++) {
            if (key.equals(list.getString(i))) {
                return true;
            }
        }
        return false;
    }

    public static void markChatAnnounced(EntityMaid maid, ResourceLocation itemId) {
        if (wasChatAnnounced(maid, itemId)) {
            return;
        }
        CompoundTag tag = root(maid);
        ListTag list = tag.getList(CHAT_ANNOUNCED, Tag.TAG_STRING);
        list.add(StringTag.valueOf(itemId.toString()));
        tag.put(CHAT_ANNOUNCED, list);
    }

    public static long getLastBubbleGameTime(EntityMaid maid) {
        return root(maid).getLong(LAST_BUBBLE);
    }

    public static void setLastBubbleGameTime(EntityMaid maid, long time) {
        root(maid).putLong(LAST_BUBBLE, time);
    }

    /** Repeating bubble while pending rewards remain (chat already announced once per item). */
    public static void tickBubbleNag(EntityMaid maid) {
        if (MilestoneData.listPendingClaims(maid).isEmpty()) {
            return;
        }
        long now = maid.level().getGameTime();
        long last = getLastBubbleGameTime(maid);
        // Every 2 minutes of game time while something is claimable.
        if (last > 0L && now - last < 2400L) {
            return;
        }
        setLastBubbleGameTime(maid, now);
        String bubble = MaidSpeech.formatMilestoneGiftBubble(maid, maid.getRandom());
        if (!bubble.isEmpty()) {
            MaidSpeech.bubbleToOwnerIfNear(maid, bubble);
        }
    }

    /**
     * Claim the first pending reward for the owner. Returns the granted stack (may be empty on failure).
     */
    public static ItemStack claimFirst(EntityMaid maid, ServerPlayer player, int expectedLevel) {
        var pending = MilestoneData.listPendingClaims(maid);
        if (pending.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (maid.getOwnerUUID() == null || !maid.getOwnerUUID().equals(player.getUUID())) {
            return ItemStack.EMPTY;
        }
        MilestoneData.PendingClaim claim = pending.get(0);
        if (!ClaimLevelGuard.matchesPendingHead(claim.level(), expectedLevel)) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = createBoundReward(claim.itemId(), maid);
        if (stack.isEmpty()) {
            MilestoneData.markClaimedEmpty(maid, claim.level());
            return ItemStack.EMPTY;
        }
        MilestoneData.markClaimed(maid, claim.level(), claim.itemId());
        if (!player.addItem(stack.copy())) {
            player.drop(stack.copy(), false);
        }
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.7F, 1.0F);
        return stack;
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains("TouhouMaidEatProperly", Tag.TAG_COMPOUND)) {
            persistent.put("TouhouMaidEatProperly", new CompoundTag());
        }
        return persistent.getCompound("TouhouMaidEatProperly");
    }
}
