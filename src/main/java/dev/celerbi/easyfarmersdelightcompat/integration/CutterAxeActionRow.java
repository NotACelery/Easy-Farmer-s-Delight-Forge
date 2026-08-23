package dev.celerbi.easyfarmersdelightcompat.integration;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.Ingredient;

/** One explanatory row inside the compact Cutter Axe Actions viewer page. */
public record CutterAxeActionRow(
        Ingredient input,
        Ingredient output,
        Component inputLabel
) {
}
