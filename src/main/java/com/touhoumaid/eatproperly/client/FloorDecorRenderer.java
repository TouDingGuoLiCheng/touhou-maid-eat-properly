package com.touhoumaid.eatproperly.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.entity.FloorDecorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FloorDecorRenderer extends EntityRenderer<FloorDecorEntity> {
    private static final ModelResourceLocation FIGURINE_3D = new ModelResourceLocation(
            new ResourceLocation(EatProperlyMod.MOD_ID, "block_figurine_3d"), "inventory");
    /** Tiny lift for foot element at y=-0.5px. */
    private static final double FOOT_LIFT = 1.0D / 16.0D;

    private final ItemRenderer itemRenderer;

    public FloorDecorRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(FloorDecorEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        float yaw = entity.getDecorYaw();
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        // Model corner at (-0.5, footLift, -0.5) so after cancel+ItemT the base centers on the entity.
        poseStack.translate(-0.5D, FOOT_LIFT, -0.5D);
        poseStack.translate(0.5D, 0.5D, 0.5D); // cancel ItemRenderer -0.5 origin

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(FIGURINE_3D);
        itemRenderer.render(stack, ItemDisplayContext.NONE, false, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, model);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FloorDecorEntity entity) {
        return new ResourceLocation(EatProperlyMod.MOD_ID, "textures/item/block_figurine_3d.png");
    }
}
