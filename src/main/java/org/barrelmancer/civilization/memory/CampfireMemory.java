package org.barrelmancer.civilization.memory;

import org.barrelmancer.civilization.configuration.ServerConfiguration;
import org.barrelmancer.civilization.utility.MaterialUtility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CampfireMemory {
    private final Location location;
    private final HashMap<Integer, ItemStack> ingredients;
    private final List<ItemStack> allSlotItems;
    private ItemStack bowl;
    private ItemStack output;
    private boolean isCooking;
    private long cookStartTime;
    private int cookDuration = ServerConfiguration.getInt("campfire", "campfire.cook-ticks");
    private UUID cookingPlayerUUID;
    int size = ServerConfiguration.getInt("campfire", "campfire.size");
    List<Integer> ingredientSlots = ServerConfiguration.getIntList("campfire", "campfire.ingredient-slots");

    public CampfireMemory(Location location) {
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

        this.bowl = null;
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
            if (item != null && item.getType() != Material.AIR && MaterialUtility.isInConfigKeys("food", "food", item.getType())) {
                ingredients.put(slotIndex, item);
            } else {
                ingredients.remove(slotIndex);
            }
        } else if (slotIndex == ServerConfiguration.getInt("campfire", "campfire.bowl-slot")) {
            if (item != null && item.getType() == Material.BOWL) {
                this.bowl = item;
            } else {
                this.bowl = null;
            }
        }
    }

    public ItemStack getBowl() {
        return bowl;
    }

    public void setBowl(ItemStack bowl) {
        this.bowl = bowl;
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
        boolean hasValidIngredient = getValidIngredientCount() > 0;

        boolean hasValidBowl = bowl != null &&
                bowl.getType() != Material.AIR &&
                bowl.getType() == Material.BOWL;

        boolean notCookingAndNoOutput = !isCooking &&
                (output == null || output.getType() == Material.AIR);

        return hasValidIngredient && hasValidBowl && notCookingAndNoOutput;
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

    public int getValidIngredientCount() {
        int count = 0;
        for (ItemStack ingredient : ingredients.values()) {
            if (ingredient != null && ingredient.getType() != Material.AIR &&
                    MaterialUtility.isInConfigKeys("food", "food", ingredient.getType())) {
                count++;
            }
        }
        return count;
    }
}
