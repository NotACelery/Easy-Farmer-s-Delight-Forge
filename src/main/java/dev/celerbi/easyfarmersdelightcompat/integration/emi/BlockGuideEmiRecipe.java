package dev.celerbi.easyfarmersdelightcompat.integration.emi;

import dev.celerbi.easyfarmersdelightcompat.integration.BlockGuideInfo;
import dev.celerbi.easyfarmersdelightcompat.integration.GuideIngredient;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

/** EMI renderer for one shared Block Guide page. */
public final class BlockGuideEmiRecipe implements EmiRecipe {
    private static final int WIDTH = 180;
    private static final int HEIGHT = 180;
    private static final int SLOT_STEP = 28;

    private final BlockGuideInfo info;

    public BlockGuideEmiRecipe(BlockGuideInfo info) {
        this.info = info;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return EasyFdEmiPlugin.BLOCK_GUIDE;
    }

    @Override
    public ResourceLocation getId() {
        return info.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        // A Block Guide page is documentation for a machine block, not a use of
        // every ingredient shown inside the page. Keeping this empty prevents
        // Sand, crops and tools from opening the guide through EMI's Uses view.
        return List.of();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        // The machine is rendered visually as a catalyst, but it must not be an
        // EMI lookup catalyst/workstation. Otherwise right-clicking the machine
        // routes to Uses instead of placing Block Guide beside Crafting.
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        // EMI's Recipes view is sourced from getOutputs(). The guide's machine
        // catalysts therefore act as semantic source targets: opening Recipes
        // for Paddy Farmer, Rich Farmer, Cutter, etc. groups this category next
        // to the normal Crafting category for that exact block.
        List<EmiStack> result = new ArrayList<>();
        for (GuideIngredient ingredient : info.ingredients()) {
            if (ingredient.role() != GuideIngredient.Role.CATALYST) continue;
            Arrays.stream(ingredient.ingredient().getItems())
                    .filter(stack -> !stack.isEmpty())
                    .map(EmiStack::of)
                    .forEach(result::add);
        }
        return List.copyOf(result);
    }

    @Override
    public int getDisplayWidth() {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight() {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int count = info.ingredients().size();
        int totalWidth = count <= 0 ? 0 : 16 + (count - 1) * SLOT_STEP;
        int startX = Math.max(4, (WIDTH - totalWidth) / 2);

        for (int i = 0; i < count; i++) {
            GuideIngredient ingredient = info.ingredients().get(i);
            SlotWidget slot = widgets.addSlot(
                    EmiIngredient.of(ingredient.ingredient()),
                    startX + i * SLOT_STEP,
                    4
            );
            if (ingredient.role() == GuideIngredient.Role.CATALYST
                    || ingredient.role() == GuideIngredient.Role.TOOL) {
                slot.catalyst(true);
            }
            if (!ingredient.label().getString().isEmpty()) {
                slot.appendTooltip(ingredient.label());
            }
        }

        widgets.addText(info.title(), 4, 30, 0x404040, false);
        EmiTextUtil.addWrappedParagraphs(widgets, info.lines(), 4, 45, WIDTH - 8, 0x555555);
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
