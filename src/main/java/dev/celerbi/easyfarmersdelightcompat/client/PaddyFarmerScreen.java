package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.menu.PaddyFarmerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class PaddyFarmerScreen extends AbstractContainerScreen<PaddyFarmerMenu> {
    public PaddyFarmerScreen(PaddyFarmerMenu menu, Inventory inventory, Component title) {
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
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ClientScreenStyle.panel(graphics, leftPos, topPos, imageWidth, imageHeight);
        ClientScreenStyle.row(graphics, leftPos + 52, topPos + 20, 4);
        ClientScreenStyle.playerInventory(graphics, leftPos + 8, topPos + 51);
    }
}
