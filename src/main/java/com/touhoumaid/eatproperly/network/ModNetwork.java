package com.touhoumaid.eatproperly.network;

import com.touhoumaid.eatproperly.EatProperlyMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL)
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .simpleChannel();

    private static int id;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(ClaimMilestoneRewardPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ClaimMilestoneRewardPacket::encode)
                .decoder(ClaimMilestoneRewardPacket::decode)
                .consumerMainThread(ClaimMilestoneRewardPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenTrinketsPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(OpenTrinketsPacket::encode)
                .decoder(OpenTrinketsPacket::decode)
                .consumerMainThread(OpenTrinketsPacket::handle)
                .add();
        CHANNEL.messageBuilder(ToggleTrinketPanelPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ToggleTrinketPanelPacket::encode)
                .decoder(ToggleTrinketPanelPacket::decode)
                .consumerMainThread(ToggleTrinketPanelPacket::handle)
                .add();
    }
}
