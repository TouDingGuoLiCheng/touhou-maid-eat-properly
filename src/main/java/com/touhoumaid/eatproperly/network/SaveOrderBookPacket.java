package com.touhoumaid.eatproperly.network;

import com.touhoumaid.eatproperly.food.FoodPool;
import com.touhoumaid.eatproperly.food.OrderBookData;
import com.touhoumaid.eatproperly.food.OrderBookFilterMode;
import com.touhoumaid.eatproperly.food.OrderBookSnapshot;
import com.touhoumaid.eatproperly.item.ModItems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/** Client -> server: persist Order Book filter from the crude GUI. */
public record SaveOrderBookPacket(InteractionHand hand, OrderBookFilterMode mode, Set<String> ids) {
    public static void encode(SaveOrderBookPacket packet, FriendlyByteBuf buf) {
        buf.writeEnum(packet.hand());
        buf.writeEnum(packet.mode());
        List<String> bounded = OrderBookProtocolLimits.boundedListedIds(packet.ids());
        buf.writeVarInt(bounded.size());
        for (String id : bounded) {
            buf.writeUtf(id, OrderBookProtocolLimits.MAX_ID_STRING_LENGTH);
        }
    }

    public static SaveOrderBookPacket decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        OrderBookFilterMode mode = buf.readEnum(OrderBookFilterMode.class);
        int count = buf.readVarInt();
        Set<String> ids = new LinkedHashSet<>();
        // Never throw DecoderException — that disconnects the integrated server (SP looks like MP lost).
        if (count < 0) {
            return new SaveOrderBookPacket(hand, mode, Set.of());
        }
        int toRead = Math.min(count, OrderBookProtocolLimits.MAX_LISTED_IDS);
        for (int i = 0; i < toRead; i++) {
            ids.add(buf.readUtf(OrderBookProtocolLimits.MAX_ID_STRING_LENGTH));
        }
        for (int i = toRead; i < count; i++) {
            try {
                buf.readUtf(OrderBookProtocolLimits.MAX_ID_STRING_LENGTH);
            } catch (Exception ignored) {
                break;
            }
        }
        return new SaveOrderBookPacket(hand, mode, ids);
    }

    public static void handle(SaveOrderBookPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (!OrderBookProtocolLimits.isValidListedIdCount(packet.ids().size())) {
                return;
            }
            ItemStack stack = player.getItemInHand(packet.hand());
            if (!stack.is(ModItems.ORDER_BOOK.get())) {
                return;
            }
            Set<String> validated = validatePoolIds(packet.ids());
            if (validated == null) {
                return;
            }
            OrderBookData.write(stack, new OrderBookSnapshot(packet.mode(), validated));
        });
        ctx.setPacketHandled(true);
    }

    /** Null if any id is not in the current food-request pool. */
    private static Set<String> validatePoolIds(Set<String> ids) {
        Set<String> validated = new LinkedHashSet<>();
        for (String id : ids) {
            ItemStack stack = FoodPool.fromId(id);
            if (stack.isEmpty() || !FoodPool.isPoolItem(stack)) {
                return null;
            }
            validated.add(FoodPool.toId(stack));
        }
        return validated;
    }
}
