package com.touhoumaid.eatproperly.milestone;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Per-maid milestone claim state on PersistentData root {@code TouhouMaidEatProperly}.
 */
public final class MilestoneData {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String PENDING = "MilestonePending";
    private static final String CLAIMED_LEVELS = "MilestoneClaimedLevels";
    private static final String OBTAINED = "MilestoneObtained";
    private static final String LEVEL = "Level";
    private static final String ITEM = "Item";

    private MilestoneData() {
    }

    private static CompoundTag root(EntityMaid maid) {
        CompoundTag persistent = maid.getPersistentData();
        if (!persistent.contains(ROOT, Tag.TAG_COMPOUND)) {
            CompoundTag created = new CompoundTag();
            persistent.put(ROOT, created);
            return created;
        }
        return persistent.getCompound(ROOT);
    }

    public static ListTag getPendingClaims(EntityMaid maid) {
        return root(maid).getList(PENDING, Tag.TAG_COMPOUND).copy();
    }

    public static List<PendingClaim> listPendingClaims(EntityMaid maid) {
        return parsePendingClaims(getPendingClaims(maid));
    }

    private static List<PendingClaim> parsePendingClaims(ListTag list) {
        List<PendingClaim> out = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int level = entry.getInt(LEVEL);
            String item = entry.getString(ITEM);
            if (item.isEmpty()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(item);
            if (id == null) {
                id = RewardPools.id(item);
            }
            out.add(new PendingClaim(level, id));
        }
        return Collections.unmodifiableList(out);
    }

    public static boolean hasPendingLevel(EntityMaid maid, int level) {
        for (PendingClaim claim : listPendingClaims(maid)) {
            if (claim.level() == level) {
                return true;
            }
        }
        return false;
    }

    public static Optional<ResourceLocation> getPendingItem(EntityMaid maid, int level) {
        for (PendingClaim claim : listPendingClaims(maid)) {
            if (claim.level() == level) {
                return Optional.of(claim.itemId());
            }
        }
        return Optional.empty();
    }

    /** Claimed items plus items locked in other pending milestones (for pool de-dupe). */
    public static Set<ResourceLocation> getExcludedItems(EntityMaid maid) {
        Set<ResourceLocation> excluded = new LinkedHashSet<>(getObtainedItems(maid));
        for (PendingClaim claim : listPendingClaims(maid)) {
            excluded.add(claim.itemId());
        }
        return Collections.unmodifiableSet(excluded);
    }

    public static void addPending(EntityMaid maid, int level, ResourceLocation itemId) {
        if (hasPendingLevel(maid, level) || hasClaimedLevel(maid, level)) {
            return;
        }
        if (getExcludedItems(maid).contains(itemId)) {
            return;
        }
        CompoundTag tag = root(maid);
        ListTag list = tag.getList(PENDING, Tag.TAG_COMPOUND);
        CompoundTag entry = new CompoundTag();
        entry.putInt(LEVEL, level);
        entry.putString(ITEM, itemId.toString());
        list.add(entry);
        tag.put(PENDING, list);
    }

    public static boolean hasClaimedLevel(EntityMaid maid, int level) {
        for (int claimed : getClaimedLevels(maid)) {
            if (claimed == level) {
                return true;
            }
        }
        return false;
    }

    public static int[] getClaimedLevels(EntityMaid maid) {
        CompoundTag tag = root(maid);
        if (!tag.contains(CLAIMED_LEVELS, Tag.TAG_INT_ARRAY)) {
            return new int[0];
        }
        return tag.getIntArray(CLAIMED_LEVELS);
    }

    public static Set<ResourceLocation> getObtainedItems(EntityMaid maid) {
        ListTag list = root(maid).getList(OBTAINED, Tag.TAG_STRING);
        Set<ResourceLocation> out = new LinkedHashSet<>();
        for (int i = 0; i < list.size(); i++) {
            String raw = list.getString(i);
            ResourceLocation id = ResourceLocation.tryParse(raw);
            if (id == null) {
                id = RewardPools.id(raw);
            }
            out.add(id);
        }
        return Collections.unmodifiableSet(out);
    }

    public static Set<String> getObtainedPaths(EntityMaid maid) {
        Set<String> paths = new HashSet<>();
        for (ResourceLocation id : getObtainedItems(maid)) {
            if (EatProperlyMod.MOD_ID.equals(id.getNamespace())) {
                paths.add(id.getPath());
            } else {
                paths.add(id.toString());
            }
        }
        return paths;
    }

    public static void markClaimed(EntityMaid maid, int level, ResourceLocation itemId) {
        removePendingLevel(maid, level);
        addClaimedLevel(maid, level);
        addObtained(maid, itemId);
    }

    /** Pool empty: mark level claimed without an item so it will not re-roll. */
    public static void markClaimedEmpty(EntityMaid maid, int level) {
        removePendingLevel(maid, level);
        addClaimedLevel(maid, level);
    }

    private static void removePendingLevel(EntityMaid maid, int level) {
        CompoundTag tag = root(maid);
        ListTag list = tag.getList(PENDING, Tag.TAG_COMPOUND);
        ListTag next = new ListTag();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (entry.getInt(LEVEL) != level) {
                next.add(entry);
            }
        }
        tag.put(PENDING, next);
    }

    private static void addClaimedLevel(EntityMaid maid, int level) {
        if (hasClaimedLevel(maid, level)) {
            return;
        }
        int[] old = getClaimedLevels(maid);
        int[] next = new int[old.length + 1];
        System.arraycopy(old, 0, next, 0, old.length);
        next[old.length] = level;
        root(maid).put(CLAIMED_LEVELS, new IntArrayTag(next));
    }

    private static void addObtained(EntityMaid maid, ResourceLocation itemId) {
        CompoundTag tag = root(maid);
        ListTag list = tag.getList(OBTAINED, Tag.TAG_STRING);
        String asString = itemId.toString();
        for (int i = 0; i < list.size(); i++) {
            if (asString.equals(list.getString(i))) {
                return;
            }
        }
        list.add(StringTag.valueOf(asString));
        tag.put(OBTAINED, list);
    }

    public record PendingClaim(int level, ResourceLocation itemId) {
    }
}
