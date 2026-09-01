package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.blockentity.CutterBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.menu.CutterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class CutterScreen extends AbstractContainerScreen<CutterMenu> {
    private static final ResourceLocation EMPTY_TOOL = new ResourceLocation(
            "easyfarmersdelightcompat", "textures/item/empty_knife_slot.png");
    private ToolSlotTooltipRenderer.Examples cuttingToolExamples;

    public CutterScreen(CutterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 164;
        inventoryLabelX = 8;
        inventoryLabelY = 71;
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
                && hoveredSlot.index == CutterMenu.TOOL_SLOT
                && !hoveredSlot.hasItem()) {
            if (cuttingToolExamples == null) {
                cuttingToolExamples = ToolSlotTooltipRenderer.cuttingExamples();
            }
            ToolSlotTooltipRenderer.renderCuttingTools(
                    graphics, mouseX, mouseY, width, height, cuttingToolExamples);
            return;
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        ClientScreenStyle.panel(graphics, x, y, imageWidth, imageHeight);
        ClientScreenStyle.row(graphics, x + 52, y + 20, 4);
        ClientScreenStyle.slot(graphics, x + 142, y + 20);
        ClientScreenStyle.row(graphics, x + 52, y + 51, 4);
        ClientScreenStyle.playerInventory(graphics, x + 8, y + 83);
        if (!menu.getSlot(CutterMenu.TOOL_SLOT).hasItem()) {
            graphics.blit(EMPTY_TOOL, x + 142, y + 20, 0, 0, 16, 16, 16, 16);
        }
        int progressWidth = Math.round(16.0F * menu.progress() / CutterBlockEntity.PROCESS_TICKS);
        graphics.fill(x + 142, y + 42, x + 142 + progressWidth, y + 44, 0xFF6B8E23);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 9, 0x404040, false);
        center(graphics, Component.translatable("gui.easyfarmersdelightcompat.cutter.input"), 88, 9);
        center(graphics, Component.translatable("gui.easyfarmersdelightcompat.cutter.output"), 88, 40);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    private void center(GuiGraphics graphics, Component text, int x, int y) {
        graphics.drawString(font, text, x - font.width(text) / 2, y, 0x404040, false);
    }
}
