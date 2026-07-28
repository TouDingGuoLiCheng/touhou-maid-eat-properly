package com.touhoumaid.eatproperly.food;

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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Maid speech to owner: bubble always shows immediately; chat-bar text only during
 * 08:00–20:00 (TLM {@code getDayTime()} clock). Outside that window, public chat is
 * deferred to a random moment in the next 08:00–20:00 window.
 */
public final class MaidSpeech {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String PENDING_CHATS = "PendingOwnerChats";
    private static final String MSG = "Msg";
    /** Absolute {@code level.getDayTime()} when this entry should deliver. */
    private static final String AT = "At";
    private static final int MAX_PENDING = 8;

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

    public static String formatHunger(EntityMaid maid, RandomSource random) {
        List<? extends String> templates = EatProperlyConfig.HUNGER_SPEECH.get();
        if (templates == null || templates.isEmpty()) {
            return "";
        }
        String template = templates.get(random.nextInt(templates.size()));
        String maidName = maid.getDisplayName().getString();
        return template.replace("{maid}", maidName);
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

    public static void speakToOwner(EntityMaid maid, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        bubbleOnly(maid, message);

        long now = maid.level().getDayTime();
        if (MaidWorldTime.isChatWindow(now)) {
            sendChatToOwner(maid, message);
        } else {
            enqueuePendingChat(maid, message, nextDeferredDeliveryTime(now, maid.getRandom()));
        }
    }

    /** Bubble only — no chat bar (used for repeating milestone nags). */
    public static void bubbleOnly(EntityMaid maid, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        maid.getChatBubbleManager().addChatBubble(TextChatBubbleData.type2(Component.literal(message)));
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

        long now = maid.level().getDayTime();
        ListTag kept = new ListTag();
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
            if (!MaidWorldTime.isChatWindow(now)) {
                entry.putLong(AT, nextDeferredDeliveryTime(now, maid.getRandom()));
                kept.add(entry);
                continue;
            }
            if (!(maid.getOwner() instanceof ServerPlayer)) {
                kept.add(entry);
                continue;
            }
            sendChatToOwner(maid, msg);
        }
        root.put(PENDING_CHATS, kept);
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

    static long nextDeferredDeliveryTime(long dayTime, RandomSource random) {
        int offset = random.nextInt(MaidWorldTime.CHAT_WINDOW_LENGTH);
        return MaidWorldTime.nextRandomDeliveryDayTime(dayTime, offset);
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
