package dev.celerbi.easyfarmersdelightcompat.integration;

import net.minecraft.world.item.ItemStack;

public enum ToolRequirement {
    NONE,
    KNIFE,
    PICKAXE,
    AXE,
    SHOVEL,
    HOE,
    SHEARS,
    KNIFE_OR_AXE,
    CUTTING_TOOL;

    public boolean isRequired() {
        return this != NONE;
    }

    public boolean isSatisfiedBy(ItemStack stack) {
        return switch (this) {
            case NONE -> true;
            case KNIFE -> FarmerToolSupport.isKnife(stack);
            case PICKAXE -> FarmerToolSupport.isPickaxe(stack);
            case AXE -> FarmerToolSupport.isAxe(stack);
            case SHOVEL -> FarmerToolSupport.isShovel(stack);
            case HOE -> FarmerToolSupport.isHoe(stack);
            case SHEARS -> FarmerToolSupport.isShears(stack);
            case KNIFE_OR_AXE -> FarmerToolSupport.isKnife(stack) || FarmerToolSupport.isAxe(stack);
            case CUTTING_TOOL -> FarmerToolSupport.isCuttingTool(stack);
        };
    }
}
