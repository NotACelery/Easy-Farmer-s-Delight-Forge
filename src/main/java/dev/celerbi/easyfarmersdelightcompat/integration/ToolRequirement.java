package dev.celerbi.easyfarmersdelightcompat.integration;

import net.minecraft.world.item.ItemStack;

/**
 * A gameplay-facing description of the tool category required by the operation
 * that is currently ready to continue.
 *
 * This is deliberately narrower than "accepted tool". A Rich Farmer accepts a
 * Hoe, Knife and Axe, but a mature Mushroom Colony specifically requires a
 * Knife, while a ready Melon/Pumpkin fruit specifically requires an Axe.
 */
public enum ToolRequirement {
    NONE,
    KNIFE,
    AXE,
    KNIFE_OR_AXE;

    public boolean isRequired() {
        return this != NONE;
    }

    public boolean isSatisfiedBy(ItemStack stack) {
        return switch (this) {
            case NONE -> true;
            case KNIFE -> FarmerToolSupport.isKnife(stack);
            case AXE -> FarmerToolSupport.isAxe(stack);
            case KNIFE_OR_AXE -> FarmerToolSupport.isKnife(stack) || FarmerToolSupport.isAxe(stack);
        };
    }

    public static ToolRequirement from(boolean knife, boolean axe) {
        if (knife && axe) return KNIFE_OR_AXE;
        if (knife) return KNIFE;
        if (axe) return AXE;
        return NONE;
    }
}
