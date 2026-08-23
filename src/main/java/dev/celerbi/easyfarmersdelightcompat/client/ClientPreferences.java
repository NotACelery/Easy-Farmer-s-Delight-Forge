package dev.celerbi.easyfarmersdelightcompat.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import net.minecraftforge.fml.loading.FMLPaths;

/**
 * Global client-only preferences for the current Minecraft installation.
 *
 * The Noise Switch intentionally does not use world/player NBT: one local value must
 * survive dimensions, servers, world unloads and restarts. Changes are flushed to
 * disk immediately with a temporary-file replace so a later crash cannot roll back
 * an already completed click.
 */
public final class ClientPreferences {
    private static final String FILE_NAME = "easyfarmersdelightcompat-client.properties";
    private static final String KEY_VILLAGERS_MUTED = "villagersMuted";
    private static boolean loaded;
    private static boolean villagersMuted;

    private ClientPreferences() {
    }

    public static synchronized boolean villagersMuted() {
        ensureLoaded();
        return villagersMuted;
    }

    public static synchronized boolean toggleVillagersMuted() {
        setVillagersMuted(!villagersMuted());
        return villagersMuted;
    }

    public static synchronized void setVillagersMuted(boolean muted) {
        ensureLoaded();
        villagersMuted = muted;
        save();
    }

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        villagersMuted = false;
        Path path = configPath();
        if (!Files.isRegularFile(path)) return;
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
            villagersMuted = Boolean.parseBoolean(properties.getProperty(KEY_VILLAGERS_MUTED, "false"));
        } catch (IOException e) {
            villagersMuted = false;
            System.err.println("[Easy Farmer's Delight Compat] Failed to load client preferences: " + e.getMessage());
        }
    }

    private static void save() {
        Path target = configPath();
        Path parent = target.getParent();
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Properties properties = new Properties();
        properties.setProperty(KEY_VILLAGERS_MUTED, Boolean.toString(villagersMuted));
        try {
            if (parent != null) Files.createDirectories(parent);
            try (OutputStream out = Files.newOutputStream(temp)) {
                properties.store(out, "Easy Farmer's Delight Compat client preferences");
            }
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("[Easy Farmer's Delight Compat] Failed to save client preferences: " + e.getMessage());
            try { Files.deleteIfExists(temp); } catch (IOException ignoredAgain) { }
        }
    }

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }
}
