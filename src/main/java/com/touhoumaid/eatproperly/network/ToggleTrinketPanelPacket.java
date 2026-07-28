package com.touhoumaid.eatproperly.network;

import com.touhoumaid.eatproperly.trinket.PlayerTrinketPanel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server: set survival-inventory left trinket panel open state. */
public record ToggleTrinketPanelPacket(boolean open) {
    public static void encode(ToggleTrinketPanelPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.open);
    }

    public static ToggleTrinketPanelPacket decode(FriendlyByteBuf buf) {
        return new ToggleTrinketPanelPacket(buf.readBoolean());
    }

    public static void handle(ToggleTrinketPanelPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                PlayerTrinketPanel.setOpen(player, packet.open());
            }
        });
        ctx.setPacketHandled(true);
    }
}
