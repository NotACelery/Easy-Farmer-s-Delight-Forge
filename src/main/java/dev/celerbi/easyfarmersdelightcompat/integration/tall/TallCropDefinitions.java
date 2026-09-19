package dev.celerbi.easyfarmersdelightcompat.integration.tall;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class TallCropDefinitions {
    private static final AtomicReference<Map<ResourceLocation, TallCropDefinition>> DEFINITIONS =
            new AtomicReference<>(Map.of());

    private TallCropDefinitions() {
    }

    public static void replace(Map<ResourceLocation, TallCropDefinition> definitions) {
        DEFINITIONS.set(Map.copyOf(definitions));
    }

    public static Optional<TallCropDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(id == null ? null : DEFINITIONS.get().get(id));
    }

    public static List<TallCropDefinition> all() {
        List<TallCropDefinition> definitions = new ArrayList<>(DEFINITIONS.get().values());
        definitions.sort(Comparator.comparing(definition -> definition.id().toString()));
        return List.copyOf(definitions);
    }

    public static Optional<TallCropDefinition> findPlanting(ItemStack stack) {
        return all().stream().filter(definition -> definition.matchesPlanting(stack)).findFirst();
    }

    public static Optional<TallCropDefinition> findCrop(BlockState state) {
        return all().stream().filter(definition -> definition.matchesCrop(state)).findFirst();
    }
}
