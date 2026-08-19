package com.touhoumaid.eatproperly.client;

import com.touhoumaid.eatproperly.EatProperlyMod;
import com.touhoumaid.eatproperly.food.FoodPool;
import com.touhoumaid.eatproperly.food.OrderBookFilterMode;
import com.touhoumaid.eatproperly.food.OrderBookListFilter;
import com.touhoumaid.eatproperly.inventory.OrderBookContainer;
import com.touhoumaid.eatproperly.network.ModNetwork;
import com.touhoumaid.eatproperly.network.SaveOrderBookPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Open-book Order Book editor: eyebrow controls, left/right page grids, corner page turns.
 */
public class OrderBookScreen extends AbstractContainerScreen<OrderBookContainer> {
    private static final ResourceLocation BG = ResourceLocation.fromNamespaceAndPath(
            EatProperlyMod.MOD_ID, "textures/gui/order_book.png");

    /** Blit crop: texture content sits below empty top margin. */
    private static final int TEX_V = 40;
    private static final int PANEL_W = 256;
    private static final int PANEL_H = 176;

    private static final int COLUMNS = 5;
    private static final int ROWS = 6;
    private static final int CELL = 18;
    private static final int LEFT_GRID_X = 30;
    private static final int RIGHT_GRID_X = 142;
    private static final int GRID_TOP = 40;

    /** Warm amber selection (replaces green overlay). */
    private static final int SELECT_FILL = 0x60E8A84A;

    private final List<Item> allFoods = new ArrayList<>();
    private final List<Item> filtered = new ArrayList<>();
    private final Set<String> selected = new HashSet<>();

    private OrderBookFilterMode mode;
    private int page;
    private String query = "";
    private EditBox searchBox;

    public OrderBookScreen(OrderBookContainer menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = PANEL_W;
        this.imageHeight = PANEL_H;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
        this.mode = menu.getSnapshot().mode();
        this.selected.addAll(menu.getSnapshot().listedIds());
    }

    @Override
    protected void init() {
        super.init();
        reloadAllFoods();
        applyFilter();

        int eyebrowY = topPos + 14;
        addRenderableWidget(Button.builder(modeLabel(), button -> {
            mode = mode == OrderBookFilterMode.WHITELIST
                    ? OrderBookFilterMode.BLACKLIST
                    : OrderBookFilterMode.WHITELIST;
            button.setMessage(modeLabel());
        }).bounds(leftPos + 28, eyebrowY, 56, 16).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.touhou_maid_eat_properly.order_book.select_all"),
                button -> toggleSelectAllFiltered()
        ).bounds(leftPos + 88, eyebrowY, 40, 16).build());

        searchBox = new EditBox(
                font,
                leftPos + 132,
                eyebrowY,
                90,
                16,
                Component.translatable("gui.touhou_maid_eat_properly.order_book.search"));
        searchBox.setMaxLength(64);
        searchBox.setHint(Component.translatable("gui.touhou_maid_eat_properly.order_book.search"));
        searchBox.setResponder(text -> {
            query = text == null ? "" : text;
            page = 0;
            applyFilter();
        });
        addRenderableWidget(searchBox);

        int turnY = topPos + 147;
        addRenderableWidget(silentPageButton(leftPos + 32, turnY, 20, 16, Component.literal("<"), button -> {
            if (page > 0) {
                page--;
                playPageTurnSound();
            }
        }));

        addRenderableWidget(silentPageButton(leftPos + 201, turnY, 20, 16, Component.literal(">"), button -> {
            if ((page + 1) * pageSize() < filtered.size()) {
                page++;
                playPageTurnSound();
            }
        }));
    }

    /** No default UI click; page-turn sound is played only when the page actually changes. */
    private static Button silentPageButton(int x, int y, int width, int height, Component label, Button.OnPress onPress) {
        return new SilentPageButton(x, y, width, height, label, onPress);
    }

    private static final class SilentPageButton extends Button {
        private SilentPageButton(int x, int y, int width, int height, Component label, OnPress onPress) {
            super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
        }
    }

    /** Same UI sound as vanilla book & quill / written book page turns. */
    private void playPageTurnSound() {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }

    private void reloadAllFoods() {
        allFoods.clear();
        allFoods.addAll(FoodPool.collectAvailableFoods());
        allFoods.sort((a, b) -> {
            var ka = ForgeRegistries.ITEMS.getKey(a);
            var kb = ForgeRegistries.ITEMS.getKey(b);
            if (ka == null || kb == null) {
                return 0;
            }
            return ka.compareTo(kb);
        });
    }

    private void applyFilter() {
        filtered.clear();
        for (Item item : allFoods) {
            ItemStack stack = new ItemStack(item);
            String id = FoodPool.toId(stack);
            String name = stack.getHoverName().getString();
            if (OrderBookListFilter.matches(query, id, name)) {
                filtered.add(item);
            }
        }
        int maxPage = Math.max(0, (filtered.size() - 1) / pageSize());
        if (page > maxPage) {
            page = maxPage;
        }
    }

    /** Select all filtered foods; if all already selected, clear those instead. */
    private void toggleSelectAllFiltered() {
        boolean allSelected = true;
        for (Item item : filtered) {
            String id = FoodPool.toId(new ItemStack(item));
            if (id.isEmpty() || !selected.contains(id)) {
                allSelected = false;
                break;
            }
        }
        if (allSelected) {
            for (Item item : filtered) {
                selected.remove(FoodPool.toId(new ItemStack(item)));
            }
        } else {
            for (Item item : filtered) {
                String id = FoodPool.toId(new ItemStack(item));
                if (!id.isEmpty()) {
                    selected.add(id);
                }
            }
        }
    }

    private Component modeLabel() {
        String key = mode == OrderBookFilterMode.WHITELIST
                ? "gui.touhou_maid_eat_properly.order_book.whitelist"
                : "gui.touhou_maid_eat_properly.order_book.blacklist";
        return Component.translatable(key);
    }

    private int pageSize() {
        return COLUMNS * ROWS * 2;
    }

    private int cellsPerSide() {
        return COLUMNS * ROWS;
    }

    private int cellX(int indexOnSpread) {
        boolean right = indexOnSpread >= cellsPerSide();
        int local = indexOnSpread % cellsPerSide();
        int col = local % COLUMNS;
        int baseX = right ? RIGHT_GRID_X : LEFT_GRID_X;
        return leftPos + baseX + col * CELL;
    }

    private int cellY(int indexOnSpread) {
        int local = indexOnSpread % cellsPerSide();
        int row = local / COLUMNS;
        return topPos + GRID_TOP + row * CELL;
    }

    private int maxPageIndex() {
        if (filtered.isEmpty()) {
            return 0;
        }
        return (filtered.size() - 1) / pageSize();
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(BG, leftPos, topPos, 0, TEX_V, PANEL_W, PANEL_H);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        String pageLabel = (page + 1) + " / " + (maxPageIndex() + 1);
        graphics.drawString(font, pageLabel, leftPos + 110, topPos + 150, 0x5A3A22, false);

        ItemStack hovered = ItemStack.EMPTY;
        int start = page * pageSize();
        for (int index = 0; index < pageSize() && start + index < filtered.size(); index++) {
            Item item = filtered.get(start + index);
            int x = cellX(index);
            int y = cellY(index);
            ItemStack icon = new ItemStack(item);
            String id = FoodPool.toId(icon);
            if (selected.contains(id)) {
                graphics.fill(x - 1, y - 1, x + 17, y + 17, SELECT_FILL);
            }
            graphics.renderItem(icon, x, y);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                hovered = icon;
            }
        }
        if (!hovered.isEmpty()) {
            graphics.renderTooltip(font, hovered, mouseX, mouseY);
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int start = page * pageSize();
        for (int index = 0; index < pageSize() && start + index < filtered.size(); index++) {
            int x = cellX(index);
            int y = cellY(index);
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                String id = FoodPool.toId(new ItemStack(filtered.get(start + index)));
                if (!selected.remove(id)) {
                    selected.add(id);
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchBox != null && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (searchBox != null && searchBox.isFocused() && minecraft != null
                && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (searchBox != null && searchBox.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void onClose() {
        ModNetwork.CHANNEL.sendToServer(new SaveOrderBookPacket(menu.getHand(), mode, selected));
        super.onClose();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title/inventory labels hidden; page index drawn in render().
    }
}
