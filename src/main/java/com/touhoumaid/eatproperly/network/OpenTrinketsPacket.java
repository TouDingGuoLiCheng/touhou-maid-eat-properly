package com.touhoumaid.eatproperly.network;

import com.touhoumaid.eatproperly.inventory.TrinketContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent when the player clicks the "Trinkets" button on the Eat Properly screen. */
public record OpenTrinketsPacket() {
    public static void encode(OpenTrinketsPacket packet, FriendlyByteBuf buf) {
    }

    public static OpenTrinketsPacket decode(FriendlyByteBuf buf) {
        return new OpenTrinketsPacket();
    }

    public static void handle(OpenTrinketsPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            TrinketContainer.open(player);
        });
        ctx.setPacketHandled(true);
    }
}
