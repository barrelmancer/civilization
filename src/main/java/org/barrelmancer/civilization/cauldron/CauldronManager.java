package org.barrelmancer.civilization.cauldron;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.constants.CauldronConstants;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.cooking.BaseCookingManager;
import org.barrelmancer.civilization.cooking.CookingSystemType;
import org.barrelmancer.civilization.cooking.config.CookingConfig;
import org.barrelmancer.civilization.memory.CauldronMemory;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CauldronManager extends BaseCookingManager<CauldronMemory> {
    private static final Logger log = LoggerFactory.getLogger(CauldronManager.class);
    private static CauldronManager instance;

    private CauldronManager() {
        super(new CookingConfig(CauldronConstants.GUI_TITLE, "cauldron-data.yml",
                CauldronConstants.COOK_TICKS));
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CauldronManager();
            instance.initialize(plugin);
        }
    }

    public static CauldronManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CauldronManager not initialized! Call initialize() first.");
        }
        return instance;
    }

    @Override
    protected CauldronMemory createMemory(Location location) {
        return new CauldronMemory(location);
    }

    @Override
    protected void handleCookingComplete(CauldronMemory memory, Location location) {
        UUID cookingPlayerUUID = memory.getCookingPlayerUUID();

        createOutput(memory, location);

        clearInputAfterCooking(memory);

        if (cookingPlayerUUID != null) {
            Player cookingPlayer = Bukkit.getPlayer(cookingPlayerUUID);
            if (cookingPlayer != null && cookingPlayer.isOnline()) {
                sendCompletionMessage(cookingPlayer);
                playCompletionSound(cookingPlayer, location);
            }
        }
    }

    private void clearInputAfterCooking(CauldronMemory memory) {
        memory.setSlotItem(CauldronConstants.INPUT_SLOT, new ItemStack(Material.AIR));
        memory.setWaterPotion(null);
    }

    @Override
    protected void sendCompletionMessage(Player player) {
        player.sendMessage(
                Component.text("Boiling complete! Your boiling water is ready!")
                        .color(TextColor.color(UIConstants.DONE_COLOR)));
    }

    @Override
    protected void playCompletionSound(Player player, Location location) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    @Override
    public ItemStack createOutput(CauldronMemory memory, Location location) {
        if (memory == null || !memory.isCookingComplete()) {
            return null;
        }

        ItemStack boilingWater = createBoilingWaterPotion();

        memory.setOutput(boilingWater);
        memory.setCooking(false);

        saveCookingMemory(memory, location);
        return boilingWater;
    }

    private ItemStack createBoilingWaterPotion() {
        ItemStack potion = new ItemStack(CauldronConstants.OUTPUT_ITEM);
        ItemMeta meta = potion.getItemMeta();

        if (meta instanceof PotionMeta potionMeta) {
            potionMeta.displayName(
                    Component.text("Boiling Water")
                            .style(Style.style(UIConstants.WATER_COLOR, TextDecoration.ITALIC.withState(false))));

            potionMeta.setColor(Color.fromRGB(
                    UIConstants.WATER_COLOR.red(),
                    UIConstants.WATER_COLOR.green(),
                    UIConstants.WATER_COLOR.blue()));
            potionMeta.setBasePotionType(PotionType.AWKWARD);
            potionMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

            potion.setItemMeta(potionMeta);
        }

        return potion;
    }

    @Override
    public void saveCookingMemory(CauldronMemory memory, Location location) {
        String locationKey = getLocationKey(location);

        saveAllSlotItems(locationKey, memory);
        saveOutputItem(locationKey, memory);
        saveCookingState(locationKey, memory);
        saveToFile();
    }

    private void saveAllSlotItems(String locationKey, CauldronMemory memory) {
        for (int i = 0; i < memory.getAllSlotItems().size(); i++) {
            ItemStack slotItem = memory.getAllSlotItems().get(i);
            if (slotItem != null && slotItem.getType() != Material.AIR) {
                memoryConfig.set(locationKey + ".allSlots." + i, slotItem);
            } else {
                memoryConfig.set(locationKey + ".allSlots." + i, null);
            }
        }
    }

    private void saveOutputItem(String locationKey, CauldronMemory memory) {
        if (memory.getOutput() != null && memory.getOutput().getType() != Material.AIR) {
            memoryConfig.set(locationKey + ".output", memory.getOutput());
        } else {
            memoryConfig.set(locationKey + ".output", null);
        }
    }

    private void saveCookingState(String locationKey, CauldronMemory memory) {
        memoryConfig.set(locationKey + ".cooking", memory.isCooking());
        memoryConfig.set(locationKey + ".cookStartTime", memory.getCookStartTime());
        memoryConfig.set(locationKey + ".cookDuration", memory.getCookDuration());

        if (memory.getCookingPlayerUUID() != null) {
            memoryConfig.set(locationKey + ".cookingPlayerUUID", memory.getCookingPlayerUUID().toString());
        } else {
            memoryConfig.set(locationKey + ".cookingPlayerUUID", null);
        }
    }

    private void saveToFile() {
        try {
            memoryConfig.save(memoryFile);
        } catch (Exception e) {
            log.info("Failed to save cauldron data: {}", e.getMessage());
        }
    }

    @Override
    protected void loadCookingMemory() {
        if (memoryConfig == null) return;

        for (String locationKey : memoryConfig.getKeys(false)) {
            try {
                Location location = parseLocationKey(locationKey);
                if (location == null) continue;

                CauldronMemory memory = createAndLoadMemory(locationKey, location);
                cookingMemory.put(locationKey, memory);
            } catch (Exception e) {
                log.info("Failed to load cauldron data for {}: {}", locationKey, e.getMessage());
            }
        }
    }

    private CauldronMemory createAndLoadMemory(String locationKey, Location location) {
        CauldronMemory memory = new CauldronMemory(location);

        loadAllSlotItems(locationKey, memory);
        loadOutputAndState(locationKey, memory);
        loadCookingState(locationKey, memory);

        return memory;
    }

    private void loadAllSlotItems(String locationKey, CauldronMemory memory) {
        for (int i = 0; i < CauldronConstants.ROWS * 9; i++) {
            ItemStack slotItem = memoryConfig.getItemStack(locationKey + ".allSlots." + i);
            memory.setSlotItem(i, slotItem);
        }
    }

    private void loadOutputAndState(String locationKey, CauldronMemory memory) {
        ItemStack output = memoryConfig.getItemStack(locationKey + ".output");
        memory.setOutput(output);
    }

    private void loadCookingState(String locationKey, CauldronMemory memory) {
        boolean isCooking = memoryConfig.getBoolean(locationKey + ".cooking", false);
        long cookStartTime = memoryConfig.getLong(locationKey + ".cookStartTime", 0);
        int cookDuration = memoryConfig.getInt(locationKey + ".cookDuration", 0);

        String playerUUIDString = memoryConfig.getString(locationKey + ".cookingPlayerUUID");
        UUID cookingPlayerUUID = parsePlayerUUID(playerUUIDString);

        memory.setCooking(isCooking);
        memory.setCookStartTime(cookStartTime);
        memory.setCookDuration(cookDuration);
        memory.setCookingPlayerUUID(cookingPlayerUUID);
    }

    private UUID parsePlayerUUID(String playerUUIDString) {
        if (playerUUIDString != null && !playerUUIDString.isEmpty()) {
            try {
                return UUID.fromString(playerUUIDString);
            } catch (IllegalArgumentException e) {
                log.info("Invalid UUID format in save data: {}", playerUUIDString);
            }
        }
        return null;
    }
    @Override
    protected CookingSystemType getSystemType() {
        return CookingSystemType.CAULDRON;
    }
    @Override
    public void cleanup() {
        for (var entry : cookingMemory.entrySet()) {
            Location location = parseLocationKey(entry.getKey());
            if (location != null) {
                saveCookingMemory(entry.getValue(), location);
            }
        }
        cookingMemory.clear();
    }
}