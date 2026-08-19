package com.touhoumaid.eatproperly.food;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.MaidSchedule;
import com.github.tartaricacid.touhoulittlemaid.entity.chatbubble.implement.TextChatBubbleData;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Maid speech to owner.
 * <ul>
 *   <li>Most lines: bubble only, and only when the owner is within {@link #OWNER_SPEECH_RANGE}.</li>
 *   <li>Thanks (and explicit {@link #speakThanksToOwner}): bubble + public chat as before.</li>
 * </ul>
 */
public final class MaidSpeech {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String PENDING_CHATS = "PendingOwnerChats";
    private static final String MSG = "Msg";
    /** Absolute {@code level.getDayTime()} when this entry should deliver. */
    private static final String AT = "At";
    private static final int MAX_PENDING = 8;

    /** Owner must be within this many blocks for non-thanks speech bubbles. */
    public static final double OWNER_SPEECH_RANGE = 16.0D;
    /** Request nag cooldown while loaded and owner-nearby (30 seconds). */
    public static final int REQUEST_NAG_COOLDOWN_TICKS = 20 * 30;
    /** Half of TLM {@code TextChatBubbleData.type2} default (300). */
    public static final int BUBBLE_EXIST_TICKS = 150;
    private static final ResourceLocation TYPE_2_BG =
            new ResourceLocation("touhou_little_maid:textures/entity/chat_bubble/type2.png");

    private MaidSpeech() {
    }

    public static String formatRequest(ItemStack food, RandomSource random) {
        return formatFood(EatProperlyConfig.REQUEST_SPEECH.get(), food, random);
    }

    public static String formatThanks(ItemStack food, RandomSource random) {
        return formatFood(EatProperlyConfig.THANKS_SPEECH.get(), food, random);
    }

    public static String formatAbandon(ItemStack food, RandomSource random) {
        return formatFood(EatProperlyConfig.ABANDON_SPEECH.get(), food, random);
    }

    public static String formatHypnosis(RandomSource random) {
        List<? extends String> templates = EatProperlyConfig.HYPNOSIS_SPEECH.get();
        if (templates == null || templates.isEmpty()) {
            return "";
        }
        return templates.get(random.nextInt(templates.size()));
    }

    public static String formatEmptyMenu(RandomSource random) {
        List<? extends String> templates = EatProperlyConfig.EMPTY_MENU_SPEECH.get();
        if (templates == null || templates.isEmpty()) {
            return "";
        }
        return templates.get(random.nextInt(templates.size()));
    }

    public static String formatOrderBookRematch(ItemStack food, RandomSource random) {
        return formatFood(EatProperlyConfig.ORDER_BOOK_REMATCH_SPEECH.get(), food, random);
    }

    public static String formatLowSatietyBeg(ItemStack food, RandomSource random) {
        return formatFood(EatProperlyConfig.LOW_SATIETY_BEG_SPEECH.get(), food, random);
    }

    public static String formatLowSatietyBegThanks(ItemStack food, RandomSource random) {
        return formatFood(EatProperlyConfig.LOW_SATIETY_BEG_THANKS_SPEECH.get(), food, random);
    }

    /** Thanks when the given food differs from the one that started begging. */
    public static String formatLowSatietyBegThanksOther(ItemStack food, RandomSource random) {
        return formatFood(EatProperlyConfig.LOW_SATIETY_BEG_THANKS_OTHER_SPEECH.get(), food, random);
    }

    public static String formatOutOfPoolRefuse(RandomSource random) {
        return pickPlain(EatProperlyConfig.OUT_OF_POOL_REFUSE_SPEECH.get(), random);
    }

    public static String formatOutOfPoolAngry(RandomSource random) {
        return pickPlain(EatProperlyConfig.OUT_OF_POOL_ANGRY_SPEECH.get(), random);
    }

    public static String formatOutOfPoolForgive(RandomSource random) {
        return pickPlain(EatProperlyConfig.OUT_OF_POOL_FORGIVE_SPEECH.get(), random);
    }

    private static String pickPlain(List<? extends String> templates, RandomSource random) {
        if (templates == null || templates.isEmpty()) {
            return "";
        }
        return templates.get(random.nextInt(templates.size()));
    }

    public static String formatHunger(EntityMaid maid, RandomSource random) {
        List<? extends String> templates = EatProperlyConfig.HUNGER_SPEECH.get();
        if (templates == null || templates.isEmpty()) {
            return "";
        }
        String template = templates.get(random.nextInt(templates.size()));
        String maidName = maid.getDisplayName().getString();
        return template.replace("{maid}", maidName);
    }

    public static String formatSnackRaid(EntityMaid maid, RandomSource random) {
        return formatMaidTemplate(EatProperlyConfig.SNACK_RAID_SPEECH.get(), maid, random);
    }

    public static String formatSnackRaidApproach(EntityMaid maid, RandomSource random) {
        return formatMaidTemplate(EatProperlyConfig.SNACK_RAID_APPROACH_SPEECH.get(), maid, random);
    }

    public static String formatFoodPermitApproach(EntityMaid maid, RandomSource random) {
        return formatMaidTemplate(EatProperlyConfig.FOOD_PERMIT_APPROACH_SPEECH.get(), maid, random);
    }

    public static String formatFoodPermit(EntityMaid maid, RandomSource random) {
        return formatMaidTemplate(EatProperlyConfig.FOOD_PERMIT_SPEECH.get(), maid, random);
    }

    public static String formatMilestoneGift(EntityMaid maid, RandomSource random) {
        return formatMaidTemplate(EatProperlyConfig.MILESTONE_GIFT_SPEECH.get(), maid, random);
    }

    public static String formatMilestoneGiftBubble(EntityMaid maid, RandomSource random) {
        return formatMaidTemplate(EatProperlyConfig.MILESTONE_GIFT_BUBBLE.get(), maid, random);
    }

    public static String formatMilestoneEmpty(EntityMaid maid, RandomSource random) {
        return formatMaidTemplate(EatProperlyConfig.MILESTONE_EMPTY_SPEECH.get(), maid, random);
    }

    private static String formatMaidTemplate(List<? extends String> templates, EntityMaid maid, RandomSource random) {
        if (templates == null || templates.isEmpty()) {
            return "";
        }
        String template = templates.get(random.nextInt(templates.size()));
        return template.replace("{maid}", maid.getDisplayName().getString());
    }

    public static boolean isOwnerNearby(EntityMaid maid) {
        LivingEntity owner = maid.getOwner();
        if (!(owner instanceof ServerPlayer) || owner.level() != maid.level()) {
            return false;
        }
        return OwnerProximityPolicy.isWithinRange(maid.distanceToSqr(owner), OWNER_SPEECH_RANGE);
    }

    /**
     * Bubble-only line if the owner is within range. No public chat.
     *
     * @return true when the bubble was shown
     */
    public static boolean bubbleToOwnerIfNear(EntityMaid maid, String message) {
        if (message == null || message.isEmpty() || !isOwnerNearby(maid)) {
            return false;
        }
        bubbleOnly(maid, message);
        return true;
    }

    /** Food Permit approach/success: ambient maid bubble (optional owner-near gate). */
    public static void bubbleFoodPermitTrip(EntityMaid maid, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        if (FoodPermitSpeechPolicy.requireOwnerNearbyForBubble()) {
            bubbleToOwnerIfNear(maid, message);
            return;
        }
        bubbleOnly(maid, message);
    }

    /**
     * Thanks / delivery gratitude: bubble + public chat (schedule window / defer rules apply).
     */
    public static void speakThanksToOwner(EntityMaid maid, String message) {
        speakWithPublicChat(maid, message, false);
    }

    /**
     * @deprecated Prefer {@link #bubbleToOwnerIfNear} or {@link #speakThanksToOwner}.
     */
    @Deprecated
    public static void speakToOwner(EntityMaid maid, String message) {
        bubbleToOwnerIfNear(maid, message);
    }

    /**
     * Immediate bubble + public chat (no defer). Used rarely; prefer thanks or bubble-near.
     */
    public static void speakToOwnerNow(EntityMaid maid, String message) {
        speakWithPublicChat(maid, message, true);
    }

    private static void speakWithPublicChat(EntityMaid maid, String message, boolean forceImmediate) {
        if (message == null || message.isEmpty()) {
            return;
        }
        bubbleOnly(maid, message);

        MaidChatWindow.Shift shift = shiftOf(maid);
        long now = maid.level().getDayTime();
        boolean inWindow = MaidChatWindow.canPublicChat(shift, now);
        if (MaidSpeechDeferPolicy.shouldDeferPublicChat(forceImmediate, inWindow)) {
            enqueuePendingChat(maid, message, nextDeferredDeliveryTime(shift, now, maid.getRandom()));
        } else {
            sendChatToOwner(maid, message);
        }
    }

    /** Bubble only — no chat bar and no proximity check. */
    public static void bubbleOnly(EntityMaid maid, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        maid.getChatBubbleManager().addChatBubble(
                TextChatBubbleData.create(BUBBLE_EXIST_TICKS, Component.literal(message), TYPE_2_BG, 0));
    }

    /** Deliver any deferred chat-bar messages whose delivery time has arrived. */
    public static void tickPendingChat(EntityMaid maid) {
        CompoundTag root = root(maid);
        if (!root.contains(PENDING_CHATS, Tag.TAG_LIST)) {
            return;
        }
        ListTag list = root.getList(PENDING_CHATS, Tag.TAG_COMPOUND);
        if (list.isEmpty()) {
            return;
        }

        MaidChatWindow.Shift shift = shiftOf(maid);
        long now = maid.level().getDayTime();
        ListTag kept = new ListTag();
        // At most one deferred public line per tick-pass to avoid chat dumps.
        boolean sentOne = false;
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            long at = entry.getLong(AT);
            String msg = entry.getString(MSG);
            if (msg.isEmpty()) {
                continue;
            }
            if (now < at) {
                kept.add(entry);
                continue;
            }
            if (!MaidChatWindow.canPublicChat(shift, now)) {
                entry.putLong(AT, nextDeferredDeliveryTime(shift, now, maid.getRandom()));
                kept.add(entry);
                continue;
            }
            if (!(maid.getOwner() instanceof ServerPlayer) || sentOne) {
                if (sentOne && now >= at && MaidChatWindow.canPublicChat(shift, now)) {
                    // Due but we already sent one this pass — push later in the awake window.
                    int window = MaidChatWindow.windowLength(shift);
                    int offset = Math.floorMod(
                            (int) (now % window) + MaidLoadedClock.DEFER_STAGGER_DAY_TICKS, window);
                    entry.putLong(AT, MaidChatWindow.nextRandomDeliveryDayTime(shift, now, offset));
                }
                kept.add(entry);
                continue;
            }
            sendChatToOwner(maid, msg);
            sentOne = true;
        }
        root.put(PENDING_CHATS, kept);
    }

    /**
     * After a chunk-unload gap: keep pending public chats but restagger their delivery
     * times so they do not all fire together when the maid loads again.
     */
    public static void reschedulePendingAfterGap(EntityMaid maid) {
        CompoundTag root = root(maid);
        if (!root.contains(PENDING_CHATS, Tag.TAG_LIST)) {
            return;
        }
        ListTag list = root.getList(PENDING_CHATS, Tag.TAG_COMPOUND);
        if (list.isEmpty()) {
            return;
        }
        MaidChatWindow.Shift shift = shiftOf(maid);
        long now = maid.level().getDayTime();
        int window = MaidChatWindow.windowLength(shift);
        int base = maid.getRandom().nextInt(Math.max(1, window));
        int[] offsets = LoadedTimePolicy.staggeredOffsets(
                list.size(), window, base, MaidLoadedClock.DEFER_STAGGER_DAY_TICKS);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.getString(MSG).isEmpty()) {
                continue;
            }
            entry.putLong(AT, MaidChatWindow.nextRandomDeliveryDayTime(shift, now, offsets[i]));
        }
        root.put(PENDING_CHATS, list);
    }

    static MaidChatWindow.Shift shiftOf(EntityMaid maid) {
        MaidSchedule schedule = maid.getSchedule();
        if (schedule == null) {
            return MaidChatWindow.Shift.DAY;
        }
        return switch (schedule) {
            case NIGHT -> MaidChatWindow.Shift.NIGHT;
            case ALL -> MaidChatWindow.Shift.ALL;
            default -> MaidChatWindow.Shift.DAY;
        };
    }

    private static void sendChatToOwner(EntityMaid maid, String message) {
        LivingEntity owner = maid.getOwner();
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }
        MutableComponent msg = Component.literal("<")
                .append(maid.getDisplayName())
                .append(">")
                .append(CommonComponents.SPACE)
                .append(message)
                .withStyle(ChatFormatting.GRAY);
        player.sendSystemMessage(msg);
    }

    private static void enqueuePendingChat(EntityMaid maid, String message, long deliverAt) {
        CompoundTag root = root(maid);
        ListTag list = root.contains(PENDING_CHATS, Tag.TAG_LIST)
                ? root.getList(PENDING_CHATS, Tag.TAG_COMPOUND)
                : new ListTag();
        while (list.size() >= MAX_PENDING) {
            list.remove(0);
        }
        CompoundTag entry = new CompoundTag();
        entry.putString(MSG, message);
        entry.putLong(AT, deliverAt);
        list.add(entry);
        root.put(PENDING_CHATS, list);
    }

    static long nextDeferredDeliveryTime(MaidChatWindow.Shift shift, long dayTime, RandomSource random) {
        int offset = random.nextInt(MaidChatWindow.windowLength(shift));
        return MaidChatWindow.nextRandomDeliveryDayTime(shift, dayTime, offset);
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    private static String formatFood(List<? extends String> templates, ItemStack food, RandomSource random) {
        if (templates == null || templates.isEmpty()) {
            return "";
        }
        String template = templates.get(random.nextInt(templates.size()));
        String foodName = food.isEmpty() ? "?" : food.getHoverName().getString();
        return template.replace("{food}", foodName);
    }
}
