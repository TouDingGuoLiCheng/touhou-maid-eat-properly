package com.touhoumaid.eatproperly.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-only book-view opener for {@code HandwrittenLetterItem}. Reads the {@code pages} NBT
 * baked in by {@code HandwrittenLetterBook} directly instead of going through
 * {@code WrittenBookItem}/{@code BookViewScreen.WrittenBookAccess}, since those require the stack
 * to literally be {@code Items.WRITTEN_BOOK}.
 */
public final class HandwrittenLetterClient {
    private HandwrittenLetterClient() {
    }

    public static void open(ItemStack stack) {
        Minecraft.getInstance().setScreen(new BookViewScreen(new PageAccess(readPages(stack))));
    }

    private static List<Component> readPages(ItemStack stack) {
        List<Component> pages = new ArrayList<>();
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("pages", Tag.TAG_LIST)) {
            ListTag list = tag.getList("pages", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                pages.add(parsePage(list.getString(i)));
            }
        }
        if (pages.isEmpty()) {
            pages.add(stack.getHoverName());
        }
        return pages;
    }

    private static Component parsePage(String json) {
        try {
            Component component = Component.Serializer.fromJson(json);
            if (component != null) {
                return component;
            }
        } catch (Exception ignored) {
            // Fall through to plain text below.
        }
        return Component.literal(json);
    }

    private record PageAccess(List<Component> pages) implements BookViewScreen.BookAccess {
        @Override
        public int getPageCount() {
            return pages.size();
        }

        @Override
        public FormattedText getPageRaw(int index) {
            return index >= 0 && index < pages.size() ? pages.get(index) : FormattedText.EMPTY;
        }
    }
}
