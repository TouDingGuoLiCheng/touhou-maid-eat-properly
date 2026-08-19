package com.touhoumaid.eatproperly.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.food.FoodPermitData;
import com.touhoumaid.eatproperly.food.FoodPermitMark;
import com.touhoumaid.eatproperly.food.FoodPermitPrune;
import com.touhoumaid.eatproperly.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/** Draws outlines for containers marked on the Food Permit held in the main hand. */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID, value = Dist.CLIENT)
public final class FoodPermitOutlineClient {
    private static final float R = 0.35F;
    private static final float G = 0.85F;
    private static final float B = 0.45F;
    private static final float A = 0.9F;

    private FoodPermitOutlineClient() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        ItemStack main = player.getMainHandItem();
        if (!main.is(ModItems.FOOD_PERMIT.get())) {
            return;
        }
        List<FoodPermitMark> marks = FoodPermitData.read(main);
        if (marks.isEmpty()) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffer.getBuffer(RenderType.lines());
        Vec3 cam = event.getCamera().getPosition();

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        for (FoodPermitMark mark : marks) {
            if (!FoodPermitPrune.isPresentMarkable(mc.level, mark)) {
                continue;
            }
            BlockPos pos = FoodPermitData.toPos(mark);
            AABB box = new AABB(pos).inflate(0.002D);
            LevelRenderer.renderLineBox(pose, lines, box, R, G, B, A);
        }
        pose.popPose();
        buffer.endBatch(RenderType.lines());
    }
}
