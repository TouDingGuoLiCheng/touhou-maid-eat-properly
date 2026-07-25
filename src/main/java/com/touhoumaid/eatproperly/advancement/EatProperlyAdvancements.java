package com.touhoumaid.eatproperly.advancement;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class EatProperlyAdvancements {
    private static final String PLAYER_ROOT = "TouhouMaidEatProperly";
    private static final String REQUEST_COUNT = "RequestCompleteCount";
    private static final String LUNCHBOX_COUNT = "LunchboxDeliverCount";

    public static final ResourceLocation ROOT = id("root");
    public static final ResourceLocation FIRST_REQUEST = id("first_request");
    public static final ResourceLocation REQUEST_100 = id("request_100");
    public static final ResourceLocation FIRST_LUNCHBOX = id("first_lunchbox_delivery");
    public static final ResourceLocation LUNCHBOX_100 = id("lunchbox_100");

    public static final int[] FAVOR_LEVELS = {5, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};

    private EatProperlyAdvancements() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, path);
    }

    public static ResourceLocation favorLevel(int level) {
        return id("favor_level_" + level);
    }

    public static void onRequestCompleted(ServerPlayer player) {
        grant(player, ROOT);
        grant(player, FIRST_REQUEST);
        int count = incrementRequestCount(player);
        if (count >= 100) {
            grant(player, REQUEST_100);
        }
    }

    public static void onLunchboxDelivered(ServerPlayer player) {
        grant(player, ROOT);
        grant(player, FIRST_LUNCHBOX);
        int count = incrementLunchboxCount(player);
        if (count >= 100) {
            grant(player, LUNCHBOX_100);
        }
    }

    public static void onFavorLevelReached(ServerPlayer player, int newLevel) {
        if (newLevel < FAVOR_LEVELS[0]) {
            return;
        }
        grant(player, ROOT);
        for (int level : FAVOR_LEVELS) {
            if (newLevel >= level) {
                grant(player, favorLevel(level));
            }
        }
    }

    public static int getRequestCount(ServerPlayer player) {
        return playerRoot(player).getInt(REQUEST_COUNT);
    }

    private static int incrementRequestCount(ServerPlayer player) {
        CompoundTag tag = playerRoot(player);
        int next = tag.getInt(REQUEST_COUNT) + 1;
        tag.putInt(REQUEST_COUNT, next);
        return next;
    }

    private static int incrementLunchboxCount(ServerPlayer player) {
        CompoundTag tag = playerRoot(player);
        int next = tag.getInt(LUNCHBOX_COUNT) + 1;
        tag.putInt(LUNCHBOX_COUNT, next);
        return next;
    }

    private static CompoundTag playerRoot(ServerPlayer player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(PLAYER_ROOT)) {
            persistent.put(PLAYER_ROOT, new CompoundTag());
        }
        return persistent.getCompound(PLAYER_ROOT);
    }

    public static void grant(ServerPlayer player, ResourceLocation advancementId) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(advancementId);
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
