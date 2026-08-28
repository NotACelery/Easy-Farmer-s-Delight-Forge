package dev.celerbi.easyfarmersdelightcompat.integration.emi;

import dev.celerbi.easyfarmersdelightcompat.integration.CutterAxeActionRow;
import dev.celerbi.easyfarmersdelightcompat.integration.CutterAxeInfo;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class CutterAxeEmiRecipe implements EmiRecipe {
    private static final int INPUT_X = 12;
    private static final int TOOL_X = 50;
    private static final int ARROW_X = 78;
    private static final int OUTPUT_X = 116;
    private static final int FIRST_Y = 5;
    private static final int ROW_STEP = 27;

    private final CutterAxeInfo info;
    private final EmiIngredient tool;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> outputs;

    public CutterAxeEmiRecipe(CutterAxeInfo info) {
        this.info = info;
        this.tool = EmiIngredient.of(info.tool());
        this.inputs = info.actions().stream().map(row -> EmiIngredient.of(row.input())).toList();
        this.outputs = info.actions().stream().map(row -> EmiIngredient.of(row.output())).toList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EasyFdEmiPlugin.CUTTER_AXE;
    }

    @Override
    public ResourceLocation getId() {
        return info.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        List<EmiIngredient> all = new ArrayList<>(inputs);
        all.add(tool);
        return List.copyOf(all);
    }

    @Override
    public List<EmiStack> getOutputs() {
        List<EmiStack> representative = new ArrayList<>();
        for (EmiIngredient output : outputs) {
            if (!output.getEmiStacks().isEmpty()) {
                representative.add(output.getEmiStacks().get(0));
            }
        }
        return List.copyOf(representative);
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 58;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < info.actions().size(); i++) {
            CutterAxeActionRow row = info.actions().get(i);
            int y = FIRST_Y + i * ROW_STEP;

            widgets.addSlot(inputs.get(i), INPUT_X, y)
                    .appendTooltip(row.inputLabel());
            widgets.addSlot(tool, TOOL_X, y);
            widgets.addTexture(EmiTexture.EMPTY_ARROW, ARROW_X, y);
            widgets.addSlot(outputs.get(i), OUTPUT_X, y);
        }
    }
}
