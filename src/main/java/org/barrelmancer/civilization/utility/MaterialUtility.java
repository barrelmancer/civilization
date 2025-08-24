package org.barrelmancer.civilization.utility;

import org.barrelmancer.civilization.Civilization;
import org.barrelmancer.civilization.configuration.Configuration;
import org.barrelmancer.civilization.configuration.ServerConfiguration;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MaterialUtility {
    private static final Logger log = LoggerFactory.getLogger(MaterialUtility.class);

    private static final Map<String, Set<Material>> materialSets = new HashMap<>();
    private static final Map<String, Map<String, ItemStack>> itemCache = new HashMap<>();

    public static void loadMaterialSetFromKeys(String configName, String path, String setName) {
        Set<String> keys = ServerConfiguration.getKeys(configName, path);
        Set<Material> materials = new HashSet<>();

        for (String materialName : keys) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                materials.add(material);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid material in {}.{}: {}", configName, path, materialName);
            }
        }

        materialSets.put(setName, materials);
        log.info("Loaded {} materials for set '{}' from keys", materials.size(), setName);
    }

    public static ItemStack getItem(String configName, String path) {
        Map<String, ItemStack> configCache = itemCache.get(configName);
        if (configCache != null && configCache.containsKey(path)) {
            return configCache.get(path).clone();
        }

        FileConfiguration config = Configuration.getConfiguration(configName);

        ItemStack item = config.getItemStack(path);
        if (item != null) {
            cacheItem(configName, path, item);
            return item.clone();
        }

        String materialName = config.getString(path);
        if (materialName != null) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                item = new ItemStack(material);
                cacheItem(configName, path, item);
                return item.clone();
            } catch (IllegalArgumentException e) {
                log.warn("Invalid material at {}.{}: {}", configName, path, materialName);
            }
        }

        log.warn("Item not found at {}.{}", configName, path);
        return null;
    }

    /**
     * Get material type safely without throwing NPE
     */
    public static Material getMaterial(String configName, String path) {
        ItemStack item = getItem(configName, path);
        return item != null ? item.getType() : Material.AIR;
    }

    /**
     * Check if an item type matches a configured material
     */
    public static boolean isItemType(Material material, String configName, String path) {
        Material configMaterial = getMaterial(configName, path);
        return material == configMaterial;
    }

    private static void cacheItem(String configName, String path, ItemStack item) {
        itemCache.computeIfAbsent(configName, k -> new HashMap<>()).put(path, item.clone());
    }

    public static boolean isInSet(String setName, Material material) {
        Set<Material> set = materialSets.get(setName);
        return set != null && set.contains(material);
    }

    public static boolean isInSet(String setName, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        return isInSet(setName, item.getType());
    }

    public static boolean isInConfigKeys(String configName, String path, Material material) {
        Set<String> keys = ServerConfiguration.getKeys(configName, path);
        return keys.contains(material.name());
    }

    public static MaterialUtility getInstance() {
        return Civilization.getCivilization().getMaterialUtility();
    }
}