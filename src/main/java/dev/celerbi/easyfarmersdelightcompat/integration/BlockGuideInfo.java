package dev.celerbi.easyfarmersdelightcompat.integration;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * One instructional Block Guide page. The same immutable data is rendered by
 * both JEI and EMI so the two integrations cannot drift apart.
 */
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
