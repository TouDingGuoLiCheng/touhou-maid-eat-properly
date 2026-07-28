package com.touhoumaid.eatproperly.trinket;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Survival-inventory left trinket panel open state.
 * Slot activity is gated via {@link net.minecraft.world.inventory.Slot#isActive()}.
 */
public final class PlayerTrinketPanel {
    private static final String ROOT = "TouhouMaidEatProperly";
    private static final String PANEL_OPEN = "TrinketPanelOpen";

    /** Visible left-rail slot coords (menu-relative). */
    public static final int SLOT_X = -26;
    public static final int SLOT_Y0 = 8;
    public static final int SLOT_STRIDE = 18;

    private PlayerTrinketPanel() {
    }

    public static boolean isOpen(Player player) {
        CompoundTag root = root(player);
        return root.contains(PANEL_OPEN, Tag.TAG_BYTE) && root.getBoolean(PANEL_OPEN);
    }

    public static void setOpen(Player player, boolean open) {
        root(player).putBoolean(PANEL_OPEN, open);
    }

    public static void toggle(Player player) {
        setOpen(player, !isOpen(player));
    }

    /**
     * Slot activity check safe for both sides. On the physical client, prefers the local fold
     * flag so the UI feels instant; dedicated server uses player NBT only.
     */
    public static boolean isSlotActive(Player player) {
        if (FMLEnvironment.dist == Dist.CLIENT && player.level().isClientSide()) {
            return ClientPanelOpen.isOpen();
        }
        return isOpen(player);
    }

    private static CompoundTag root(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT, new CompoundTag());
        }
        return persistent.getCompound(ROOT);
    }

    /** Tiny bridge so {@link PlayerTrinketPanel} does not hard-link the client class at verify time on dedicated servers. */
    static final class ClientPanelOpen {
        private ClientPanelOpen() {
        }

        static boolean isOpen() {
            return com.touhoumaid.eatproperly.client.TrinketPanelClient.isOpen();
        }
    }
}
