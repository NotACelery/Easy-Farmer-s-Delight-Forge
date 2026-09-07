package dev.celerbi.easyfarmersdelightcompat.integration.stem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class StemCropDefinitions {
    private static final AtomicReference<Map<ResourceLocation, StemCropDefinition>> DEFINITIONS =
            new AtomicReference<>(Map.of());

    private StemCropDefinitions() {
    }

    public static void replace(Map<ResourceLocation, StemCropDefinition> definitions) {
        DEFINITIONS.set(Map.copyOf(definitions));
    }

    public static Optional<StemCropDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(id == null ? null : DEFINITIONS.get().get(id));
    }

    public static List<StemCropDefinition> all() {
        List<StemCropDefinition> definitions = new ArrayList<>(DEFINITIONS.get().values());
        definitions.sort(Comparator.comparing(definition -> definition.id().toString()));
        return List.copyOf(definitions);
    }

    public static Optional<StemCropDefinition> findPlanting(ItemStack stack) {
        return all().stream().filter(definition -> definition.matchesPlanting(stack)).findFirst();
    }

    public static Optional<StemCropDefinition> findStem(BlockState state) {
        return all().stream().filter(definition -> definition.matchesStem(state)).findFirst();
    }
}
