package dev.celerbi.easyfarmersdelightcompat.integration;

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;

public final class CuttingRecipeResolver {
    private static final ResourceLocation CUTTING_TYPE = new ResourceLocation(
            "farmersdelight",
            "cutting");
    private static final String WRAPPER_CLASS = "net.minecraftforge.items.wrapper.RecipeWrapper";

    public record Result(
            ResourceLocation recipeId,
            List<ItemStack> outputs,
            Optional<SoundEvent> sound) {
        public Result {
            outputs = List.copyOf(outputs);
            sound = sound == null ? Optional.empty() : sound;
        }
    }

    private CuttingRecipeResolver() {
    }

    public static Optional<Result> resolve(
            Level level,
            ItemStack input,
            ItemStack tool,
            int fortuneLevel) {
        if (level == null
                || input == null
                || input.isEmpty()
                || tool == null
                || tool.isEmpty())
            return Optional.empty();

        try {
            Object ri = createInput(input);
            for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
                if (!isCuttingRecipe(recipe)
                        || !toolMatches(recipe, tool)
                        || !matches(recipe, ri, level))
                    continue;

                Method roll = findMethod(recipe.getClass(), "rollResults", 3);
                if (roll == null)
                    return Optional.empty();

                Object raw = roll.invoke(
                        recipe,
                        level.random,
                        Math.max(0, fortuneLevel),
                        ri);
                if (!(raw instanceof List<?> list))
                    return Optional.empty();

                List<ItemStack> out = new ArrayList<>();
                for (Object v : list)
                    if (v instanceof ItemStack st && !st.isEmpty())
                        out.add(st.copy());

                return Optional.of(new Result(
                        recipe.getId(),
                        out,
                        resolveSound(recipe)));
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
        }

        return Optional.empty();
    }

    public static boolean hasMatchingRecipe(
            Level level,
            ItemStack input,
            Iterable<ItemStack> tools) {
        if (level == null || input == null || input.isEmpty() || tools == null)
            return false;

        try {
            Object ri = createInput(input);
            for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
                if (!isCuttingRecipe(recipe) || !matches(recipe, ri, level))
                    continue;

                for (ItemStack tool : tools)
                    if (tool != null && !tool.isEmpty() && toolMatches(recipe, tool))
                        return true;
            }
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
        }

        return false;
    }

    private static Object createInput(ItemStack input) throws ReflectiveOperationException {
        ItemStackHandler h = new ItemStackHandler(1);
        h.setStackInSlot(0, input.copyWithCount(1));

        Class<?> c = ReflectionCache.type(WRAPPER_CLASS);
        Constructor<?> ctor = ReflectionCache.constructor(c, net.minecraftforge.items.IItemHandlerModifiable.class);
        return ctor.newInstance(h);
    }

    private static boolean toolMatches(Recipe<?> r, ItemStack tool) throws ReflectiveOperationException {
        Method m = findMethod(r.getClass(), "getTool", 0);
        return m != null
                && m.invoke(r) instanceof Ingredient i
                && i.test(tool);
    }

    private static Optional<SoundEvent> resolveSound(Recipe<?> r) throws ReflectiveOperationException {
        Method m = findMethod(r.getClass(), "getSoundEventID", 0);
        if (m == null)
            return Optional.empty();

        Object raw = m.invoke(r);
        if (!(raw instanceof String id) || id.isBlank())
            return Optional.empty();

        ResourceLocation loc = ResourceLocation.tryParse(id);
        return loc != null && BuiltInRegistries.SOUND_EVENT.containsKey(loc)
                ? Optional.ofNullable(BuiltInRegistries.SOUND_EVENT.get(loc))
                : Optional.empty();
    }

    private static boolean isCuttingRecipe(Recipe<?> r) {
        return r != null
                && CUTTING_TYPE.equals(BuiltInRegistries.RECIPE_TYPE.getKey(r.getType()));
    }

    private static boolean matches(Recipe<?> r, Object input, Level level)
            throws ReflectiveOperationException {
        Method m = findMethod(r.getClass(), "matches", 2);
        return m != null && Boolean.TRUE.equals(m.invoke(r, input, level));
    }

    private static Method findMethod(Class<?> type, String name, int count) {
        try {
            return ReflectionCache.publicMethodByArity(type, name, count);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
