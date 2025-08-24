package org.barrelmancer.civilization.configuration;

import org.barrelmancer.civilization.Civilization;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    private static final Map<String, File> files = new HashMap<>();
    private static final Map<String, FileConfiguration> configurations = new HashMap<>();

    public static void initialize(JavaPlugin plugin, String configName, String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);

        try {
            file.createNewFile();
        } catch (IOException e) {
            log.error("Failed to create config file {}: {}", fileName, e.getMessage());
        }

        files.put(configName, file);
        configurations.put(configName, YamlConfiguration.loadConfiguration(file));
        log.info("Initialized configuration: {} -> {}", configName, fileName);
    }

    public static void initializeAll(JavaPlugin plugin, Map<String, String> configs) {
        configs.forEach((configName, fileName) -> initialize(plugin, configName, fileName));
    }
    public static FileConfiguration getConfiguration(String configName) {
        FileConfiguration config = configurations.get(configName);
        if (config == null) {
            log.warn("Configuration '{}' not found", configName);
        }
        return config;
    }

    public static void saveConfiguration(String configName) {
        File file = files.get(configName);
        FileConfiguration config = configurations.get(configName);

        if (file == null || config == null) {
            log.warn("Cannot save configuration '{}' - not found", configName);
            return;
        }

        try {
            config.save(file);
        } catch (IOException e) {
            log.error("Failed to save configuration {}: {}", configName, e.getMessage());
        }
    }

    public static void saveAll() {
        configurations.keySet().forEach(Configuration::saveConfiguration);
    }

    public static void reloadConfiguration(String configName) {
        File file = files.get(configName);
        if (file == null) {
            log.warn("Cannot reload configuration '{}' - not found", configName);
            return;
        }

        configurations.put(configName, YamlConfiguration.loadConfiguration(file));
        log.info("Reloaded configuration: {}", configName);
    }

    public static void reloadAll() {
        configurations.keySet().forEach(Configuration::reloadConfiguration);
    }

    public static boolean hasConfiguration(String configName) {
        return configurations.containsKey(configName);
    }

    public static void removeConfiguration(String configName) {
        files.remove(configName);
        configurations.remove(configName);
        log.info("Removed configuration: {}", configName);
    }
}