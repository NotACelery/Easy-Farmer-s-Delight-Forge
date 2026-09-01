package dev.celerbi.easyfarmersdelightcompat.integration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public final class CuttingRecipeResolver {
    private static final ResourceLocation CUTTING_TYPE = new ResourceLocation("farmersdelight", "cutting");
    private static final String WRAPPER_CLASS = "net.minecraftforge.items.wrapper.RecipeWrapper";

    public record Result(ResourceLocation recipeId, List<ItemStack> outputs, Optional<SoundEvent> sound) {
        public Result {
            outputs = List.copyOf(outputs);
            sound = sound == null ? Optional.empty() : sound;
        }
    }

    private CuttingRecipeResolver() {
    }

    public static Optional<Result> resolve(Level level, ItemStack input, ItemStack tool, int fortuneLevel) {
        if (level == null || input == null || input.isEmpty() || tool == null || tool.isEmpty()) {
            return Optional.empty();
        }

        try {
            Object recipeInput = createInput(input);
            for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
                if (!isCuttingRecipe(recipe)
                        || !toolMatches(recipe, tool)
                        || !matches(recipe, recipeInput, level)) {
                    continue;
                }

                Method rollResults = findMethod(recipe.getClass(), "rollResults", 3);
                if (rollResults == null) {
                    return Optional.empty();
                }

                Object raw = rollResults.invoke(recipe, level.random, Math.max(0, fortuneLevel), recipeInput);
                if (!(raw instanceof List<?> rawList)) {
                    return Optional.empty();
                }

                List<ItemStack> outputs = new ArrayList<>();
                for (Object value : rawList) {
                    if (value instanceof ItemStack stack && !stack.isEmpty()) {
                        outputs.add(stack.copy());
                    }
                }

                return Optional.of(new Result(recipe.getId(), outputs, resolveSound(recipe)));
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
        }

        return Optional.empty();
    }

    public static boolean hasMatchingRecipe(Level level, ItemStack input, Iterable<ItemStack> tools) {
        if (level == null || input == null || input.isEmpty() || tools == null) {
            return false;
        }

        try {
            Object recipeInput = createInput(input);
            for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
                if (!isCuttingRecipe(recipe) || !matches(recipe, recipeInput, level)) {
                    continue;
                }

                for (ItemStack tool : tools) {
                    if (tool != null && !tool.isEmpty() && toolMatches(recipe, tool)) {
                        return true;
                    }
                }
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
        }

        return false;
    }

    private static Object createInput(ItemStack input) throws ReflectiveOperationException {
        ItemStackHandler handler = new ItemStackHandler(1);
        handler.setStackInSlot(0, input.copyWithCount(1));

        Class<?> wrapperClass = ReflectionCache.type(WRAPPER_CLASS);
        Constructor<?> constructor = ReflectionCache.constructor(wrapperClass, IItemHandlerModifiable.class);
        return constructor.newInstance(handler);
    }

    private static boolean toolMatches(Recipe<?> recipe, ItemStack tool) throws ReflectiveOperationException {
        Method getTool = findMethod(recipe.getClass(), "getTool", 0);
        return getTool != null
                && getTool.invoke(recipe) instanceof Ingredient ingredient
                && ingredient.test(tool);
    }

    private static Optional<SoundEvent> resolveSound(Recipe<?> recipe) throws ReflectiveOperationException {
        Method getSound = findMethod(recipe.getClass(), "getSoundEventID", 0);
        if (getSound == null) {
            return Optional.empty();
        }

        Object raw = getSound.invoke(recipe);
        if (!(raw instanceof String id) || id.isBlank()) {
            return Optional.empty();
        }

        ResourceLocation location = ResourceLocation.tryParse(id);
        return location != null && BuiltInRegistries.SOUND_EVENT.containsKey(location)
                ? Optional.ofNullable(BuiltInRegistries.SOUND_EVENT.get(location))
                : Optional.empty();
    }

    private static boolean isCuttingRecipe(Recipe<?> recipe) {
        return recipe != null && CUTTING_TYPE.equals(BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()));
    }

    private static boolean matches(Recipe<?> recipe, Object input, Level level) throws ReflectiveOperationException {
        Method matches = findMethod(recipe.getClass(), "matches", 2);
        return matches != null && Boolean.TRUE.equals(matches.invoke(recipe, input, level));
    }

    private static Method findMethod(Class<?> type, String name, int parameterCount) {
        try {
            return ReflectionCache.publicMethodByArity(type, name, parameterCount);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
