package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidHomeMealTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.food.FoodRequestData;
import com.touhoumaid.eatproperly.food.FoodRequestHandler;
import com.touhoumaid.eatproperly.food.HomeMealSlotPick;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

/**
 * When a maid has an active food request, prefer eating that food from the picnic mat.
 */
@Mixin(value = MaidHomeMealTask.class, remap = false)
public abstract class MaidHomeMealPreferRequestMixin {
    @Inject(
            method = "start(Lnet/minecraft/server/level/ServerLevel;Lcom/github/tartaricacid/touhoulittlemaid/entity/passive/EntityMaid;J)V",
            at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/IntList;size()I", ordinal = 0),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void eatproperly$preferRequestedFood(
            ServerLevel serverLevel,
            EntityMaid maid,
            long gameTime,
            CallbackInfo ci,
            List<?> maidMeals,
            InteractionHand eanHand,
            ItemStack itemInHand,
            IItemHandlerModifiable availableInv,
            ItemStack handItemCopy,
            ItemStack leftoverStack,
            ItemStackHandler handler,
            IntList candidateFood) {
        if (!FoodRequestData.hasRequest(maid) || candidateFood == null || candidateFood.isEmpty() || handler == null) {
            return;
        }
        ItemStack wanted = FoodRequestData.getRequestStack(maid);
        if (wanted.isEmpty()) {
            return;
        }
        boolean[] matches = new boolean[candidateFood.size()];
        for (int i = 0; i < candidateFood.size(); i++) {
            matches[i] = FoodRequestHandler.matchesRequest(handler.getStackInSlot(candidateFood.getInt(i)), wanted);
        }
        int[] candIdx = HomeMealSlotPick.candidateIndices(matches);
        if (candIdx.length == 0 || candIdx.length == matches.length) {
            return;
        }
        int[] preferredSlots = new int[candIdx.length];
        for (int i = 0; i < candIdx.length; i++) {
            preferredSlots[i] = candidateFood.getInt(candIdx[i]);
        }
        candidateFood.clear();
        for (int slot : preferredSlots) {
            candidateFood.add(slot);
        }
    }
}
