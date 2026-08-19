package com.touhoumaid.eatproperly.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.entity.WallHangEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Wall-hang renderer.
 * <p>
 * {@link ItemRenderer#render} always ends with {@code translate(-0.5,-0.5,-0.5)}.
 * We apply the inverse {@code (+0.5,+0.5,+0.5)} last so remaining math is plain model space
 * (0..1 blockbench units), then place that on the hang AABB / wall face.
 */
@OnlyIn(Dist.CLIENT)
public class WallHangRenderer extends EntityRenderer<WallHangEntity> {
    private static final ModelResourceLocation CERT_3D = new ModelResourceLocation(
            new ResourceLocation(EatProperlyMod.MOD_ID, "praise_certificate_3d"), "inventory");
    private static final ModelResourceLocation BANNER_3D = new ModelResourceLocation(
            new ResourceLocation(EatProperlyMod.MOD_ID, "banner_honest_boss_3d"), "inventory");
    /** Local +Z = into room; wall face is this far toward -Z from the entity origin. */
    private static final double WALL_FACE_LOCAL_Z = -0.03125D;
    /** Banner cloth sits at model z ≈ -4 (pixels). */
    private static final double BANNER_CLOTH_Z = -4.0D / 16.0D;

    private final ItemRenderer itemRenderer;

    public WallHangRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(WallHangEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        Direction dir = entity.getDirection();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - dir.toYRot()));

        boolean banner = entity.isBanner();
        double halfH = entity.getHeight() / 32.0D;

        if (banner) {
            // Model already faces -Z (toward wall). Center X, align Y, put cloth on wall.
            poseStack.translate(-0.5D, -halfH, WALL_FACE_LOCAL_Z - BANNER_CLOTH_Z);
        } else {
            // Plane at model x=16. -90°Y: east/+X → +Z (into room), plane → z=+1;
            // translate X +0.5 and Z WALL-1 so the plane sits on the wall, centered.
            poseStack.translate(0.5D, -halfH, WALL_FACE_LOCAL_Z - 1.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        }

        // Cancel ItemRenderer's baked-model origin shift (must be the last pose before render).
        poseStack.translate(0.5D, 0.5D, 0.5D);

        BakedModel model = Minecraft.getInstance().getModelManager().getModel(banner ? BANNER_3D : CERT_3D);
        itemRenderer.render(stack, ItemDisplayContext.NONE, false, poseStack, buffer, packedLight,
                OverlayTexture.NO_OVERLAY, model);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WallHangEntity entity) {
        return new ResourceLocation(EatProperlyMod.MOD_ID, "textures/item/praise_certificate_3d.png");
    }
}
