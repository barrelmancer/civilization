package org.barrelmancer.civilization.campfire;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.constants.CampfireConstants;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.memory.CampfireMemory;
import org.barrelmancer.civilization.util.UIUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CampfireManager {
    private static final Logger log = LoggerFactory.getLogger(CampfireManager.class);
    private static CampfireManager instance;
    private final Map<String, CampfireMemory> campfires = new HashMap<>();
    private static JavaPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    private CampfireManager() {
    }

    public static void initialize(JavaPlugin pluginInstance) {
        plugin = pluginInstance;
        if (instance == null) {
            instance = new CampfireManager();
            instance.setupDataFile();
            instance.loadCampfireData();
        }
    }

    public static CampfireManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CampfireManager not initialized! Call initialize() first.");
        }
        return instance;
    }

    private void setupDataFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        dataFile = new File(plugin.getDataFolder(), "campfire-data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                log.info("Failed to create campfire data file: {}", e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    private String getLocationKey(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            throw new IllegalArgumentException("Location or world cannot be null");
        }
        return loc.getWorld().getName() + ":" +
                loc.getBlockX() + ":" +
                loc.getBlockY() + ":" +
                loc.getBlockZ();
    }

    private Location parseLocationKey(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length != 4) return null;

            return new Location(
                    Bukkit.getWorld(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            );
        } catch (Exception e) {
            log.info("Failed to parse location key {}: {}", key, e.getMessage());
            return null;
        }
    }

    public CampfireMemory getCampfireData(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        String key = getLocationKey(location);
        CampfireMemory memory = campfires.get(key);

        if (memory == null) {
            memory = new CampfireMemory(location);
            campfires.put(key, memory);
        }

        return memory;
    }

    public void removeCampfireData(Location location) {
        if (location == null) {
            return;
        }

        String key = getLocationKey(location);
        CampfireMemory removed = campfires.remove(key);

        if (removed != null) {
            dataConfig.set(key, null);
            try {
                dataConfig.save(dataFile);
            } catch (IOException e) {
                log.info("Failed to save after removing campfire data: {}", e.getMessage());
            }
        }
    }

    public void startCooking(CampfireMemory data, Location location, UUID playerUUID) {
        if (data == null) {
            return;
        }

        if (!data.canStartCooking()) {
            return;
        }

        data.setCooking(true);
        data.setCookStartTime(System.currentTimeMillis());
        data.setCookDuration(CampfireConstants.COOK_TICKS);
        if (data.getCookingPlayerUUID() == null) {
            data.setCookingPlayerUUID(playerUUID);
        }

        saveCampfireData(data, location);
    }

    public void saveCampfireData(CampfireMemory memory, Location location) {
        String locationKey = getLocationKey(location);

        for (int i = 0; i < memory.getAllSlotItems().size(); i++) {
            ItemStack slotItem = memory.getAllSlotItems().get(i);
            if (slotItem != null && slotItem.getType() != Material.AIR) {
                dataConfig.set(locationKey + ".allSlots." + i, slotItem);
            } else {
                dataConfig.set(locationKey + ".allSlots." + i, null);
            }
        }

        if (memory.getBowl() != null && memory.getBowl().getType() != Material.AIR) {
            dataConfig.set(locationKey + ".bowl", memory.getBowl());
        } else {
            dataConfig.set(locationKey + ".bowl", null);
        }

        if (memory.getOutput() != null && memory.getOutput().getType() != Material.AIR) {
            dataConfig.set(locationKey + ".output", memory.getOutput());
        } else {
            dataConfig.set(locationKey + ".output", null);
        }

        dataConfig.set(locationKey + ".cooking", memory.isCooking());
        dataConfig.set(locationKey + ".cookStartTime", memory.getCookStartTime());
        dataConfig.set(locationKey + ".cookDuration", memory.getCookDuration());

        if (memory.getCookingPlayerUUID() != null) {
            dataConfig.set(locationKey + ".cookingPlayerUUID", memory.getCookingPlayerUUID().toString());
        } else {
            dataConfig.set(locationKey + ".cookingPlayerUUID", null);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            log.info("Failed to save campfire data: {}", e.getMessage());
        }
    }

    private void loadCampfireData() {
        if (dataConfig == null) return;

        for (String locationKey : dataConfig.getKeys(false)) {
            try {
                Location location = parseLocationKey(locationKey);
                if (location == null) continue;

                CampfireMemory memory = new CampfireMemory(location);

                for (int i = 0; i < CampfireConstants.ROWS * 9; i++) {
                    ItemStack slotItem = dataConfig.getItemStack(locationKey + ".allSlots." + i);
                    memory.setSlotItem(i, slotItem);
                }

                ItemStack bowl = dataConfig.getItemStack(locationKey + ".bowl");
                if (bowl != null && bowl.getType() != Material.AIR) {
                    memory.setBowl(bowl);
                }

                ItemStack output = dataConfig.getItemStack(locationKey + ".output");
                memory.setOutput(output);

                boolean isCooking = dataConfig.getBoolean(locationKey + ".cooking", false);
                long cookStartTime = dataConfig.getLong(locationKey + ".cookStartTime", 0);
                int cookDuration = dataConfig.getInt(locationKey + ".cookDuration", 0);

                String playerUUIDString = dataConfig.getString(locationKey + ".cookingPlayerUUID");
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

                campfires.put(locationKey, memory);
            } catch (Exception e) {
                log.info("Failed to load campfire data for {}: {}", locationKey, e.getMessage());
            }
        }
    }

    public void updateCooking() {
        for (Map.Entry<String, CampfireMemory> entry : campfires.entrySet()) {
            CampfireMemory data = entry.getValue();
            String locationKey = entry.getKey();

            if (data.isCooking() && data.isCookingComplete()) {
                Location location = parseLocationKey(locationKey);
                if (location != null) {
                    UUID cookingPlayerUUID = data.getCookingPlayerUUID();

                    createDish(data, location);
                    if (cookingPlayerUUID != null) {
                        Player cookingPlayer = Bukkit.getPlayer(cookingPlayerUUID);
                        if (cookingPlayer != null && cookingPlayer.isOnline()) {
                            cookingPlayer.sendMessage(
                                    Component.text("Cooking complete! Your campfire stew is ready!")
                                            .color(TextColor.color(UIConstants.DONE_COLOR)));
                            cookingPlayer.playSound(cookingPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        }
                    }
                    if (location.getWorld() != null) {
                        location.getWorld().playSound(location, Sound.BLOCK_CAMPFIRE_CRACKLE, 0.5f, 1.5f);
                    }
                }
            }
        }
    }

    public ItemStack createDish(CampfireMemory data, Location location) {
        if (data == null) {
            return null;
        }

        if (!data.isCookingComplete()) {
            return null;
        }

        UUID cookingPlayerUUID = data.getCookingPlayerUUID();

        int totalSaturation = CampfireConstants.BASE_SATURATION;
        StringBuilder ingredientList = new StringBuilder();
        for (ItemStack ingredient : data.getIngredients().values()) {
            if (ingredient != null && ingredient.getType() != Material.AIR) {
                int saturation = CampfireConstants.getIngredientSaturation(ingredient.getType());
                if (saturation > 0) {
                    totalSaturation += saturation;
                    if (!ingredientList.isEmpty()) {
                        ingredientList.append(", ");
                    }

                    String formattedName = UIUtils.toTitleCase(ingredient.getType().toString()
                            .replace('_', ' '));
                    ingredientList.append(formattedName);
                }
            }
        }

        ItemStack dish = new ItemStack(CampfireConstants.DISH_ITEM);
        ItemMeta meta = dish.getItemMeta();
        FoodComponent food = meta.getFood();

        food.setNutrition(totalSaturation);
        food.setSaturation((float) totalSaturation / 2);
        meta.setFood(food);
        if (meta != null) {
            meta.displayName(
                    Component.text("Campfire Stew")
                            .style(Style.style(UIConstants.STEW_COLOR, TextDecoration.ITALIC.withState(false))));

            meta.lore(Arrays.asList(
                    Component.text("Ingredients")
                            .style(Style.style(UIConstants.INFORMATION_COLOR, TextDecoration.ITALIC.withState(false)))
                            .append(Component.text(": ")
                                    .style(Style.style(UIConstants.SUB_INFORMATION_COLOR, TextDecoration.ITALIC.withState(false))))
                            .append(Component.text(ingredientList.toString())
                                    .style(Style.style(UIConstants.SUB_NOTIFICATION_COLOR, TextDecoration.ITALIC.withState(false)))),

                    Component.text("Total Saturation")
                            .style(Style.style(UIConstants.INFORMATION_COLOR, TextDecoration.ITALIC.withState(false)))
                            .append(Component.text(": ")
                                    .style(Style.style(UIConstants.SUB_INFORMATION_COLOR, TextDecoration.ITALIC.withState(false))))
                            .append(Component.text(totalSaturation)
                                    .style(Style.style(UIConstants.SUB_NOTIFICATION_COLOR, TextDecoration.ITALIC.withState(false))))
            ));


            dish.setItemMeta(meta);
        }

        for (int i = 0; i < CampfireConstants.ROWS * 9; i++) {
            if (CampfireConstants.INGREDIENT_SLOTS.contains(i) || i == CampfireConstants.BOWL_SLOT) {
                data.setSlotItem(i, null);
            }
        }
        data.setBowl(null);

        data.setOutput(dish);
        data.setCooking(false);
        data.setCookingPlayerUUID(cookingPlayerUUID);

        saveCampfireData(data, location);
        return dish;
    }

    public void cleanup() {
        for (Map.Entry<String, CampfireMemory> entry : campfires.entrySet()) {
            Location location = parseLocationKey(entry.getKey());
            if (location != null) {
                saveCampfireData(entry.getValue(), location);
            }
        }
        campfires.clear();
    }
}