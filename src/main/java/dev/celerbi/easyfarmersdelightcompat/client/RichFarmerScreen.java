package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.blockentity.CompatFarmerBlockEntity;
import dev.celerbi.easyfarmersdelightcompat.menu.RichFarmerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class RichFarmerScreen extends AbstractContainerScreen<RichFarmerMenu> {
    private static final int ATTACHED_PANEL_X = 184;
    private static final int HOST_SLOT_X = 188;
    private static final int FACE_SLOT_X = 214;
    private static final int SLOT_STEP = 18;

    private ToolSlotTooltipRenderer.Examples harvestToolExamples;

    public RichFarmerScreen(RichFarmerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 288;
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

        graphics.fill(leftPos + 178, topPos + 5, leftPos + 180, topPos + imageHeight - 5, 0xFF777777);
        renderAttachedPanel(graphics);
    }

    private void renderAttachedPanel(GuiGraphics graphics) {
        graphics.drawString(
                font,
                Component.translatable("easyfarmersdelightcompat.rich_farmer.attached.title"),
                leftPos + ATTACHED_PANEL_X,
                topPos + 8,
                0x404040,
                false
        );

        CompatFarmerBlockEntity farmer = farmer();
        renderAttachedLevel(graphics, farmer, 0, topPos + 32,
                "easyfarmersdelightcompat.rich_farmer.attached.lower");
        renderAttachedLevel(graphics, farmer, 1, topPos + 75,
                "easyfarmersdelightcompat.rich_farmer.attached.upper");
    }

    private void renderAttachedLevel(
            GuiGraphics graphics,
            CompatFarmerBlockEntity farmer,
            int levelIndex,
            int slotY,
            String labelKey
    ) {
        graphics.drawString(font, Component.translatable(labelKey), leftPos + ATTACHED_PANEL_X, slotY - 11,
                0x555555, false);

        ClientScreenStyle.slot(graphics, leftPos + HOST_SLOT_X, slotY);
        for (int faceIndex = 0; faceIndex < 4; faceIndex++) {
            ClientScreenStyle.slot(graphics, leftPos + FACE_SLOT_X + faceIndex * SLOT_STEP, slotY);
        }

        if (farmer != null) {
            renderBlockState(graphics, farmer.attachedHostState(levelIndex), leftPos + HOST_SLOT_X, slotY);
            for (int faceIndex = 0; faceIndex < 4; faceIndex++) {
                int x = leftPos + FACE_SLOT_X + faceIndex * SLOT_STEP;
                BlockState crop = farmer.attachedCropState(levelIndex, faceIndex);
                renderBlockState(graphics, crop, x, slotY);
                if (!crop.isAir()) {
                    graphics.drawString(font, Integer.toString(farmer.attachedCropAge(levelIndex, faceIndex)),
                            x + 10, slotY + 9, 0xFFFFFF, true);
                }
            }
        }

        String[] faces = {"N", "S", "E", "W"};
        for (int faceIndex = 0; faceIndex < faces.length; faceIndex++) {
            graphics.drawString(font, faces[faceIndex],
                    leftPos + FACE_SLOT_X + faceIndex * SLOT_STEP + 5, slotY + 19, 0x666666, false);
        }
    }

    private static void renderBlockState(GuiGraphics graphics, BlockState state, int x, int y) {
        if (state == null || state.isAir()) {
            return;
        }
        ItemStack stack = new ItemStack(state.getBlock());
        if (!stack.isEmpty()) {
            graphics.renderItem(stack, x, y);
        }
    }

    private CompatFarmerBlockEntity farmer() {
        if (Minecraft.getInstance().level == null) {
            return null;
        }
        BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(menu.blockPos());
        return blockEntity instanceof CompatFarmerBlockEntity farmer ? farmer : null;
    }
}
