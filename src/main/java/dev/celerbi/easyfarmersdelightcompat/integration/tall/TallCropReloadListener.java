package dev.celerbi.easyfarmersdelightcompat.integration.tall;

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

public final class TallCropReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final String DIRECTORY = "efdc_tall_crops";
    public static final TallCropReloadListener INSTANCE = new TallCropReloadListener();

    private TallCropReloadListener() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(
            Map<ResourceLocation, JsonElement> resources,
            ResourceManager resourceManager,
            ProfilerFiller profiler
    ) {
        Map<ResourceLocation, TallCropDefinition> loaded = new LinkedHashMap<>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            ResourceLocation id = entry.getKey();
            try {
                if (!entry.getValue().isJsonObject()) {
                    throw new IllegalArgumentException("root must be a JSON object");
                }
                JsonObject json = entry.getValue().getAsJsonObject();
                TallCropDefinition definition = TallCropDefinition.parse(id, json);
                if (definition != null) loaded.put(id, definition);
            } catch (RuntimeException exception) {
                EasyFarmersDelightCompat.LOGGER.warn(
                                "Skipping invalid tall-crop definition {}: {}", id, exception.getMessage());
            }
        });
        TallCropDefinitions.replace(loaded);
        EasyFarmersDelightCompat.LOGGER.info("Loaded {} tall-crop definition(s).", loaded.size());
    }
}
