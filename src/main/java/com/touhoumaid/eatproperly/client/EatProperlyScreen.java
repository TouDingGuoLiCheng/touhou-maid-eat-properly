package com.touhoumaid.eatproperly.client;

import com.github.tartaricacid.touhoulittlemaid.client.gui.entity.maid.AbstractMaidContainerGui;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.mojang.blaze3d.systems.RenderSystem;
import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.config.EatProperlyConfig;
import com.touhoumaid.eatproperly.inventory.EatProperlyContainer;
import com.touhoumaid.eatproperly.milestone.MilestoneRewards;
import com.touhoumaid.eatproperly.network.ClaimMilestoneRewardPacket;
import com.touhoumaid.eatproperly.network.ModNetwork;
import com.touhoumaid.eatproperly.network.ToggleSnackRaidPacket;
import com.touhoumaid.eatproperly.satiety.SatietyData;
import com.touhoumaid.eatproperly.satiety.SatietySaturationOverlay;
import com.touhoumaid.eatproperly.snackraid.SnackRaidToggleTexts;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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
    private static final ResourceLocation SATIETY_ICONS =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "textures/gui/satiety_icons.png");
    private static final ResourceLocation SNACK_ALLOWED =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "textures/gui/snack_raid_allowed.png");
    private static final ResourceLocation SNACK_DISALLOWED =
            ResourceLocation.fromNamespaceAndPath(EatProperlyMod.MOD_ID, "textures/gui/snack_raid_disallowed.png");

    private static final int PANEL_X = 80;
    private static final int PANEL_Y = 28;
    private static final int PANEL_W = 176;
    private static final int PANEL_H = 137;

    private static final int CONTENT_X = 96;
    private static final int CONTENT_W = 144;

    private static final int SATIETY_LABEL_Y = 42;
    private static final int SATIETY_BAR_Y = 56;
    private static final int REQUEST_Y = 80;
    private static final int ITEM_Y = 96;
    private static final int HINT_Y = 120;

    /** Icon button near satiety label (top-right of content strip). */
    private static final int SNACK_BUTTON_SIZE = 18;
    private static final int SNACK_BUTTON_X = CONTENT_X + CONTENT_W - SNACK_BUTTON_SIZE;
    private static final int SNACK_BUTTON_Y = SATIETY_LABEL_Y - 2;
    private static final int SNACK_ICON_SRC = 64;

    /** Milestone claim slot (screen-relative, same space as renderLabels). */
    private static final int MILESTONE_SLOT_X = CONTENT_X + CONTENT_W - 20;
    private static final int MILESTONE_SLOT_Y = ITEM_Y;
    private static final int MILESTONE_SLOT_SIZE = 18;

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

    private SnackRaidIconButton snackRaidButton;

    public EatProperlyScreen(EatProperlyContainer container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        super.init();
        this.snackRaidButton = this.addRenderableWidget(new SnackRaidIconButton(
                this.leftPos + SNACK_BUTTON_X,
                this.topPos + SNACK_BUTTON_Y,
                button -> {
                    EntityMaid maid = this.menu.getMaid();
                    if (maid != null) {
                        ModNetwork.CHANNEL.sendToServer(new ToggleSnackRaidPacket(maid.getId()));
                    }
                }));
        refreshSnackRaidButton();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        refreshSnackRaidButton();
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
                    CONTENT_X, REQUEST_Y, CONTENT_W - 24, MUTED_COLOR);
        } else if (this.menu.isRequestForgotten()) {
            EntityMaid maid = this.menu.getMaid();
            String maidName = maid != null ? maid.getDisplayName().getString() : "";
            graphics.drawWordWrap(this.font,
                    Component.translatable("gui.touhou_maid_eat_properly.request_forgotten", maidName),
                    CONTENT_X, REQUEST_Y, CONTENT_W - 24, TEXT_COLOR);
            graphics.drawCenteredString(this.font, "?", CONTENT_X + 8, ITEM_Y + 4, MUTED_COLOR);
        } else {
            graphics.drawString(this.font,
                    Component.translatable("gui.touhou_maid_eat_properly.current_request"),
                    CONTENT_X, REQUEST_Y, TEXT_COLOR, false);

            graphics.renderItem(food, CONTENT_X, ITEM_Y);
            graphics.renderItemDecorations(this.font, food, CONTENT_X, ITEM_Y);

            int nameX = CONTENT_X + 20;
            int nameY = ITEM_Y + 4;
            String name = this.font.plainSubstrByWidth(food.getHoverName().getString(), CONTENT_W - 44);
            graphics.drawString(this.font, name, nameX, nameY, TEXT_COLOR, false);

            if (this.menu.getMilestoneMode() != MilestoneRewards.MODE_BOND) {
                graphics.drawWordWrap(this.font,
                        Component.translatable("gui.touhou_maid_eat_properly.hint"),
                        CONTENT_X, HINT_Y, CONTENT_W, MUTED_COLOR);
            }
        }

        renderMilestoneSlot(graphics, mouseX, mouseY);
    }

    private void renderMilestoneSlot(GuiGraphics graphics, int mouseX, int mouseY) {
        int mode = this.menu.getMilestoneMode();
        EntityMaid maid = this.menu.getMaid();

        if (mode == MilestoneRewards.MODE_BOND) {
            String maidName = maid != null ? maid.getDisplayName().getString() : "";
            graphics.drawWordWrap(this.font,
                    Component.translatable("gui.touhou_maid_eat_properly.milestone_bond", maidName),
                    CONTENT_X, HINT_Y - 2, CONTENT_W, TEXT_COLOR);
            return;
        }

        int slotX = MILESTONE_SLOT_X;
        int slotY = MILESTONE_SLOT_Y;
        graphics.fill(slotX - 1, slotY - 1, slotX + MILESTONE_SLOT_SIZE, slotY + MILESTONE_SLOT_SIZE, 0xFF8B8B8B);
        graphics.fill(slotX, slotY, slotX + MILESTONE_SLOT_SIZE - 2, slotY + MILESTONE_SLOT_SIZE - 2, 0xFF373737);

        if (mode == MilestoneRewards.MODE_CLAIMABLE) {
            ItemStack pending = this.menu.getPendingMilestoneReward();
            if (!pending.isEmpty()) {
                graphics.renderItem(pending, slotX + 1, slotY + 1);
                graphics.renderItemDecorations(this.font, pending, slotX + 1, slotY + 1);
            }
        } else {
            graphics.drawCenteredString(this.font, "?", slotX + 8, slotY + 5, 0x777777);
        }

        if (isHoveringMilestone(mouseX, mouseY)) {
            if (mode == MilestoneRewards.MODE_CLAIMABLE) {
                ItemStack pending = this.menu.getPendingMilestoneReward();
                if (!pending.isEmpty()) {
                    graphics.renderTooltip(this.font, pending, mouseX - this.leftPos, mouseY - this.topPos);
                }
            } else {
                graphics.renderTooltip(this.font,
                        Component.literal("???"),
                        mouseX - this.leftPos, mouseY - this.topPos);
            }
        }
    }

    private boolean isHoveringMilestone(int mouseX, int mouseY) {
        if (this.menu.getMilestoneMode() == MilestoneRewards.MODE_BOND) {
            return false;
        }
        int absX = this.leftPos + MILESTONE_SLOT_X;
        int absY = this.topPos + MILESTONE_SLOT_Y;
        return mouseX >= absX && mouseX < absX + MILESTONE_SLOT_SIZE
                && mouseY >= absY && mouseY < absY + MILESTONE_SLOT_SIZE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHoveringMilestone((int) mouseX, (int) mouseY)) {
            EntityMaid maid = this.menu.getMaid();
            if (maid != null) {
                ModNetwork.CHANNEL.sendToServer(new ClaimMilestoneRewardPacket(
                        maid.getId(), this.menu.getPendingMilestoneLevel()));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderSatietyBar(GuiGraphics graphics, int x, int y) {
        EntityMaid maid = this.menu.getMaid();
        int foodLevel = maid != null ? SatietyData.getSatiety(maid) : 0;
        float saturation = this.menu.getSaturationLevel();
        for (int i = 0; i < 10; i++) {
            int iconX = x + i * SATIETY_ICON_STEP;
            blitIcon(graphics, iconX, y, SATIETY_UV_EMPTY);
            if (foodLevel > i * 2 + 1) {
                blitIcon(graphics, iconX, y, SATIETY_UV_FULL);
            } else if (foodLevel > i * 2) {
                blitIcon(graphics, iconX, y, SATIETY_UV_HALF);
            }
        }
        for (int i = 0; i < 10; i++) {
            SatietySaturationOverlay.Kind kind = SatietySaturationOverlay.kindForIcon(saturation, i);
            if (kind == SatietySaturationOverlay.Kind.NONE) {
                continue;
            }
            int iconX = x + i * SATIETY_ICON_STEP;
            if (SatietySaturationOverlay.needsNormalCakeRightUnderlay(kind)) {
                // Paint normal FULL cake on the right first (covers dark HALF food base), then gold left.
                graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                int halfDest = SatietySaturationOverlay.destWidth(kind, SATIETY_ICON_SIZE);
                int halfSrc = SatietySaturationOverlay.srcWidth(kind, SATIETY_SRC_SIZE);
                blitIcon(graphics, iconX, y, SATIETY_UV_FULL, halfDest, halfSrc,
                        SatietySaturationOverlay.halfDestOffset(SATIETY_ICON_SIZE),
                        SatietySaturationOverlay.halfSrcUOffset(SATIETY_SRC_SIZE));
                graphics.setColor(1.0F, 0.85F, 0.2F, 1.0F);
                blitIcon(graphics, iconX, y, SATIETY_UV_FULL, halfDest, halfSrc);
            } else {
                graphics.setColor(1.0F, 0.85F, 0.2F, 1.0F);
                blitIcon(graphics, iconX, y, SATIETY_UV_FULL,
                        SatietySaturationOverlay.destWidth(kind, SATIETY_ICON_SIZE),
                        SatietySaturationOverlay.srcWidth(kind, SATIETY_SRC_SIZE));
            }
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        String value = foodLevel + "/" + EatProperlyConfig.SATIETY_MAX;
        int valueX = x + 10 * SATIETY_ICON_STEP + 2;
        graphics.drawString(this.font, value, valueX, y + 2, TEXT_COLOR, false);
    }

    private static void blitIcon(GuiGraphics graphics, int x, int y, int u) {
        blitIcon(graphics, x, y, u, SATIETY_ICON_SIZE, SATIETY_SRC_SIZE);
    }

    private static void blitIcon(GuiGraphics graphics, int x, int y, int u, int destWidth, int srcWidth) {
        blitIcon(graphics, x, y, u, destWidth, srcWidth, 0, 0);
    }

    private static void blitIcon(GuiGraphics graphics, int x, int y, int u, int destWidth, int srcWidth,
                                 int destXOffset, int srcUOffset) {
        graphics.blit(SATIETY_ICONS, x + destXOffset, y, destWidth, SATIETY_ICON_SIZE,
                u + srcUOffset, SATIETY_UV_Y, srcWidth, SATIETY_SRC_SIZE, SATIETY_ATLAS, SATIETY_ATLAS);
    }

    private void refreshSnackRaidButton() {
        if (this.snackRaidButton == null) {
            return;
        }
        boolean configOn = EatProperlyConfig.SNACK_RAID_ENABLED.get();
        boolean allowed = this.menu.isSnackRaidAllowed();
        this.snackRaidButton.active = configOn;
        this.snackRaidButton.setAllowed(allowed);
        String tipKey = configOn
                ? SnackRaidToggleTexts.tooltipKey(allowed)
                : SnackRaidToggleTexts.TOOLTIP_DISABLED_BY_CONFIG_KEY;
        this.snackRaidButton.setTooltip(Tooltip.create(Component.translatable(tipKey)));
    }

    private final class SnackRaidIconButton extends Button {
        private boolean allowed = true;

        private SnackRaidIconButton(int x, int y, OnPress onPress) {
            super(x, y, SNACK_BUTTON_SIZE, SNACK_BUTTON_SIZE, Component.empty(), onPress, DEFAULT_NARRATION);
        }

        private void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            RenderSystem.enableDepthTest();
            ResourceLocation icon = this.allowed ? SNACK_ALLOWED : SNACK_DISALLOWED;
            float brightness = this.active ? (this.isHoveredOrFocused() ? 1.15F : 1.0F) : 0.45F;
            graphics.setColor(brightness, brightness, brightness, 1.0F);
            graphics.blit(icon, this.getX(), this.getY(), SNACK_BUTTON_SIZE, SNACK_BUTTON_SIZE,
                    0, 0, SNACK_ICON_SRC, SNACK_ICON_SRC, SNACK_ICON_SRC, SNACK_ICON_SRC);
            graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
