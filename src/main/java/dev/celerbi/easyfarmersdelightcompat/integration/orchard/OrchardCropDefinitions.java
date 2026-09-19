package dev.celerbi.easyfarmersdelightcompat.integration.orchard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public final class OrchardCropDefinitions {
    // Durian is intentionally excluded from productive orchards: Fruits Delight grows it as a
    // separate hanging block which later falls, so EFD cannot represent it faithfully.
    private static final Set<ResourceLocation> EXCLUDED_DEFINITION_IDS = Set.of(
            new ResourceLocation("easyfarmersdelightcompat", "fruits_delight_durian")
    );
    private static final Set<ResourceLocation> EXCLUDED_PLANTING_IDS = Set.of(
            new ResourceLocation("fruitsdelight", "durian_leaves")
    );

    private static final Set<ResourceLocation> VANILLA_APPLE_DEFINITION_IDS = Set.of(
            new ResourceLocation("easyfarmersdelightcompat", "apple_oak"),
            new ResourceLocation("easyfarmersdelightcompat", "apple_dark_oak")
    );
    private static final Set<ResourceLocation> VANILLA_APPLE_LEAF_IDS = Set.of(
            new ResourceLocation("minecraft", "oak_leaves"),
            new ResourceLocation("minecraft", "dark_oak_leaves")
    );

    private static final AtomicReference<Map<ResourceLocation, OrchardCropDefinition>> DEFINITIONS =
            new AtomicReference<>(Map.of());

    private OrchardCropDefinitions() {
    }

    public static void replace(Map<ResourceLocation, OrchardCropDefinition> definitions) {
        DEFINITIONS.set(Map.copyOf(definitions));
    }

    public static Optional<OrchardCropDefinition> get(ResourceLocation id) {
        if (isRuntimeSuppressedDefinition(id)) {
            return Optional.empty();
        }
        return Optional.ofNullable(id == null ? null : DEFINITIONS.get().get(id));
    }

    public static List<OrchardCropDefinition> all() {
        List<OrchardCropDefinition> result = new ArrayList<>(DEFINITIONS.get().values());
        result.sort(Comparator.comparing(definition -> definition.id().toString()));
        return List.copyOf(result);
    }

    public static Optional<OrchardCropDefinition> findPlanting(ItemStack stack) {
        if (isExplicitlyExcludedPlanting(stack) || isRuntimeSuppressedPlanting(stack)) {
            return Optional.empty();
        }
        return all().stream()
                .filter(definition -> !isRuntimeSuppressedDefinition(definition.id()))
                .filter(definition -> definition.matchesPlanting(stack))
                .findFirst();
    }

    /**
     * Vanilla Oak/Dark Oak apples are a fallback only. Dedicated apple-tree mods should
     * own apple production when present, avoiding duplicate productive canopies.
     */
    public static boolean hasDedicatedAppleProvider() {
        return ModList.get().isLoaded("regions_unexplored")
                || ModList.get().isLoaded("croptopia")
                || ModList.get().isLoaded("fruitsdelight");
    }

    public static boolean isRuntimeSuppressedDefinition(ResourceLocation id) {
        return hasDedicatedAppleProvider() && id != null && VANILLA_APPLE_DEFINITION_IDS.contains(id);
    }

    public static boolean isRuntimeSuppressedPlanting(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return isRuntimeSuppressedResource(itemId);
    }

    public static boolean isRuntimeSuppressedResource(ResourceLocation id) {
        return hasDedicatedAppleProvider() && id != null && VANILLA_APPLE_LEAF_IDS.contains(id);
    }

    public static boolean isExplicitlyExcluded(OrchardCropDefinition definition) {
        return definition != null
                && (isExplicitlyExcludedDefinition(definition.id())
                || isExplicitlyExcludedResource(definition.plantingItemId())
                || isExplicitlyExcludedResource(definition.renderBlockId()));
    }

    public static boolean isExplicitlyExcludedDefinition(ResourceLocation id) {
        return id != null && EXCLUDED_DEFINITION_IDS.contains(id);
    }

    public static boolean isExplicitlyExcludedResource(ResourceLocation id) {
        return id != null && EXCLUDED_PLANTING_IDS.contains(id);
    }

    public static boolean isExplicitlyExcludedPlanting(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return isExplicitlyExcludedResource(
                net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
