package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.integration.FarmerToolSupport;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ToolSlotTooltipRenderer {
    private static final int ICON = 16;
    private static final int GAP = 2;
    private static final int HEIGHT = 18;

    public static final class Examples {
        private final List<List<ItemStack>> categories;

        private Examples(List<List<ItemStack>> categories) {
            this.categories = categories;
        }
    }

    private ToolSlotTooltipRenderer() {
    }

    public static Examples harvestExamples() {
        Examples tagged = snapshot(FarmerToolSupport.HARVEST_TOOL_CATEGORIES);
        List<List<ItemStack>> categories = new ArrayList<>(tagged.categories);
        categories.add(List.of(new ItemStack(Items.SHEARS)));
        return new Examples(List.copyOf(categories));
    }

    public static Examples cuttingExamples() {
        return snapshot(FarmerToolSupport.CUTTING_TOOL_CATEGORIES);
    }

    public static void renderHarvestTools(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int screenWidth,
            int screenHeight,
            Examples examples
    ) {
        render(graphics, mouseX, mouseY, screenWidth, screenHeight, examples);
    }

    public static void renderCuttingTools(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int screenWidth,
            int screenHeight,
            Examples examples
    ) {
        render(graphics, mouseX, mouseY, screenWidth, screenHeight, examples);
    }

    private static Examples snapshot(List<TagKey<Item>> categories) {
        List<List<ItemStack>> snapshot = new ArrayList<>(categories.size());
        for (TagKey<Item> tag : categories) {
            snapshot.add(FarmerToolSupport.taggedToolStacks(tag));
        }
        return new Examples(List.copyOf(snapshot));
    }

    private static void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int screenWidth,
            int screenHeight,
            Examples examples
    ) {
        if (examples == null)
            return;

        List<ItemStack> shown = new ArrayList<>(examples.categories.size());
        long second = Util.getMillis() / 1000L;
        for (int categoryIndex = 0; categoryIndex < examples.categories.size(); categoryIndex++) {
            List<ItemStack> candidates = examples.categories.get(categoryIndex);
            if (candidates.isEmpty())
                continue;

            long phase = 7L * (categoryIndex + 1L) * (categoryIndex + 2L);
            int index = (int) Math.floorMod(second + phase, candidates.size());
            shown.add(candidates.get(index));
        }
        if (shown.isEmpty())
            return;

        int width = shown.size() * ICON + Math.max(0, shown.size() - 1) * GAP;
        int x = mouseX + 12;
        int y = mouseY - 12;
        if (x + width + 6 > screenWidth)
            x = mouseX - width - 12;
        if (y + HEIGHT + 6 > screenHeight)
            y = screenHeight - HEIGHT - 6;
        if (y < 6)
            y = 6;

        TooltipRenderUtil.renderTooltipBackground(graphics, x, y, width, HEIGHT, 400);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 410.0F);
        for (int i = 0; i < shown.size(); i++) {
            graphics.renderItem(shown.get(i), x + i * (ICON + GAP), y + 1);
        }
        graphics.pose().popPose();
    }
}
