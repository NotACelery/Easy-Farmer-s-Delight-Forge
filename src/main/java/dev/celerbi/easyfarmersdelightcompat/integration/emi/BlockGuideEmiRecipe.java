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

        return List.of();
    }

    @Override
    public List<EmiIngredient> getCatalysts() {

        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {

        List<EmiStack> result = new ArrayList<>();
        for (GuideIngredient ingredient : info.ingredients()) {
            if (ingredient.role() != GuideIngredient.Role.CATALYST)
                continue;
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
        boolean ironFarmAssembly = isIronFarmAssembly();
        int totalWidth = count <= 0 ? 0 : 16 + (count - 1) * SLOT_STEP;
        int startX = Math.max(4, (WIDTH - totalWidth) / 2);
        int[] assemblyX = {
            8, 58, 94, 150
        };

        for (int i = 0; i < count; i++) {
            GuideIngredient ingredient = info.ingredients().get(i);
            EmiIngredient display = ingredient.displayStack().isEmpty()
                    ? EmiIngredient.of(ingredient.ingredient())
                    : EmiStack.of(ingredient.displayStack());
            SlotWidget slot = widgets.addSlot(
                    display,
                    ironFarmAssembly ? assemblyX[i] : startX + i * SLOT_STEP,
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

        if (ironFarmAssembly) {
            widgets.addText(net.minecraft.network.chat.Component.literal("+"), 82, 9, 0x555555, false);
            widgets.addText(net.minecraft.network.chat.Component.literal("→"), 126, 9, 0x555555, false);
        }

        widgets.addText(info.title(), 4, 30, 0x404040, false);
        EmiTextUtil.addWrappedParagraphs(widgets, info.lines(), 4, 45, WIDTH - 8, 0x555555);
    }

    private boolean isIronFarmAssembly() {
        return "block_guide/iron_farm_noise_switch".equals(info.id().getPath());
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }
}
