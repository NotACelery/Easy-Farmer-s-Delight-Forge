package dev.celerbi.easyfarmersdelightcompat.integration;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public record CutterAxeInfo(
        ResourceLocation id,
        Ingredient tool,
        List<CutterAxeActionRow> actions
) {
}
