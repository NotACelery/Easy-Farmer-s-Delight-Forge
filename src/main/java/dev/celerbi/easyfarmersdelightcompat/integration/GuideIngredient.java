package dev.celerbi.easyfarmersdelightcompat.integration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Viewer-neutral ingredient descriptor shared by JEI and EMI Block Guide pages.
 * No viewer-specific API types are allowed here.
 */
public record GuideIngredient(Ingredient ingredient, Role role, Component label) {
    public enum Role {
        INPUT,
        TOOL,
        OUTPUT,
        CATALYST
    }

    public GuideIngredient {
        if (ingredient == null) {
            ingredient = Ingredient.EMPTY;
        }
        if (role == null) {
            role = Role.INPUT;
        }
        if (label == null) {
            label = Component.empty();
        }
    }
}
