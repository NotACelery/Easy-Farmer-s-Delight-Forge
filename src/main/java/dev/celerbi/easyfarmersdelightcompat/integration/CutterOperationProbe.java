package dev.celerbi.easyfarmersdelightcompat.integration;

import java.util.List;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Non-destructive description of which supported Cutter tool categories can
 * process a pending input. Used by diagnostics/viewers without rolling outputs.
 */
public final class CutterOperationProbe {
    public record Result(boolean knifeCuttingRecipe, boolean axeCuttingRecipe, boolean axeAction) {
        public boolean knifeSupported() {
            return knifeCuttingRecipe;
        }

        public boolean axeSupported() {
            return axeCuttingRecipe || axeAction;
        }

        public ToolRequirement requirement() {
            return ToolRequirement.from(knifeSupported(), axeSupported());
        }

        public boolean processable() {
            return requirement().isRequired();
        }
    }

    private CutterOperationProbe() {
    }

    public static Result probe(Level level, ItemStack input) {
        List<ItemStack> knives = FarmerToolSupport.taggedToolStacks(FarmerToolSupport.KNIVES);
        List<ItemStack> axes = FarmerToolSupport.taggedToolStacks(ItemTags.AXES);
        return probe(level, input, knives, axes);
    }

    public static Result probe(Level level, ItemStack input, List<ItemStack> knives, List<ItemStack> axes) {
        if (level == null || input == null || input.isEmpty()) {
            return new Result(false, false, false);
        }
        List<ItemStack> safeKnives = knives == null ? List.of() : knives;
        List<ItemStack> safeAxes = axes == null ? List.of() : axes;

        boolean knifeCutting = CuttingRecipeResolver.hasMatchingRecipe(level, input, safeKnives);
        boolean axeCutting = CuttingRecipeResolver.hasMatchingRecipe(level, input, safeAxes);
        boolean axeAction = safeAxes.stream().anyMatch(axe -> AxeActionResolver.resolve(input, axe).isPresent());

        return new Result(knifeCutting, axeCutting, axeAction);
    }
}
