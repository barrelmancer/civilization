package org.barrelmancer.civilization.memory;

import org.barrelmancer.civilization.constants.CampfireConstants;
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
    private int cookDuration;
    private UUID cookingPlayerUUID;

    public CampfireMemory(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        this.location = location.clone();
        this.ingredients = new HashMap<>();

        this.allSlotItems = new ArrayList<>(CampfireConstants.ROWS * 9);
        for (int i = 0; i < CampfireConstants.INGREDIENT_SLOTS.toArray().length; i++) {
            ingredients.put(CampfireConstants.INGREDIENT_SLOTS.get(i), null);
        }
        for (int i = 0; i < CampfireConstants.ROWS * 9; i++) {
            this.allSlotItems.add(null);
        }

        this.bowl = null;
        this.output = null;
        this.isCooking = false;
        this.cookStartTime = 0;
        this.cookDuration = 0;
        this.cookingPlayerUUID = null;
    }

    public UUID getCookingPlayerUUID() {
        return cookingPlayerUUID;
    }

    public void setCookingPlayerUUID(UUID playerUUID) {
        this.cookingPlayerUUID = playerUUID;
    }

    private String locationToString() {
        if (location == null || location.getWorld() == null) {
            return "unknown";
        }
        return location.getWorld().getName() + ":" +
                location.getBlockX() + ":" +
                location.getBlockY() + ":" +
                location.getBlockZ();
    }

    public HashMap<Integer, ItemStack> getIngredients() {
        return ingredients;
    }

    public List<ItemStack> getAllSlotItems() {
        return allSlotItems;
    }

    public void setSlotItem(int slotIndex, ItemStack item) {
        if (slotIndex < 0 || slotIndex >= CampfireConstants.ROWS * 9) {
            return;
        }
        allSlotItems.set(slotIndex, item);

        if (CampfireConstants.INGREDIENT_SLOTS.contains(slotIndex)) {
            if (item != null && item.getType() != Material.AIR && CampfireConstants.isValidIngredient(item.getType())) {
                ingredients.put(slotIndex, item);
            } else {
                ingredients.remove(slotIndex);
            }
        } else if (slotIndex == CampfireConstants.BOWL_SLOT) {
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
                CampfireConstants.isValidBowl(bowl.getType());

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
                    CampfireConstants.isValidIngredient(ingredient.getType())) {
                count++;
            }
        }
        return count;
    }
}