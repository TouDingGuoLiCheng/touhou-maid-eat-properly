package com.touhoumaid.eatproperly.client;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.inventory.EatProperlyContainer;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * 好好吃饭页：一体背景板，内容自上而下留白排布。
 * 面板贴在女仆 GUI 内容区 (leftPos+80, topPos+28)，尺寸 176×137。
 */
public class EatProperlyScreen extends AbstractMaidContainerGui<EatProperlyContainer> {
    private static final ResourceLocation BG =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "textures/gui/eat_properly_panel.png");
    /** Atlas: 满(0,16) / 半(16,16) / 空(32,16)，各 16×16；绘制时缩到 12×12 */
    private static final ResourceLocation SATIETY_ICONS =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "textures/gui/satiety_icons.png");

    private static final int PANEL_X = 80;
    private static final int PANEL_Y = 28;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 137;

    /** Inset from panel edge (panel starts at 80,28; border ~2px). */
    private static final int CONTENT_X = 96;
    private static final int CONTENT_W = 144;

    private static final int SATIETY_LABEL_Y = 42;
    private static final int SATIETY_BAR_Y = 56;
    private static final int REQUEST_Y = 80;
    private static final int ITEM_Y = 96;
    private static final int HINT_Y = 120;

    private static final int SATIETY_ICON_SIZE = 12;
    private static final int SATIETY_ICON_STEP = 11;
    private static final int SATIETY_SRC_SIZE = 16;
    private static final int SATIETY_UV_Y = 16;
    private static final int SATIETY_UV_FULL = 0;
    private static final int SATIETY_UV_HALF = 16;
    private static final int SATIETY_UV_EMPTY = 32;
    private static final int SATIETY_ATLAS = 256;

    private static final int TEXT_COLOR = 0x404040;
    private static final int MUTED_COLOR = 0x666666;

    public EatProperlyScreen(EatProperlyContainer container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
        super.renderBg(graphics, partialTicks, x, y);
        graphics.blit(BG, this.leftPos + PANEL_X, this.topPos + PANEL_Y, 0, 0, PANEL_W, PANEL_H);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font,
                Component.translatable("gui.touhou_maid_eat_properly.satiety"),
                CONTENT_X, SATIETY_LABEL_Y, TEXT_COLOR, false);
        renderSatietyBar(graphics, CONTENT_X, SATIETY_BAR_Y);

        ItemStack food = this.menu.getRequestedFood();
        if (food.isEmpty()) {
            graphics.drawWordWrap(this.font,
                    Component.translatable("gui.touhou_maid_eat_properly.no_request"),
                    CONTENT_X, REQUEST_Y, CONTENT_W, MUTED_COLOR);
        } else {
            graphics.drawString(this.font,
                    Component.translatable("gui.touhou_maid_eat_properly.current_request"),
                    CONTENT_X, REQUEST_Y, TEXT_COLOR, false);

            graphics.renderItem(food, CONTENT_X, ITEM_Y);
            graphics.renderItemDecorations(this.font, food, CONTENT_X, ITEM_Y);

            int nameX = CONTENT_X + 20;
            int nameY = ITEM_Y + 4;
            String name = this.font.plainSubstrByWidth(food.getHoverName().getString(), CONTENT_W - 20);
            graphics.drawString(this.font, name, nameX, nameY, TEXT_COLOR, false);

            graphics.drawWordWrap(this.font,
                    Component.translatable("gui.touhou_maid_eat_properly.hint"),
                    CONTENT_X, HINT_Y, CONTENT_W, MUTED_COLOR);
        }
    }

    private void renderSatietyBar(GuiGraphics graphics, int x, int y) {
        EntityMaid maid = this.menu.getMaid();
        int foodLevel = maid != null ? SatietyData.getSatiety(maid) : 0;
        // Vanilla hunger style: icons left→right, empty from the right as satiety drops.
        for (int i = 0; i < 10; i++) {
            int iconX = x + i * SATIETY_ICON_STEP;
            blitIcon(graphics, iconX, y, SATIETY_UV_EMPTY);
            if (foodLevel > i * 2 + 1) {
                blitIcon(graphics, iconX, y, SATIETY_UV_FULL);
            } else if (foodLevel > i * 2) {
                blitIcon(graphics, iconX, y, SATIETY_UV_HALF);
            }
        }
        String value = foodLevel + "/" + EatProperlyConfig.SATIETY_MAX;
        int valueX = x + 10 * SATIETY_ICON_STEP + 2;
        graphics.drawString(this.font, value, valueX, y + 2, TEXT_COLOR, false);
    }

    private static void blitIcon(GuiGraphics graphics, int x, int y, int u) {
        graphics.blit(SATIETY_ICONS, x, y, SATIETY_ICON_SIZE, SATIETY_ICON_SIZE,
                u, SATIETY_UV_Y, SATIETY_SRC_SIZE, SATIETY_SRC_SIZE, SATIETY_ATLAS, SATIETY_ATLAS);
    }
}
