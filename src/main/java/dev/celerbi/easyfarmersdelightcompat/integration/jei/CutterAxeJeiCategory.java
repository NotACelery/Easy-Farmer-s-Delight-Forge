package dev.celerbi.easyfarmersdelightcompat.integration.jei;

import dev.celerbi.easyfarmersdelightcompat.integration.CutterAxeActionRow;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterAxeInfo;
import dev.celerbi.easyfarmersdelightcompat.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class CutterAxeJeiCategory implements IRecipeCategory<CutterAxeInfo> {
    private static final int WIDTH = 150;
    private static final int HEIGHT = 58;
    private static final int INPUT_X = 12;
    private static final int TOOL_X = 50;
    private static final int OUTPUT_X = 116;
    private static final int FIRST_Y = 6;
    private static final int ROW_STEP = 27;

    private final IDrawable icon;

    public CutterAxeJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.CUTTER_ITEM.get()));
    }

    @Override
    public RecipeType<CutterAxeInfo> getRecipeType() {
        return EasyFdJeiPlugin.CUTTER_AXE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.easyfarmersdelightcompat.cutter_axe");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CutterAxeInfo recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.actions().size(); i++) {
            CutterAxeActionRow row = recipe.actions().get(i);
            int y = FIRST_Y + i * ROW_STEP;

            builder.addInputSlot(INPUT_X, y)
                    .addIngredients(row.input())
                    .setStandardSlotBackground()
                    .addRichTooltipCallback((slotView, tooltip) -> tooltip.add(row.inputLabel()));

            builder.addSlot(RecipeIngredientRole.CATALYST, TOOL_X, y)
                    .addIngredients(recipe.tool())
                    .setStandardSlotBackground();

            builder.addOutputSlot(OUTPUT_X, y)
                    .addIngredients(row.output())
                    .setStandardSlotBackground();
        }
    }

    @Override
    public void draw(CutterAxeInfo recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        for (int i = 0; i < recipe.actions().size(); i++) {
            int y = FIRST_Y + i * ROW_STEP;
            graphics.drawString(font, "+", 38, y + 5, 0x404040, false);
            graphics.drawString(font, "→", 88, y + 5, 0x404040, false);
        }
    }
}
