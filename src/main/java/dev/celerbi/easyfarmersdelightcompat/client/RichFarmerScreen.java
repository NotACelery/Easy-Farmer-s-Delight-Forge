package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.menu.RichFarmerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RichFarmerScreen extends AbstractContainerScreen<RichFarmerMenu> {
    private ToolSlotTooltipRenderer.Examples harvestToolExamples;

    public RichFarmerScreen(RichFarmerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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
            if (harvestToolExamples == null) {
                harvestToolExamples = ToolSlotTooltipRenderer.harvestExamples();
            }
            ToolSlotTooltipRenderer.renderHarvestTools(
                    graphics, mouseX, mouseY, width, height, harvestToolExamples);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ClientScreenStyle.panel(graphics, leftPos, topPos, imageWidth, imageHeight);
        ClientScreenStyle.row(graphics, leftPos + 52, topPos + 20, 4);
        ClientScreenStyle.slot(graphics, leftPos + 142, topPos + 20);
        ClientScreenStyle.playerInventory(graphics, leftPos + 8, topPos + 51);
    }
}
