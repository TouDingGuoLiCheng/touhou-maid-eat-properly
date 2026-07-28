package com.touhoumaid.eatproperly.client;

import com.touhoumaid.eatproperly.inventory.DualLunchboxContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

/**
 * Two-slot dual lunchbox screen (placeholder panel; layout matches {@link LunchboxScreen}).
 */
public class DualLunchboxScreen extends AbstractContainerScreen<DualLunchboxContainer> {
    private static final int PANEL_COLOR = 0xFFC6C6C6;
    private static final int SLOT_FRAME_COLOR = 0xFF8B8B8B;
    private static final int SLOT_INNER_COLOR = 0xFF373737;
    private static final int SLOT_SIZE = 18;

    public DualLunchboxScreen(DualLunchboxContainer menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        // +18 vs LunchboxScreen to fit the second food slot above the player inventory.
        this.imageHeight = 151;
        this.inventoryLabelY = this.imageHeight - 94;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, PANEL_COLOR);
        for (Slot slot : this.menu.slots) {
            int sx = x + slot.x;
            int sy = y + slot.y;
            graphics.fill(sx - 1, sy - 1, sx + SLOT_SIZE, sy + SLOT_SIZE, SLOT_FRAME_COLOR);
            graphics.fill(sx, sy, sx + SLOT_SIZE - 2, sy + SLOT_SIZE - 2, SLOT_INNER_COLOR);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
