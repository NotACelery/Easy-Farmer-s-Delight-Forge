package dev.celerbi.easyfarmersdelightcompat.integration.orchard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class OrchardCropDefinitions {
    private static final AtomicReference<Map<ResourceLocation, OrchardCropDefinition>> DEFINITIONS =
            new AtomicReference<>(Map.of());

    private OrchardCropDefinitions() {
    }

    public static void replace(Map<ResourceLocation, OrchardCropDefinition> definitions) {
        DEFINITIONS.set(Map.copyOf(definitions));
    }

    public static Optional<OrchardCropDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(id == null ? null : DEFINITIONS.get().get(id));
    }

    public static List<OrchardCropDefinition> all() {
        List<OrchardCropDefinition> result = new ArrayList<>(DEFINITIONS.get().values());
        result.sort(Comparator.comparing(definition -> definition.id().toString()));
        return List.copyOf(result);
    }

    public static Optional<OrchardCropDefinition> findPlanting(ItemStack stack) {
        return all().stream().filter(definition -> definition.matchesPlanting(stack)).findFirst();
    }
}
