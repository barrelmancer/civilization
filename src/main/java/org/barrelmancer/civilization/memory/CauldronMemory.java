package org.barrelmancer.civilization.memory;

import org.barrelmancer.civilization.configuration.ServerConfiguration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CauldronMemory {
    private final Location location;
    private final HashMap<Integer, ItemStack> ingredients;
    private final List<ItemStack> allSlotItems;
    private ItemStack output;
    private boolean isCooking;
    private long cookStartTime;
    private int cookDuration = ServerConfiguration.getInt("cauldron", "cauldron.cook-ticks");
    private UUID cookingPlayerUUID;

    public ItemStack getWaterPotion() {
        return waterPotion;
    }

    public void setWaterPotion(ItemStack waterPotion) {
        this.waterPotion = waterPotion;
    }

    private ItemStack waterPotion;
    int size = ServerConfiguration.getInt("cauldron", "cauldron.size");
    List<Integer> ingredientSlots = ServerConfiguration.getIntList("cauldron", "cauldron.ingredient-slots");

    public CauldronMemory(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }


        this.location = location.clone();
        this.ingredients = new HashMap<>();

        this.allSlotItems = new ArrayList<>(size);
        for (int i = 0; i < ingredientSlots.toArray().length; i++) {
            ingredients.put(ingredientSlots.get(i), null);
        }
        for (int i = 0; i < size; i++) {
            this.allSlotItems.add(null);
        }

        this.output = null;
        this.isCooking = false;
        this.cookStartTime = 0;
        this.cookingPlayerUUID = null;
    }
    public UUID getCookingPlayerUUID() {
        return cookingPlayerUUID;
    }

    public void setCookingPlayerUUID(UUID playerUUID) {
        this.cookingPlayerUUID = playerUUID;
    }

    public HashMap<Integer, ItemStack> getIngredients() {
        return ingredients;
    }

    public List<ItemStack> getAllSlotItems() {
        return allSlotItems;
    }

    public void setSlotItem(int slotIndex, ItemStack item) {
        if (slotIndex < 0 || slotIndex >= size) {
            return;
        }
        allSlotItems.set(slotIndex, item);

        if (ingredientSlots.contains(slotIndex)) {
            if (item != null && item.getType() != Material.AIR && isValidWaterPotion(item)) {
                waterPotion = item;
                ingredients.put(slotIndex, item);
            } else {
                if (waterPotion != null && ingredients.get(slotIndex) != null) {
                    waterPotion = null;
                }
                ingredients.remove(slotIndex);
            }
        }
    }

    public ItemStack getOutput() {
        return output;
    }

    public void setOutput(ItemStack output) {
        this.output = output;
    }

    public boolean isCooking() {
        return isCooking;
    }

    public void setCooking(boolean cooking) {
        this.isCooking = cooking;
    }

    public long getCookStartTime() {
        return cookStartTime;
    }

    public void setCookStartTime(long cookStartTime) {
        this.cookStartTime = cookStartTime;
    }

    public int getCookDuration() {
        return cookDuration;
    }

    public void setCookDuration(int cookDuration) {
        this.cookDuration = cookDuration;
    }

    public boolean canStartCooking() {
        boolean hasValidInputs = waterPotion != null;

        boolean notCurrentlyCooking = !isCooking;

        boolean noExistingOutput = (output == null || output.getType() == Material.AIR);

        return hasValidInputs && notCurrentlyCooking && noExistingOutput;
    }

    public boolean isCookingComplete() {
        if (!isCooking) {
            return false;
        }

        long cookTimeMs = cookDuration * 50L;
        long currentTime = System.currentTimeMillis();

        return currentTime >= (cookStartTime + cookTimeMs);
    }

    public long getRemainingCookTime() {
        if (!isCooking) return 0;

        long cookTimeMs = cookDuration * 50L;
        long elapsed = System.currentTimeMillis() - cookStartTime;

        return Math.max(0, cookTimeMs - elapsed);
    }

        private boolean isValidWaterPotion(ItemStack item) {
            return item != null &&
                    item.getType() == Material.POTION &&
                    item.getItemMeta() instanceof PotionMeta meta &&
                    meta.getBasePotionType() == PotionType.WATER;
        }
    }
