package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.menu.RichFarmerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class RichFarmerScreen extends AbstractContainerScreen<RichFarmerMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("easy_villagers", "textures/gui/container/output.png");
    private ToolSlotTooltipRenderer.Examples harvestToolExamples;

    public RichFarmerScreen(RichFarmerMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        imageWidth = 176;
        imageHeight = 133;
        inventoryLabelY = 40;
        titleLabelY = 9;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty()
                && hoveredSlot != null
                && hoveredSlot.index == RichFarmerMenu.HARVEST_TOOL_SLOT
                && !hoveredSlot.hasItem()) {
            if (harvestToolExamples == null) harvestToolExamples = ToolSlotTooltipRenderer.harvestExamples();
            ToolSlotTooltipRenderer.renderHarvestTools(graphics, mouseX, mouseY, width, height, harvestToolExamples);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        graphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight);
        graphics.blit(BACKGROUND, x + 141, y + 19, 51, 19, 18, 18);
    }
}
