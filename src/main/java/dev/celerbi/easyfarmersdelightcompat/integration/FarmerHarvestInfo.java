package dev.celerbi.easyfarmersdelightcompat.integration;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Viewer-neutral Farmer harvest documentation.
 *
 * <p>The displayed outputs are examples. When {@code realLoot} is true, gameplay
 * remains authoritative and the real crop/block loot table decides the final
 * result, including enchantment-sensitive results such as Fortune/Silk Touch.</p>
 */
public record FarmerHarvestInfo(
        ResourceLocation id,
        Ingredient input,
        Ingredient tool,
        ToolUse toolUse,
        boolean toolDamaged,
        boolean realLoot,
        List<ItemStack> outputs,
        Component description
) {
    public FarmerHarvestInfo {
        input = input == null ? Ingredient.EMPTY : input;
        tool = tool == null ? Ingredient.EMPTY : tool;
        toolUse = toolUse == null ? ToolUse.NONE : toolUse;
        outputs = List.copyOf(outputs);
        description = description == null ? Component.empty() : description;
    }

    public boolean hasTool() {
        return toolUse != ToolUse.NONE && !tool.isEmpty();
    }
}
