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

public final class ClientPreferences {
    private static final String FILE_NAME = "easyfarmersdelightcompat-client.properties";
    private static final String KEY_VILLAGERS_MUTED = "villagersMuted";
    private static final String KEY_IRON_FARM_SOUNDS_MUTED = "ironFarmSoundsMuted";
    private static final String KEY_EASY_MOB_FARM_SOUNDS_MUTED = "easyMobFarmSoundsMuted";
    private static boolean loaded;
    private static boolean villagersMuted;
    private static boolean ironFarmSoundsMuted;
    private static boolean easyMobFarmSoundsMuted;

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

    public static synchronized boolean ironFarmSoundsMuted() {
        ensureLoaded();
        return ironFarmSoundsMuted;
    }

    public static synchronized boolean toggleIronFarmSoundsMuted() {
        setIronFarmSoundsMuted(!ironFarmSoundsMuted());
        return ironFarmSoundsMuted;
    }

    public static synchronized void setIronFarmSoundsMuted(boolean muted) {
        ensureLoaded();
        ironFarmSoundsMuted = muted;
        save();
    }

    public static synchronized boolean easyMobFarmSoundsMuted() {
        ensureLoaded();
        return easyMobFarmSoundsMuted;
    }

    public static synchronized boolean toggleEasyMobFarmSoundsMuted() {
        setEasyMobFarmSoundsMuted(!easyMobFarmSoundsMuted());
        return easyMobFarmSoundsMuted;
    }

    public static synchronized void setEasyMobFarmSoundsMuted(boolean muted) {
        ensureLoaded();
        easyMobFarmSoundsMuted = muted;
        save();
    }

    private static void ensureLoaded() {
        if (loaded)
            return;
        loaded = true;
        villagersMuted = false;
        ironFarmSoundsMuted = false;
        easyMobFarmSoundsMuted = false;
        Path path = configPath();
        if (!Files.isRegularFile(path))
            return;
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
            villagersMuted = Boolean.parseBoolean(properties.getProperty(KEY_VILLAGERS_MUTED, "false"));
            ironFarmSoundsMuted = Boolean.parseBoolean(properties.getProperty(KEY_IRON_FARM_SOUNDS_MUTED, "false"));
            easyMobFarmSoundsMuted = Boolean.parseBoolean(
                    properties.getProperty(KEY_EASY_MOB_FARM_SOUNDS_MUTED, "false"));
        } catch (IOException e) {
            villagersMuted = false;
            ironFarmSoundsMuted = false;
            easyMobFarmSoundsMuted = false;
            System.err.println("[Easy Farmer's Delight Compat] Failed to load client preferences: " + e.getMessage());
        }
    }

    private static void save() {
        Path target = configPath();
        Path parent = target.getParent();
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        Properties properties = new Properties();
        properties.setProperty(KEY_VILLAGERS_MUTED, Boolean.toString(villagersMuted));
        properties.setProperty(KEY_IRON_FARM_SOUNDS_MUTED, Boolean.toString(ironFarmSoundsMuted));
        properties.setProperty(KEY_EASY_MOB_FARM_SOUNDS_MUTED, Boolean.toString(easyMobFarmSoundsMuted));
        try {
            if (parent != null)
                Files.createDirectories(parent);
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
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignoredAgain) {
            }
        }
    }

    private static Path configPath() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }
}
