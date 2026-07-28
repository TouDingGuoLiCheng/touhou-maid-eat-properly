package com.touhoumaid.eatproperly.client;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.network.ModNetwork;
import com.touhoumaid.eatproperly.network.ToggleTrinketPanelPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Survival inventory: top-right tab button + optional left trinket rail.
 */
@Mod.EventBusSubscriber(modid = EatProperlyMod.MOD_ID, value = Dist.CLIENT)
public final class InventoryTrinketPanelClient {
    private static final ResourceLocation BUTTON =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "textures/gui/trinket_tab_button.png");
    private static final ResourceLocation PANEL =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "textures/gui/player_trinket_panel.png");

    private static final int BTN_W = 14;
    private static final int BTN_H = 14;
    /** Relative to InventoryScreen leftPos/topPos — near top-right of the player model. */
    private static final int BTN_X = 142;
    private static final int BTN_Y = 8;

    private static final int PANEL_W = 32;
    private static final int PANEL_H = 68;
    private static final int PANEL_X = -34;
    private static final int PANEL_Y = 4;

    private InventoryTrinketPanelClient() {
    }

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        if (!(event.getNewScreen() instanceof InventoryScreen)) {
            return;
        }
        // Re-sync fold state whenever survival inventory opens.
        ModNetwork.CHANNEL.sendToServer(new ToggleTrinketPanelPacket(TrinketPanelClient.isOpen()));
    }

    @SubscribeEvent
    public static void onScreenRenderPre(ScreenEvent.Render.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen screen) || !TrinketPanelClient.isOpen()) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int x = screen.getGuiLeft() + PANEL_X;
        int y = screen.getGuiTop() + PANEL_Y;
        graphics.blit(PANEL, x, y, 0, 0, PANEL_W, PANEL_H, PANEL_W, PANEL_H);
    }

    @SubscribeEvent
    public static void onScreenRenderPost(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        int bx = screen.getGuiLeft() + BTN_X;
        int by = screen.getGuiTop() + BTN_Y;
        graphics.blit(BUTTON, bx, by, 0, 0, BTN_W, BTN_H, BTN_W, BTN_H);
    }

    @SubscribeEvent
    public static void onMouseClickPre(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen screen) || event.getButton() != 0) {
            return;
        }
        int bx = screen.getGuiLeft() + BTN_X;
        int by = screen.getGuiTop() + BTN_Y;
        double mx = event.getMouseX();
        double my = event.getMouseY();
        if (mx >= bx && mx < bx + BTN_W && my >= by && my < by + BTN_H) {
            TrinketPanelClient.toggle();
            ModNetwork.CHANNEL.sendToServer(new ToggleTrinketPanelPacket(TrinketPanelClient.isOpen()));
            event.setCanceled(true);
        }
    }
}
