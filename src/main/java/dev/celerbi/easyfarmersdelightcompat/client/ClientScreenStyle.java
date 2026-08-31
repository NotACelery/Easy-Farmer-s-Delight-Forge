package dev.celerbi.easyfarmersdelightcompat.client;

import net.minecraft.client.gui.GuiGraphics;

public final class ClientScreenStyle {
    private static final int PANEL_BORDER = 0xFF2D2D2D;
    private static final int PANEL_EDGE = 0xFF9A9A9A;
    private static final int PANEL_FACE = 0xFFC6C6C6;
    private static final int SLOT_DARK = 0xFF555555;
    private static final int SLOT_LIGHT = 0xFFFFFFFF;
    private static final int SLOT_FACE = 0xFF8B8B8B;

    private ClientScreenStyle() {
    }

    public static void panel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_EDGE);
        graphics.fill(x + 3, y + 3, x + width - 3, y + height - 3, PANEL_FACE);
    }

    public static void slot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_DARK);
        graphics.fill(x, y, x + 17, y + 17, SLOT_LIGHT);
        graphics.fill(x, y, x + 16, y + 16, SLOT_FACE);
    }

    public static void row(GuiGraphics graphics, int x, int y, int count) {
        for (int index = 0; index < count; index++) {
            slot(graphics, x + index * 18, y);
        }
    }

    public static void playerInventory(GuiGraphics graphics, int x, int y) {
        for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
            row(graphics, x, y + rowIndex * 18, 9);
        }
        row(graphics, x, y + 58, 9);
    }
}
