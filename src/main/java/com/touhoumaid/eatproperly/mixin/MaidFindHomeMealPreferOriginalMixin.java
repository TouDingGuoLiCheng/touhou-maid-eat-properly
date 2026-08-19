package com.touhoumaid.eatproperly.mixin;

import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidFindHomeMealTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityPicnicMat;
import com.touhoumaid.eatproperly.food.PicnicRefill;
import com.touhoumaid.eatproperly.food.PicnicRefillData;
import com.touhoumaid.eatproperly.food.PicnicRefillPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * N3.10: after fetching food for an empty-mat refill, steer FindHomeMeal to the remembered
 * original picnic mat instead of the nearest POI.
 */
@Mixin(value = MaidFindHomeMealTask.class, remap = false)
public abstract class MaidFindHomeMealPreferOriginalMixin {
    @Inject(method = "findPicnicMat", at = @At("RETURN"), cancellable = true)
    private void eatproperly$preferOriginalMat(
            ServerLevel level, EntityMaid maid, CallbackInfoReturnable<BlockPos> cir) {
        boolean seeking = PicnicRefillData.isSeeking(maid);
        boolean carries = PicnicRefill.carriesPoolFood(maid);
        boolean hasHome = PicnicRefillData.hasHomeMat(maid);
        if (!PicnicRefillPolicy.shouldForceOriginalMat(seeking, carries, hasHome)) {
            return;
        }
        BlockPos home = PicnicRefillData.getHomeMatPos(maid);
        if (home == null) {
            return;
        }
        String dim = PicnicRefillData.getHomeMatDimension(maid);
        if (!dim.isEmpty() && !dim.equals(level.dimension().location().toString())) {
            return;
        }
        BlockEntity be = level.getBlockEntity(home);
        if (!(be instanceof TileEntityPicnicMat)) {
            return;
        }
        cir.setReturnValue(home);
    }
}
