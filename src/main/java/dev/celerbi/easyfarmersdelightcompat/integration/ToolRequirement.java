package dev.celerbi.easyfarmersdelightcompat.integration;

import net.minecraft.world.item.ItemStack;

public enum ToolRequirement {
    NONE,
    KNIFE,
    HOE,
    AXE,
    SHEARS,
    KNIFE_OR_AXE;

    public boolean isRequired() {
        return this != NONE;
    }

    public boolean isSatisfiedBy(ItemStack stack) {
        return switch (this) {
            case NONE -> true;
            case KNIFE -> FarmerToolSupport.isKnife(stack);
            case HOE -> FarmerToolSupport.isHoe(stack);
            case AXE -> FarmerToolSupport.isAxe(stack);
            case SHEARS -> FarmerToolSupport.isShears(stack);
            case KNIFE_OR_AXE -> FarmerToolSupport.isKnife(stack) || FarmerToolSupport.isAxe(stack);
        };
    }

    public static ToolRequirement from(boolean knife, boolean axe) {
        if (knife && axe) {
            return KNIFE_OR_AXE;
        }
        if (knife) {
            return KNIFE;
        }
        if (axe) {
            return AXE;
        }
        return NONE;
    }
}
