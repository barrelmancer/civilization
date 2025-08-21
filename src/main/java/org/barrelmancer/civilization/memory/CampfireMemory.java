package org.barrelmancer.civilization.memory;

import org.barrelmancer.civilization.constants.CampfireConstants;
import org.barrelmancer.civilization.cooking.BaseCookingMemory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class CampfireMemory extends BaseCookingMemory {
    private final HashMap<Integer, ItemStack> ingredients;
    private ItemStack bowl;

    public CampfireMemory(Location location) {
        super(location, CampfireConstants.ROWS * 9);
        this.ingredients = new HashMap<>();
        this.bowl = null;

        for (int slot : CampfireConstants.INGREDIENT_SLOTS) {
            ingredients.put(slot, null);
        }
    }

    public HashMap<Integer, ItemStack> getIngredients() {
        return ingredients;
    }

    public ItemStack getBowl() {
        return bowl;
    }

    public void setBowl(ItemStack bowl) {
        this.bowl = bowl;
    }

    public void setIngredient(int slot, ItemStack ingredient) {
        ingredients.put(slot, ingredient);
    }

    @Override
    protected void onSlotItemChanged(int slotIndex, ItemStack item) {
        if (CampfireConstants.INGREDIENT_SLOTS.contains(slotIndex)) {
            if (item != null && item.getType() != Material.AIR &&
                    CampfireConstants.isValidIngredient(item.getType())) {
                ingredients.put(slotIndex, item.clone());
            } else {
                ingredients.remove(slotIndex);
            }
        } else if (slotIndex == CampfireConstants.BOWL_SLOT) {
            if (item != null && item.getType() == Material.BOWL) {
                this.bowl = item.clone();
            } else {
                this.bowl = null;
            }
        } else {
            this.output = null;
        }
    }

    @Override
    public boolean canStartCooking() {
        boolean hasValidInputs = hasValidInputs();
        boolean notCurrentlyCooking = !isCooking;
        boolean noExistingOutput = (output == null || output.getType() == Material.AIR);
        boolean meetsAdditionalRequirements = hasAdditionalRequirements();
        return hasValidInputs && notCurrentlyCooking && noExistingOutput && meetsAdditionalRequirements;
    }

    @Override
    public boolean hasValidInputs() {
        return getValidInputCount() > 0;
    }

    @Override
    public int getValidInputCount() {
        int count = 0;
        for (ItemStack ingredient : ingredients.values()) {
            if (ingredient != null && ingredient.getType() != Material.AIR &&
                    CampfireConstants.isValidIngredient(ingredient.getType())) {
                count++;
            }
        }
        return count;
    }

    @Override
    protected boolean hasAdditionalRequirements() {
        return bowl != null && bowl.getType() != Material.AIR &&
                CampfireConstants.isValidBowl(bowl.getType());
    }
}