package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.food.FoodRequestData;
import com.touhoumaid.eatproperly.inventory.EatProperlyContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityMaid.class)
public abstract class EntityMaidMixin {
    @Inject(method = "getGuiProvider", at = @At("HEAD"), cancellable = true, remap = false)
    private void eatproperly$getGuiProvider(int tabId, CallbackInfoReturnable<MenuProvider> cir) {
        if (tabId == EatProperlyConfig.GUI_TAB_ID) {
            EntityMaid self = (EntityMaid) (Object) this;
            cir.setReturnValue(EatProperlyContainer.create(self.getId()));
        }
    }

    @Inject(method = "openMaidGui(Lnet/minecraft/world/entity/player/Player;I)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void eatproperly$openMaidGui(Player player, int tabId, CallbackInfoReturnable<Boolean> cir) {
        if (tabId != EatProperlyConfig.GUI_TAB_ID) {
            return;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            cir.setReturnValue(true);
            return;
        }
        EntityMaid self = (EntityMaid) (Object) this;
        if (self.isSleeping()) {
            cir.setReturnValue(true);
            return;
        }
        self.getNavigation().stop();
        ItemStack food = FoodRequestData.getRequestStack(self);
        MenuProvider provider = EatProperlyContainer.create(self.getId());
        NetworkHooks.openScreen(serverPlayer, provider, buf -> EatProperlyContainer.writeExtraData(buf, self.getId(), food));
        cir.setReturnValue(true);
    }
}
