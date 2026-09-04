package dev.celerbi.easyfarmersdelightcompat.integration.orchard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, OrchardCropDefinition> loaded = new LinkedHashMap<>();
        resources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            ResourceLocation id = entry.getKey();
            try {
                if (!entry.getValue().isJsonObject()) {
                    throw new IllegalArgumentException("root must be a JSON object");
                }
                JsonObject json = entry.getValue().getAsJsonObject();
                OrchardCropDefinition definition = OrchardCropDefinition.parse(id, json);
                if (definition != null) loaded.put(id, definition);
            } catch (RuntimeException exception) {
                System.err.println("[Easy Farmer's Delight] Skipping invalid orchard definition "
                        + id + ": " + exception.getMessage());
            }
        });
        OrchardCropDefinitions.replace(loaded);
        System.out.println("[Easy Farmer's Delight] Loaded " + loaded.size() + " orchard definition(s).");
    }
}
