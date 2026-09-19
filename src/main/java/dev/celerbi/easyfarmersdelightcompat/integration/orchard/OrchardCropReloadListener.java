package dev.celerbi.easyfarmersdelightcompat.integration.orchard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.celerbi.easyfarmersdelightcompat.EasyFarmersDelightCompat;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public final class OrchardCropReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final String DIRECTORY = "efdc_orchard_crops";
    public static final OrchardCropReloadListener INSTANCE = new OrchardCropReloadListener();

    private OrchardCropReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager manager,
            ProfilerFiller profiler
    ) {
        Map<ResourceLocation, OrchardCropDefinition> loaded = new LinkedHashMap<>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            ResourceLocation id = entry.getKey();
            try {
                if (!entry.getValue().isJsonObject()) {
                    throw new IllegalArgumentException("root must be a JSON object");
                }
                JsonObject json = entry.getValue().getAsJsonObject();
                OrchardCropDefinition definition = OrchardCropDefinition.parse(id, json);
                if (definition != null && OrchardCropDefinitions.isExplicitlyExcluded(definition)) {
                    EasyFarmersDelightCompat.LOGGER.debug(
                            "Ignoring intentionally excluded orchard definition {}.", id);
                    return;
                }
                if (definition != null && OrchardCropDefinitions.isRuntimeSuppressedDefinition(id)) {
                    EasyFarmersDelightCompat.LOGGER.debug(
                            "Using dedicated apple-tree compatibility; ignoring vanilla orchard fallback {}.", id);
                    return;
                }
                if (definition != null) loaded.put(id, definition);
            } catch (RuntimeException exception) {
                EasyFarmersDelightCompat.LOGGER.warn(
                                "Skipping invalid orchard definition {}: {}", id, exception.getMessage());
            }
        });
        OrchardCropDefinitions.replace(loaded);
        EasyFarmersDelightCompat.LOGGER.info("Loaded {} orchard definition(s).", loaded.size());
    }
}
