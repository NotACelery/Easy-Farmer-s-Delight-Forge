package dev.celerbi.easyfarmersdelightcompat.integration.jei;

import dev.celerbi.easyfarmersdelightcompat.integration.BlockGuideInfo;
import dev.celerbi.easyfarmersdelightcompat.integration.GuideIngredient;
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

public final class BlockGuideJeiCategory implements IRecipeCategory<BlockGuideInfo> {
    private static final int WIDTH = 180;
    private static final int HEIGHT = 180;
    private static final int SLOT_STEP = 28;

    private final IDrawable icon;

    public BlockGuideJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModBlocks.RICH_FARMER_ITEM.get()));
    }

    @Override
    public RecipeType<BlockGuideInfo> getRecipeType() {
        return EasyFdJeiPlugin.BLOCK_GUIDE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.easyfarmersdelightcompat.block_guide");
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
    public void setRecipe(IRecipeLayoutBuilder builder, BlockGuideInfo recipe, IFocusGroup focuses) {
        int count = recipe.ingredients().size();
        boolean ironFarmAssembly = isIronFarmAssembly(recipe);
        int totalWidth = count <= 0 ? 0 : 16 + (count - 1) * SLOT_STEP;
        int startX = Math.max(4, (WIDTH - totalWidth) / 2);
        int[] assemblyX = {
            8, 58, 94, 150
        };

        for (int i = 0; i < count; i++) {
            GuideIngredient guideIngredient = recipe.ingredients().get(i);
            RecipeIngredientRole role = switch (guideIngredient.role()) {
                case INPUT -> RecipeIngredientRole.INPUT;
                case OUTPUT -> RecipeIngredientRole.OUTPUT;
                case TOOL, CATALYST -> RecipeIngredientRole.CATALYST;
            };

            var slot = builder.addSlot(role, ironFarmAssembly ? assemblyX[i] : startX + i * SLOT_STEP, 6);
            if (!guideIngredient.displayStack().isEmpty()) {
                slot.addItemStack(guideIngredient.displayStack());
            } else {
                slot.addIngredients(guideIngredient.ingredient());
            }
            slot.setStandardSlotBackground()
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        if (!guideIngredient.label().getString().isEmpty()) {
                            tooltip.add(guideIngredient.label());
                        }
                    });
        }
    }

    @Override
    public void draw(BlockGuideInfo recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
            double mouseY) {
        var font = Minecraft.getInstance().font;

        if (isIronFarmAssembly(recipe)) {
            graphics.drawString(font, "+", 82, 10, 0x555555, false);
            graphics.drawString(font, "→", 126, 10, 0x555555, false);
        }

        int titleWidth = font.width(recipe.title());
        graphics.drawString(
                font,
                recipe.title(),
                Math.max(4, (WIDTH - titleWidth) / 2),
                31,
                0x404040,
                false
        );

        int y = 47;
        for (Component paragraph : recipe.lines()) {
            for (var line : font.split(paragraph, WIDTH - 10)) {
                if (y > HEIGHT - 9)
                    return;
                graphics.drawString(font, line, 5, y, 0x555555, false);
                y += 9;
            }
            y += 2;
        }
    }

    private static boolean isIronFarmAssembly(BlockGuideInfo recipe) {
        return "block_guide/iron_farm_noise_switch".equals(recipe.id().getPath());
    }
}
