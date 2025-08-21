package org.barrelmancer.civilization.cooking;

import org.barrelmancer.civilization.cooking.config.CookingConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class BaseCookingManager<T extends CookingMemory> implements CookingManager<T> {
    private static final Logger log = LoggerFactory.getLogger(BaseCookingManager.class);

    protected final Map<String, T> cookingMemory = new HashMap<>();
    protected final CookingConfig config;
    protected JavaPlugin plugin;
    protected File memoryFile;
    protected FileConfiguration memoryConfig;

    protected BaseCookingManager(CookingConfig config) {
        this.config = config;
    }

    public void initialize(JavaPlugin plugin) {
        this.plugin = plugin;
        setupMemoryFile();
        loadCookingMemory();
    }

    private void setupMemoryFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        memoryFile = new File(plugin.getDataFolder(), config.getMemoryFileName());
        if (!memoryFile.exists()) {
            try {
                memoryFile.createNewFile();
            } catch (IOException e) {
                log.info("Failed to create {} data file: {}", config.getCookingType(), e.getMessage());
            }
        }
        memoryConfig = YamlConfiguration.loadConfiguration(memoryFile);
    }

    protected String getLocationKey(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            throw new IllegalArgumentException("Location or world cannot be null");
        }
        return loc.getWorld().getName() + ":" + loc.getBlockX() + ":" +
                loc.getBlockY() + ":" + loc.getBlockZ();
    }

    protected Location parseLocationKey(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length != 4) return null;
            return new Location(Bukkit.getWorld(parts[0]),
                    Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (Exception e) {
            log.info("Failed to parse location key {}: {}", key, e.getMessage());
            return null;
        }
    }

    @Override
    public T getCookingMemory(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        String key = getLocationKey(location);
        T memory = cookingMemory.get(key);

        if (memory == null) {
            memory = (T) CookingMemoryFactory.createMemory(getSystemType(), location);
            cookingMemory.put(key, memory);
        }

        return memory;
    }

    protected abstract CookingSystemType getSystemType();

    @Override
    public void removeCookingData(Location location) {
        if (location == null) return;

        String key = getLocationKey(location);
        T removed = cookingMemory.remove(key);

        if (removed != null) {
            memoryConfig.set(key, null);
            try {
                memoryConfig.save(memoryFile);
            } catch (IOException e) {
                log.info("Failed to save after removing {} data: {}", config.getCookingType(), e.getMessage());
            }
        }
    }

    @Override
    public void startCooking(T memory, Location location, UUID playerUUID) {
        if (memory == null || !memory.canStartCooking()) return;

        memory.setCooking(true);
        memory.setCookStartTime(System.currentTimeMillis());
        memory.setCookDuration(config.getCookTicks());
        if (memory.getCookingPlayerUUID() == null) {
            memory.setCookingPlayerUUID(playerUUID);
        }

        saveCookingMemory(memory, location);
    }

    @Override
    public void updateCooking() {
        for (Map.Entry<String, T> entry : cookingMemory.entrySet()) {
            T memory = entry.getValue();
            if (memory.isCooking() && memory.isCookingComplete()) {
                Location location = parseLocationKey(entry.getKey());
                if (location != null) {
                    handleCookingComplete(memory, location);
                }
            }
        }
    }

    protected void handleCookingComplete(T memory, Location location) {
    }

    protected abstract T createMemory(Location location);

    protected abstract void sendCompletionMessage(Player player);

    protected abstract void playCompletionSound(Player player, Location location);

    protected abstract void loadCookingMemory();
}