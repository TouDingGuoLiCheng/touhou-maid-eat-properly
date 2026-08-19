package com.touhoumaid.eatproperly.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.inventory.EatProperlyContainer;
import com.touhoumaid.eatproperly.snackraid.SnackRaidData;
import com.touhoumaid.eatproperly.snackraid.SnackRaidToggleTexts;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ToggleSnackRaidPacket(int maidEntityId) {
    public static void encode(ToggleSnackRaidPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.maidEntityId);
    }

    public static ToggleSnackRaidPacket decode(FriendlyByteBuf buf) {
        return new ToggleSnackRaidPacket(buf.readVarInt());
    }

    public static void handle(ToggleSnackRaidPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (!(player.level().getEntity(packet.maidEntityId) instanceof EntityMaid maid)) {
                return;
            }
            if (!(player.containerMenu instanceof EatProperlyContainer menu) || menu.getMaid() != maid) {
                return;
            }
            boolean allowed = SnackRaidData.toggleAllowedLocally(maid);
            player.displayClientMessage(Component.translatable(SnackRaidToggleTexts.feedbackKey(allowed)), true);
        });
        ctx.setPacketHandled(true);
    }
}
