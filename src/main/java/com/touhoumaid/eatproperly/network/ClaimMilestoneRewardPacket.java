package com.touhoumaid.eatproperly.network;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.inventory.EatProperlyContainer;
import com.touhoumaid.eatproperly.milestone.MilestoneRewards;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ClaimMilestoneRewardPacket(int maidEntityId, int level) {
    public static void encode(ClaimMilestoneRewardPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.maidEntityId);
        buf.writeVarInt(packet.level);
    }

    public static ClaimMilestoneRewardPacket decode(FriendlyByteBuf buf) {
        return new ClaimMilestoneRewardPacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ClaimMilestoneRewardPacket packet, Supplier<NetworkEvent.Context> ctxSupplier) {
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
            int modeBefore = MilestoneRewards.uiMode(maid);
            ItemStack granted = MilestoneRewards.claimFirst(maid, player, packet.level);
            if (granted.isEmpty() && modeBefore == MilestoneRewards.MODE_WAITING) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "gui.touhou_maid_eat_properly.milestone_care_more"), true);
            }
        });
        ctx.setPacketHandled(true);
    }
}
