package dev.celerbi.easyfarmersdelightcompat.integration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

public record CutterAxeActionRow(
        Ingredient input,
        Ingredient output,
        Component inputLabel
) {
}
