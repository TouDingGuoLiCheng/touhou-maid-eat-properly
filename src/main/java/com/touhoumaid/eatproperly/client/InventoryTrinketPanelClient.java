package com.touhoumaid.eatproperly.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.network.ModNetwork;
import com.touhoumaid.eatproperly.network.OpenTrinketsPacket;
import com.touhoumaid.eatproperly.network.ToggleTrinketPanelPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Inventory tab button on the player-model frame top-right (survival + creative inventory tab).
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID, value = Dist.CLIENT)
public final class InventoryTrinketPanelClient {
    private static final ResourceLocation BUTTON =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "textures/gui/trinket_tab_button.png");
    private static final ResourceLocation PANEL =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "textures/gui/player_trinket_panel.png");

    /** Match small silver inventory accessory buttons (~8px), not the old 14px gold plate. */
    private static final int BTN_W = 8;
    private static final int BTN_H = 8;
    private static final int BTN_TEX = 8;
    /** Survival inventory (176 wide): model preview box spans x 26..75, y 8..78. */
    private static final int BTN_X_SURVIVAL = 66;
    private static final int BTN_Y_SURVIVAL = 9;
    /** Creative inventory tab (195 wide): model preview box sits further right, x 73..104, y 6..48. */
    private static final int BTN_X_CREATIVE = 95;
    private static final int BTN_Y_CREATIVE = 7;

    private static final int PANEL_W = 32;
    private static final int PANEL_H = 68;
    private static final int PANEL_X = -34;
    private static final int PANEL_Y = 4;

    private InventoryTrinketPanelClient() {
    }

    private static boolean isSupportedScreen(Screen screen) {
        if (screen instanceof InventoryScreen) {
            return true;
        }
        return screen instanceof CreativeModeInventoryScreen creative && creative.isInventoryOpen();
    }

    private static int buttonX(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen ? BTN_X_CREATIVE : BTN_X_SURVIVAL;
    }

    private static int buttonY(Screen screen) {
        return screen instanceof CreativeModeInventoryScreen ? BTN_Y_CREATIVE : BTN_Y_SURVIVAL;
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        Screen next = event.getNewScreen();
        if (!(next instanceof InventoryScreen) && !(next instanceof CreativeModeInventoryScreen)) {
            return;
        }
        if (next instanceof InventoryScreen) {
            ModNetwork.CHANNEL.sendToServer(new ToggleTrinketPanelPacket(TrinketPanelClient.isOpen()));
        }
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
            return;
        }
        if (!(screen instanceof InventoryScreen) && !(screen instanceof CreativeModeInventoryScreen)) {
            return;
        }
        TrinketTabButton button = new TrinketTabButton(screen, btn -> onButtonPress((TrinketTabButton) btn));
        refreshTooltip(button);
        event.addListener(button);
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen screen) || !TrinketPanelClient.isOpen()) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int px = screen.getGuiLeft() + PANEL_X;
        int py = screen.getGuiTop() + PANEL_Y;
        graphics.blit(PANEL, px, py, 0, 0, PANEL_W, PANEL_H, PANEL_W, PANEL_H);
    }

    private static void onButtonPress(TrinketTabButton button) {
        AbstractContainerScreen<?> parent = button.parent;
        if (parent instanceof CreativeModeInventoryScreen) {
            // Creative menu has no InventoryMenu trinket slots — open standalone GUI.
            ModNetwork.CHANNEL.sendToServer(new OpenTrinketsPacket());
            return;
        }
        TrinketPanelClient.toggle();
        ModNetwork.CHANNEL.sendToServer(new ToggleTrinketPanelPacket(TrinketPanelClient.isOpen()));
        refreshTooltip(button);
    }

    private static void refreshTooltip(TrinketTabButton button) {
        boolean creative = button.parent instanceof CreativeModeInventoryScreen;
        String key = creative
                ? "gui.touhou_maid_eat_properly.trinkets"
                : (TrinketPanelClient.isOpen()
                        ? "gui.touhou_maid_eat_properly.trinket_tab.close"
                        : "gui.touhou_maid_eat_properly.trinket_tab.open");
        button.setTooltip(Tooltip.create(Component.translatable(key)));
    }

    private static final class TrinketTabButton extends Button {
        private final AbstractContainerScreen<?> parent;

        private TrinketTabButton(AbstractContainerScreen<?> parent, OnPress onPress) {
            super(parent.getGuiLeft() + buttonX(parent), parent.getGuiTop() + buttonY(parent),
                    BTN_W, BTN_H, Component.empty(), onPress, DEFAULT_NARRATION);
            this.parent = parent;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.visible = isSupportedScreen(this.parent);
            this.active = this.visible;
            if (!this.visible) {
                return;
            }
            this.setX(this.parent.getGuiLeft() + buttonX(this.parent));
            this.setY(this.parent.getGuiTop() + buttonY(this.parent));
            // 3D player preview writes depth; keep depth off or the tab vanishes over the model.
            RenderSystem.disableDepthTest();
            float brightness = this.isHoveredOrFocused() ? 1.2F : 1.0F;
            graphics.setColor(brightness, brightness, brightness, 1.0F);
            graphics.blit(BUTTON, this.getX(), this.getY(), BTN_W, BTN_H, 0, 0, BTN_TEX, BTN_TEX, BTN_TEX, BTN_TEX);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableDepthTest();
        }
    }
}
