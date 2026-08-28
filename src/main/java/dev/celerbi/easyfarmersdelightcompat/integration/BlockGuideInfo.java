package dev.celerbi.easyfarmersdelightcompat.integration;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record BlockGuideInfo(
        ResourceLocation id,
        Component title,
        List<GuideIngredient> ingredients,
        List<Component> lines
) {
    public BlockGuideInfo {
        ingredients = List.copyOf(ingredients);
        lines = List.copyOf(lines);
    }
}
