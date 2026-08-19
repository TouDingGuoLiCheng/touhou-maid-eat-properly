package com.touhoumaid.eatproperly.inventory;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.food.OrderBookFilterMode;
import com.touhoumaid.eatproperly.food.OrderBookSnapshot;
import com.touhoumaid.eatproperly.network.OrderBookProtocolLimits;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OrderBookContainer extends AbstractContainerMenu {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, EatProperlyMod.MOD_ID);

    public static final RegistryObject<MenuType<OrderBookContainer>> TYPE = MENUS.register(
            "order_book",
            () -> IForgeMenuType.create(OrderBookContainer::fromNetwork));

    private final InteractionHand hand;
    private final OrderBookSnapshot snapshot;

    public OrderBookContainer(int containerId, InteractionHand hand, OrderBookSnapshot snapshot) {
        super(TYPE.get(), containerId);
        this.hand = hand;
        this.snapshot = snapshot;
    }

    private static OrderBookContainer fromNetwork(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        OrderBookFilterMode mode = buf.readEnum(OrderBookFilterMode.class);
        int count = buf.readVarInt();
        Set<String> ids = new LinkedHashSet<>();
        if (count > 0) {
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
        }
        return new OrderBookContainer(containerId, hand, new OrderBookSnapshot(mode, ids));
    }

    public static void writeOpenData(FriendlyByteBuf buf, InteractionHand hand, OrderBookSnapshot snapshot) {
        buf.writeEnum(hand);
        buf.writeEnum(snapshot.mode());
        List<String> boundedIds = OrderBookProtocolLimits.boundedListedIds(snapshot.listedIds());
        buf.writeVarInt(boundedIds.size());
        for (String id : boundedIds) {
            buf.writeUtf(id, OrderBookProtocolLimits.MAX_ID_STRING_LENGTH);
        }
    }

    public InteractionHand getHand() {
        return hand;
    }

    public OrderBookSnapshot getSnapshot() {
        return snapshot;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static final class Provider implements MenuProvider {
        private final InteractionHand hand;
        private final OrderBookSnapshot snapshot;

        public Provider(InteractionHand hand, OrderBookSnapshot snapshot) {
            this.hand = hand;
            this.snapshot = snapshot;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("gui.touhou_maid_eat_properly.order_book");
        }

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
            return new OrderBookContainer(id, hand, snapshot);
        }
    }
}
