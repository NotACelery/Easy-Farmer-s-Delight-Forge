package dev.celerbi.easyfarmersdelightcompat.integration.emi;

import dev.celerbi.easyfarmersdelightcompat.integration.FarmerHarvestInfo;
import dev.celerbi.easyfarmersdelightcompat.integration.ToolUse;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FarmerHarvestEmiRecipe implements EmiRecipe {
    private final FarmerHarvestInfo info;
    private final EmiRecipeCategory category;
    private final EmiIngredient input;
    private final EmiIngredient tool;
    private final List<EmiStack> outputs;

    public FarmerHarvestEmiRecipe(FarmerHarvestInfo info, EmiRecipeCategory category) {
        this.info = info;
        this.category = category;
        this.input = EmiIngredient.of(info.input());
        this.tool = info.hasTool() ? EmiIngredient.of(info.tool()) : EmiStack.EMPTY;
        this.outputs = info.outputs().stream().map(EmiStack::of).toList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public ResourceLocation getId() {
        return info.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(input);
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return info.hasTool() ? List.of(tool) : List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 180;
    }

    @Override
    public int getDisplayHeight() {
        return 106;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(input, 8, 6);

        if (info.hasTool()) {
            widgets.addSlot(tool, 48, 6)
                    .catalyst(true)
                    .appendTooltip(Component.translatable(info.toolUse() == ToolUse.REQUIRED
                            ? "easyfarmersdelightcompat.viewer.tool.required"
                            : "easyfarmersdelightcompat.viewer.tool.optional"))
                    .appendTooltip(Component.translatable(info.toolDamaged()
                            ? "easyfarmersdelightcompat.viewer.tool.damaged"
                            : "easyfarmersdelightcompat.viewer.tool.not_damaged"));
        }

        if (!outputs.isEmpty()) {
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 76, 6);
            for (int i = 0; i < Math.min(4, outputs.size()); i++) {
                widgets.addSlot(outputs.get(i), 108 + i * 18, 6).recipeContext(this);
            }
        }

        EmiTextUtil.addWrappedParagraphs(widgets, List.of(info.description()), 4, 36, 172, 0x555555);
    }

    @Override
    public boolean supportsRecipeTree() {

        return false;
    }
}
