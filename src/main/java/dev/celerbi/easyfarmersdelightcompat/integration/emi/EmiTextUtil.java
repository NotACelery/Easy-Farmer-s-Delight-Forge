package dev.celerbi.easyfarmersdelightcompat.integration.emi;

import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class EmiTextUtil {
    private static final int LINE_HEIGHT = 9;

    private EmiTextUtil() {
    }

    public static int addWrapped(WidgetHolder widgets, Component text, int x, int y, int width, int color) {
        var font = Minecraft.getInstance().font;
        int currentY = y;
        for (var line : font.split(text, width)) {
            if (currentY > widgets.getHeight() - LINE_HEIGHT) {
                return currentY;
            }
            widgets.addText(line, x, currentY, color, false);
            currentY += LINE_HEIGHT;
        }
        return currentY;
    }

    public static int addWrappedParagraphs(
            WidgetHolder widgets,
            List<Component> paragraphs,
            int x,
            int y,
            int width,
            int color
    ) {
        int currentY = y;
        for (Component paragraph : paragraphs) {
            currentY = addWrapped(widgets, paragraph, x, currentY, width, color);
            currentY += 2;
            if (currentY > widgets.getHeight() - LINE_HEIGHT) {
                break;
            }
        }
        return currentY;
    }
}
