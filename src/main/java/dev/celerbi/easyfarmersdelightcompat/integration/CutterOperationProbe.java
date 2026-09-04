package dev.celerbi.easyfarmersdelightcompat.integration;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class CutterOperationProbe {
    public record ToolSamples(
            List<ItemStack> knives,
            List<ItemStack> pickaxes,
            List<ItemStack> axes,
            List<ItemStack> shovels,
            List<ItemStack> hoes,
            List<ItemStack> shears
    ) {
        public ToolSamples {
            knives = List.copyOf(knives);
            pickaxes = List.copyOf(pickaxes);
            axes = List.copyOf(axes);
            shovels = List.copyOf(shovels);
            hoes = List.copyOf(hoes);
            shears = List.copyOf(shears);
        }
    }

    public record Result(Set<ToolRequirement> supportedRequirements) {
        public Result {
            supportedRequirements = supportedRequirements.isEmpty()
                    ? Set.of()
                    : Set.copyOf(supportedRequirements);
        }

        public boolean supports(ItemStack stack) {
            return supportedRequirements.stream().anyMatch(requirement -> requirement.isSatisfiedBy(stack));
        }

        public ToolRequirement requirement() {
            if (supportedRequirements.isEmpty()) {
                return ToolRequirement.NONE;
            }
            if (supportedRequirements.size() == 1) {
                return supportedRequirements.iterator().next();
            }
            if (supportedRequirements.size() == 2
                    && supportedRequirements.contains(ToolRequirement.KNIFE)
                    && supportedRequirements.contains(ToolRequirement.AXE)) {
                return ToolRequirement.KNIFE_OR_AXE;
            }
            return ToolRequirement.CUTTING_TOOL;
        }

        public boolean processable() {
            return !supportedRequirements.isEmpty();
        }
    }

    private CutterOperationProbe() {
    }

    public static ToolSamples representativeTools() {
        return new ToolSamples(
                FarmerToolSupport.representativeKnives(),
                FarmerToolSupport.representativePickaxes(),
                FarmerToolSupport.representativeAxes(),
                FarmerToolSupport.representativeShovels(),
                FarmerToolSupport.representativeHoes(),
                FarmerToolSupport.representativeShears()
        );
    }

    public static Result probe(Level level, ItemStack input) {
        return probe(level, input, representativeTools());
    }

    public static Result probe(Level level, ItemStack input, ToolSamples tools) {
        if (level == null || input == null || input.isEmpty() || tools == null) {
            return new Result(Set.of());
        }

        EnumSet<ToolRequirement> requirements = EnumSet.noneOf(ToolRequirement.class);
        addRecipeRequirement(level, input, tools.knives(), ToolRequirement.KNIFE, requirements);
        addRecipeRequirement(level, input, tools.pickaxes(), ToolRequirement.PICKAXE, requirements);
        addRecipeRequirement(level, input, tools.axes(), ToolRequirement.AXE, requirements);
        addRecipeRequirement(level, input, tools.shovels(), ToolRequirement.SHOVEL, requirements);
        addRecipeRequirement(level, input, tools.hoes(), ToolRequirement.HOE, requirements);
        addRecipeRequirement(level, input, tools.shears(), ToolRequirement.SHEARS, requirements);

        if (tools.axes().stream().anyMatch(axe -> AxeActionResolver.resolve(input, axe).isPresent())) {
            requirements.add(ToolRequirement.AXE);
        }

        return new Result(requirements);
    }

    private static void addRecipeRequirement(
            Level level,
            ItemStack input,
            List<ItemStack> tools,
            ToolRequirement requirement,
            Set<ToolRequirement> requirements
    ) {
        if (CuttingRecipeResolver.hasMatchingRecipe(level, input, tools)) {
            requirements.add(requirement);
        }
    }
}
