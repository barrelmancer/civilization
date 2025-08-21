package org.barrelmancer.civilization.memory;

import org.barrelmancer.civilization.constants.CauldronConstants;
import org.barrelmancer.civilization.cooking.BaseCookingMemory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class CauldronMemory extends BaseCookingMemory {
    private ItemStack waterPotion;

    public CauldronMemory(Location location) {
        super(location, CauldronConstants.ROWS * 9);
        this.waterPotion = null;
    }

    public ItemStack getWaterPotion() {
        return waterPotion;
    }

    public void setWaterPotion(ItemStack waterPotion) {
        this.waterPotion = waterPotion;
    }

    @Override
    protected void onSlotItemChanged(int slotIndex, ItemStack item) {
        if (slotIndex == CauldronConstants.INPUT_SLOT) {
            if (isValidWaterPotion(item)) {
                waterPotion = item;
            } else {
                waterPotion = null;
            }
        }
    }

    @Override
    public boolean hasValidInputs() {
        return waterPotion != null;
    }

    @Override
    public int getValidInputCount() {
        return waterPotion != null ? 1 : 0;
    }

    @Override
    protected boolean hasAdditionalRequirements() {
        return true;
    }

    private boolean isValidWaterPotion(ItemStack item) {
        return item != null &&
                item.getType() == Material.POTION &&
                item.getItemMeta() instanceof PotionMeta meta &&
                meta.getBasePotionType() == PotionType.WATER;
    }
}