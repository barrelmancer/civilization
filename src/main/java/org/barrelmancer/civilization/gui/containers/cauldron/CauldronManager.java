package org.barrelmancer.civilization.gui.containers.cauldron;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.barrelmancer.civilization.configuration.ServerConfiguration;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.memory.CauldronMemory;
import org.barrelmancer.civilization.utility.UIUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CauldronManager {
    private static final Logger log = LoggerFactory.getLogger(CauldronManager.class);
    private static CauldronManager instance;
    private static JavaPlugin plugin;
    private final Map<String, CauldronMemory> cauldrones = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    private CauldronManager() {
    }

    public static void initialize(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        if (instance == null) {
            instance = new CauldronManager();
            instance.setupDataFile();
            instance.loadCauldronData();
        }
    }

    public static CauldronManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CauldronManager not initialized! Call initialize() first.");
        }
        return instance;
    }

    private void setupDataFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "cauldron-data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                log.info("Failed to create cauldron data file: {}", e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public CauldronMemory getCauldronData(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        String key = UIUtility.locationToString(location);
        CauldronMemory memory = cauldrones.get(key);

        if (memory == null) {
            memory = new CauldronMemory(location);
            cauldrones.put(key, memory);
        }

        return memory;
    }

    public void removeCauldronData(Location location) {
        if (location == null) {
            return;
        }

        String key = UIUtility.locationToString(location);
        CauldronMemory removed = cauldrones.remove(key);

        if (removed != null) {
            dataConfig.set(key, null);
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                log.info("Failed to save after removing cauldron data: {}", e.getMessage());
            }
        }
    }

    public void startCooking(CauldronMemory data, Location location, UUID playerUUID) {
        if (data == null) {
            return;
        }

        if (!data.canStartCooking()) {
            return;
        }

        data.setCooking(true);
        data.setCookStartTime(System.currentTimeMillis());
        data.setCookDuration(ServerConfiguration.getInt("cauldron", "cauldron.cook-ticks"));
        if (data.getCookingPlayerUUID() == null) {
            data.setCookingPlayerUUID(playerUUID);
        }

        saveCauldronData(data, location);
    }

    public void saveCauldronData(CauldronMemory memory, Location location) {
        String locationKey = UIUtility.locationToString(location);

        for (int i = 0; i < memory.getAllSlotItems().size(); i++) {
            ItemStack slotItem = memory.getAllSlotItems().get(i);
            if (slotItem != null && slotItem.getType() != Material.AIR) {
                dataConfig.set(locationKey + ".all-slots." + i, slotItem);
            } else {
                dataConfig.set(locationKey + ".all-slots." + i, null);
            }
        }

        if (memory.getOutput() != null && memory.getOutput().getType() != Material.AIR) {
            dataConfig.set(locationKey + ".output", memory.getOutput());
        } else {
            dataConfig.set(locationKey + ".output", null);
        }

        dataConfig.set(locationKey + ".cooking", memory.isCooking());
        dataConfig.set(locationKey + ".cook-start-time", memory.getCookStartTime());
        dataConfig.set(locationKey + ".cook-duration", memory.getCookDuration());

        if (memory.getCookingPlayerUUID() != null) {
            dataConfig.set(locationKey + ".cooking-player-UUID", memory.getCookingPlayerUUID().toString());
        } else {
            dataConfig.set(locationKey + ".cooking-player-UUID", null);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            log.info("Failed to save cauldron data: {}", e.getMessage());
        }
    }

    private void loadCauldronData() {
        if (dataConfig == null) return;

        for (String locationKey : dataConfig.getKeys(false)) {
            try {
                Location location = UIUtility.parseLocationKey(locationKey);
                if (location == null) continue;

                CauldronMemory memory = new CauldronMemory(location);

                for (int i = 0; i < ServerConfiguration.getInt("cauldron", "cauldron.size"); i++) {
                    ItemStack slotItem = dataConfig.getItemStack(locationKey + ".all-slots." + i);
                    memory.setSlotItem(i, slotItem);
                }

                ItemStack output = dataConfig.getItemStack(locationKey + ".output");
                memory.setOutput(output);

                boolean isCooking = dataConfig.getBoolean(locationKey + ".cooking", false);
                long cookStartTime = dataConfig.getLong(locationKey + ".cook-start-time", 0);
                int cookDuration = dataConfig.getInt(locationKey + ".cook-duration", 0);

                String playerUUIDString = dataConfig.getString(locationKey + ".cooking-player-UUID");
                UUID cookingPlayerUUID = null;
                if (playerUUIDString != null && !playerUUIDString.isEmpty()) {
                    try {
                        cookingPlayerUUID = UUID.fromString(playerUUIDString);
                    } catch (IllegalArgumentException e) {
                        log.info("Invalid UUID format in save data: {}", playerUUIDString);
                    }
                }

                memory.setCooking(isCooking);
                memory.setCookStartTime(cookStartTime);
                memory.setCookDuration(cookDuration);
                memory.setCookingPlayerUUID(cookingPlayerUUID);

                cauldrones.put(locationKey, memory);
            } catch (Exception e) {
                log.info("Failed to load cauldron data for {}: {}", locationKey, e.getMessage());
            }
        }
    }

    public void updateCooking() {
        for (Map.Entry<String, CauldronMemory> entry : cauldrones.entrySet()) {
            CauldronMemory data = entry.getValue();
            String locationKey = entry.getKey();

            if (data.isCooking() && data.isCookingComplete()) {
                Location location = UIUtility.parseLocationKey(locationKey);
                if (location != null) {
                    UUID cookingPlayerUUID = data.getCookingPlayerUUID();

                    createOutput(data, location);
                    if (cookingPlayerUUID != null) {
                        Player cookingPlayer = Bukkit.getPlayer(cookingPlayerUUID);
                        if (cookingPlayer != null && cookingPlayer.isOnline()) {
                            cookingPlayer.sendMessage(
                                    Component.text(ServerConfiguration.getString("cauldron", "messages.complete"))
                                            .color(TextColor.color(UIConstants.DONE_COLOR)));
                            cookingPlayer.playSound(cookingPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        }
                    }
                    if (location.getWorld() != null) {
                        location.getWorld().playSound(location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 0.5f, 1.5f);
                    }
                }
            }
        }
    }

    public ItemStack createOutput(CauldronMemory data, Location location) {
        if (data == null) {
            return null;
        }

        if (!data.isCookingComplete()) {
            return null;
        }

        ItemStack output = UIUtility.createCleanWaterPotion();
        UUID cookingPlayerUUID = data.getCookingPlayerUUID();

        List<Integer> ingredientSlots = ServerConfiguration.getIntList("cauldron", "cauldron.ingredient-slots");
        for (int slot : ingredientSlots) {
            data.setSlotItem(slot, null);
        }

        data.setWaterPotion(null);

        data.setOutput(output);
        data.setCooking(false);
        data.setCookingPlayerUUID(cookingPlayerUUID);

        saveCauldronData(data, location);
        return output;
    }


    public void cleanup() {
        for (Map.Entry<String, CauldronMemory> entry : cauldrones.entrySet()) {
            Location location = UIUtility.parseLocationKey(entry.getKey());
            if (location != null) {
                saveCauldronData(entry.getValue(), location);
            }
        }
        cauldrones.clear();
    }
}
