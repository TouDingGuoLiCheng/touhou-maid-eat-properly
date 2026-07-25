package com.touhoumaid.eatproperly.client;

import com.github.tartaricacid.touhoulittlemaid.api.event.client.MaidContainerGuiEvent;
import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui;
import com.github.tartaricacid.touhoulittlemaid.network.NetworkHandler;
import com.github.tartaricacid.touhoulittlemaid.network.message.ToggleTabMessage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID, value = Dist.CLIENT)
public final class ClientGuiEvents {
    private static final ResourceLocation GREEN_TEA =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, EatProperlyMod.TAB_ICON);
    /** Same atlas TLM uses for selected-tab white overlay. */
    private static final ResourceLocation SIDE =
            ResourceLocation.fromNamespaceAndPath("touhou_little_maid", "textures/gui/maid_gui_side.png");

    private static final int TAB_W = 24;
    private static final int TAB_H = 26;
    private static final int ICON_DRAW = 16; // match TLM MaidTabButton icon size
    /** Same U column as TLM "main" tab — V=21 is the light selected plate (U=0 is unrelated art). */
    private static final int SIDE_SELECTED_U = 107;
    private static final int SIDE_SELECTED_V = 21;

    private ClientGuiEvents() {
    }

    @SubscribeEvent
    public static void onGuiInit(MaidContainerGuiEvent.Init event) {
        AbstractMaidContainerGui<?> gui = event.getGui();
        if (gui.getMaid() == null) {
            return;
        }
        int x = event.getLeftPos() + 169;
        int y = event.getTopPos() + 5;
        boolean onOurScreen = gui instanceof EatProperlyScreen;

        TabIconButton iconButton = new TabIconButton(x, y, !onOurScreen, b ->
                NetworkHandler.CHANNEL.sendToServer(
                        new ToggleTabMessage(gui.getMaid().getId(), EatProperlyConfig.GUI_TAB_ID)));
        iconButton.setTooltip(Tooltip.create(Component.translatable("gui.touhou_maid_eat_properly.tab")));
        event.addButton("eat_properly_tab", iconButton);
    }

    private static class TabIconButton extends Button {
        private final boolean clickable;

        TabIconButton(int x, int y, boolean clickable, OnPress onPress) {
            super(x, y, TAB_W, TAB_H, Component.empty(), onPress, DEFAULT_NARRATION);
            this.clickable = clickable;
            this.active = clickable;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableDepthTest();
            // Selected tab: same light plate as MaidTabButton (U=left of main tab, V=21).
            if (!this.clickable) {
                graphics.blit(SIDE, this.getX(), this.getY(),
                        SIDE_SELECTED_U, SIDE_SELECTED_V, TAB_W, TAB_H, 256, 256);
            }
            // Match TLM icon inset: +4 x, +6 y inside 24×26 tab.
            int iconX = this.getX() + 4;
            int iconY = this.getY() + 6;
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            graphics.blit(GREEN_TEA, iconX, iconY, ICON_DRAW, ICON_DRAW, 0, 0, 32, 32, 32, 32);
        }
    }
}
