package com.touhoumaid.eatproperly.item;

import com.touhoumaid.eatproperly.food.OrderBookData;
import com.touhoumaid.eatproperly.food.OrderBookSnapshot;
import com.touhoumaid.eatproperly.inventory.OrderBookContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Maid-only bauble: filters the food-request pool via blacklist/whitelist on the item NBT. */
public class OrderBookItem extends Item {
    public OrderBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            OrderBookSnapshot snapshot = OrderBookData.read(stack);
            NetworkHooks.openScreen(serverPlayer, new OrderBookContainer.Provider(hand, snapshot),
                    buf -> OrderBookContainer.writeOpenData(buf, hand, snapshot));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.touhou_maid_eat_properly.order_book.lore.0"));
        tooltip.add(Component.translatable("item.touhou_maid_eat_properly.order_book.lore.1"));
        tooltip.add(Component.translatable("item.touhou_maid_eat_properly.order_book.lore.2"));
    }
}
