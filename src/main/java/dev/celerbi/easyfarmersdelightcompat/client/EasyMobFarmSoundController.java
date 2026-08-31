package dev.celerbi.easyfarmersdelightcompat.client;

import dev.celerbi.easyfarmersdelightcompat.compat.easymobfarm.EasyMobFarmCompat;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.entity.Entity;

public final class EasyMobFarmSoundController {
    private static final String RENDERER_MANAGER =
            "de.markusbordihn.easymobfarm.client.renderer.manager.RendererManager";
    private static final String ENTITY_MAP_FIELD = "entityMap";

    private static final Map<Entity, Boolean> ORIGINAL_SILENT_STATE = new IdentityHashMap<>();
    private static Field entityMapField;
    private static boolean reflectionResolved;

    private EasyMobFarmSoundController() {
    }

    public static void tick() {
        if (!EasyMobFarmCompat.isLoaded()) {
            return;
        }
        applyPreference(ClientPreferences.easyMobFarmSoundsMuted());
    }

    public static void applyPreferenceNow() {
        if (!EasyMobFarmCompat.isLoaded()) {
            return;
        }
        applyPreference(ClientPreferences.easyMobFarmSoundsMuted());
    }

    private static void applyPreference(boolean muted) {
        if (!muted) {
            restoreAll();
            return;
        }

        Map<?, ?> displayEntities = displayEntityMap();
        if (displayEntities == null) {
            return;
        }

        ORIGINAL_SILENT_STATE.entrySet().removeIf(entry -> {
            if (displayEntities.containsValue(entry.getKey())) {
                return false;
            }
            entry.getKey().setSilent(entry.getValue());
            return true;
        });
        for (Object value : displayEntities.values()) {
            if (!(value instanceof Entity entity)) {
                continue;
            }
            ORIGINAL_SILENT_STATE.putIfAbsent(entity, entity.isSilent());
            if (!entity.isSilent()) {
                entity.setSilent(true);
            }
        }
    }

    private static void restoreAll() {
        for (Map.Entry<Entity, Boolean> entry : ORIGINAL_SILENT_STATE.entrySet()) {
            entry.getKey().setSilent(entry.getValue());
        }
        ORIGINAL_SILENT_STATE.clear();
    }

    private static Map<?, ?> displayEntityMap() {
        Field field = resolveEntityMapField();
        if (field == null) {
            return null;
        }
        try {
            Object value = field.get(null);
            return value instanceof Map<?, ?> map ? map : null;
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    private static Field resolveEntityMapField() {
        if (reflectionResolved) {
            return entityMapField;
        }
        reflectionResolved = true;
        try {
            Class<?> rendererManager = Class.forName(RENDERER_MANAGER, false,
                    EasyMobFarmSoundController.class.getClassLoader());
            Field field = rendererManager.getDeclaredField(ENTITY_MAP_FIELD);
            field.setAccessible(true);
            entityMapField = field;
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            entityMapField = null;
        }
        return entityMapField;
    }
}
