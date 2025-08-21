package org.barrelmancer.civilization.campfire;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.constants.CampfireConstants;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.cooking.BaseCookingManager;
import org.barrelmancer.civilization.cooking.CookingSystemType;
import org.barrelmancer.civilization.memory.CampfireMemory;
import org.barrelmancer.civilization.util.UIUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

public class CampfireManager extends BaseCookingManager<CampfireMemory> {
    private static final Logger log = LoggerFactory.getLogger(CampfireManager.class);
    private static CampfireManager instance;

    private CampfireManager() {
        super(new CampfireConfig(CampfireConstants.GUI_TITLE, "campfire-data.yml",
                CampfireConstants.COOK_TICKS, CampfireConstants.BASE_SATURATION));
    }

    public static void init(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CampfireManager();
            instance.initialize(plugin);
        }
    }

    public static CampfireManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("CampfireManager not initialized! Call initialize() first.");
        }
        return instance;
    }

    @Override
    protected CampfireMemory createMemory(Location location) {
        return new CampfireMemory(location);
    }

    @Override
    protected void handleCookingComplete(CampfireMemory memory, Location location) {
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

    @Override
    protected void sendCompletionMessage(Player player) {
        player.sendMessage(
                Component.text("Cooking complete! Your campfire stew is ready!")
                        .color(TextColor.color(UIConstants.DONE_COLOR)));
    }

    @Override
    protected void playCompletionSound(Player player, Location location) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    @Override
    public ItemStack createOutput(CampfireMemory memory, Location location) {
        if (memory == null || !memory.isCookingComplete()) {
            return null;
        }

        UUID cookingPlayerUUID = memory.getCookingPlayerUUID();

        int totalSaturation = ((CampfireConfig) config).getBaseSaturation();
        StringBuilder ingredientList = new StringBuilder();

        for (ItemStack ingredient : memory.getIngredients().values()) {
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
        ItemStack output = new ItemStack(CampfireConstants.OUTPUT_ITEM);
        ItemMeta meta = output.getItemMeta();
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
            meta.setCustomModelData(CampfireConstants.OUTPUT_ITEM_CUSTOM_MODEL_DATA);
            output.setItemMeta(meta);
        }

        memory.setOutput(output);
        memory.setCooking(false);
        memory.setCookingPlayerUUID(cookingPlayerUUID);

        saveCookingMemory(memory, location);
        return output;
    }

    @Override
    public void saveCookingMemory(CampfireMemory memory, Location location) {
        String locationKey = getLocationKey(location);

        for (int i = 0; i < memory.getAllSlotItems().size(); i++) {
            ItemStack slotItem = memory.getAllSlotItems().get(i);
            if (slotItem != null && slotItem.getType() != Material.AIR) {
                memoryConfig.set(locationKey + ".allSlots." + i, slotItem);
            } else {
                memoryConfig.set(locationKey + ".allSlots." + i, null);
            }
        }

        if (memory.getBowl() != null && memory.getBowl().getType() != Material.AIR) {
            memoryConfig.set(locationKey + ".bowl", memory.getBowl());
        } else {
            memoryConfig.set(locationKey + ".bowl", null);
        }

        if (memory.getOutput() != null && memory.getOutput().getType() != Material.AIR) {
            memoryConfig.set(locationKey + ".output", memory.getOutput());
        } else {
            memoryConfig.set(locationKey + ".output", null);
        }

        memoryConfig.set(locationKey + ".cooking", memory.isCooking());
        memoryConfig.set(locationKey + ".cookStartTime", memory.getCookStartTime());
        memoryConfig.set(locationKey + ".cookDuration", memory.getCookDuration());

        if (memory.getCookingPlayerUUID() != null) {
            memoryConfig.set(locationKey + ".cookingPlayerUUID", memory.getCookingPlayerUUID().toString());
        } else {
            memoryConfig.set(locationKey + ".cookingPlayerUUID", null);
        }

        try {
            memoryConfig.save(memoryFile);
        } catch (Exception e) {
            log.info("Failed to save campfire data: {}", e.getMessage());
        }
    }

    @Override
    protected void loadCookingMemory() {
        if (memoryConfig == null) return;

        for (String locationKey : memoryConfig.getKeys(false)) {
            try {
                Location location = parseLocationKey(locationKey);
                if (location == null) continue;

                CampfireMemory memory = new CampfireMemory(location);

                for (int i = 0; i < CampfireConstants.ROWS * 9; i++) {
                    ItemStack slotItem = memoryConfig.getItemStack(locationKey + ".allSlots." + i);
                    memory.setSlotItem(i, slotItem);
                }

                ItemStack bowl = memoryConfig.getItemStack(locationKey + ".bowl");
                if (bowl != null && bowl.getType() != Material.AIR) {
                    memory.setBowl(bowl);
                }

                ItemStack output = memoryConfig.getItemStack(locationKey + ".output");
                memory.setOutput(output);

                boolean isCooking = memoryConfig.getBoolean(locationKey + ".cooking", false);
                long cookStartTime = memoryConfig.getLong(locationKey + ".cookStartTime", 0);
                int cookDuration = memoryConfig.getInt(locationKey + ".cookDuration", 0);

                String playerUUIDString = memoryConfig.getString(locationKey + ".cookingPlayerUUID");
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

                cookingMemory.put(locationKey, memory);
            } catch (Exception e) {
                log.info("Failed to load campfire data for {}: {}", locationKey, e.getMessage());
            }
        }
    }

    private void clearInputAfterCooking(CampfireMemory memory) {
        for (Object i : CampfireConstants.INGREDIENT_SLOTS.toArray()) {
            memory.setSlotItem((int) i, null);
        }
        memory.setSlotItem(memory.getAllSlotItems().indexOf(memory.getBowl()), null);
        memory.setBowl(null);
    }
    @Override
    protected CookingSystemType getSystemType() {
        return CookingSystemType.CAMPFIRE;
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