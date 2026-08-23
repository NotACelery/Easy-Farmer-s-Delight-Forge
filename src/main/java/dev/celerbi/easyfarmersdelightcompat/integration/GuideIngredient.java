package dev.celerbi.easyfarmersdelightcompat.integration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Viewer-neutral ingredient descriptor shared by JEI and EMI Block Guide pages.
 * No viewer-specific API types are allowed here.
 */
public record GuideIngredient(Ingredient ingredient, Role role, Component label, ItemStack displayStack) {
    public enum Role {
        INPUT,
        TOOL,
        OUTPUT,
        CATALYST
    }

    public GuideIngredient(Ingredient ingredient, Role role, Component label) {
        this(ingredient, role, label, ItemStack.EMPTY);
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
        if (displayStack == null) {
            displayStack = ItemStack.EMPTY;
        } else if (!displayStack.isEmpty()) {
            displayStack = displayStack.copy();
        }
    }
}
