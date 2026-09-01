package dev.celerbi.easyfarmersdelightcompat.integration.attached;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class AttachedCropDefinitions {
    private static final AtomicReference<Map<ResourceLocation, AttachedCropDefinition>> DEFINITIONS =
            new AtomicReference<>(Map.of());

    private AttachedCropDefinitions() {
    }

    public static void replace(Map<ResourceLocation, AttachedCropDefinition> definitions) {
        DEFINITIONS.set(Map.copyOf(definitions));
    }

    public static Optional<AttachedCropDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(id == null ? null : DEFINITIONS.get().get(id));
    }

    public static List<AttachedCropDefinition> all() {
        List<AttachedCropDefinition> definitions = new ArrayList<>(DEFINITIONS.get().values());
        definitions.sort(Comparator.comparing(definition -> definition.id().toString()));
        return List.copyOf(definitions);
    }

    public static boolean acceptsHost(BlockState host) {
        return all().stream().anyMatch(definition -> definition.matchesHost(host));
    }

    public static boolean isPlantingItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return all().stream().anyMatch(definition -> definition.matchesPlanting(stack));
    }

    public static Optional<AttachedCropDefinition> findPlanting(ItemStack stack, BlockState host) {
        return all().stream()
                .filter(definition -> definition.matchesPlanting(stack) && definition.matchesHost(host))
                .findFirst();
    }
}
