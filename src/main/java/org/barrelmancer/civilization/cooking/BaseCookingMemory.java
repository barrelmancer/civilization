package org.barrelmancer.civilization.cooking;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class BaseCookingMemory implements CookingMemory {
    protected final Location location;
    protected final List<ItemStack> allSlotItems;
    protected final int totalSlots;
    protected ItemStack output;
    protected boolean isCooking;
    protected long cookStartTime;
    protected int cookDuration;
    protected UUID cookingPlayerUUID;

    protected BaseCookingMemory(Location location, int totalSlots) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        this.location = location.clone();
        this.totalSlots = totalSlots;
        this.allSlotItems = new ArrayList<>(totalSlots);

        for (int i = 0; i < totalSlots; i++) {
            this.allSlotItems.add(null);
        }

        this.output = null;
        this.isCooking = false;
        this.cookStartTime = 0;
        this.cookDuration = 0;
        this.cookingPlayerUUID = null;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public boolean isCooking() {
        return isCooking;
    }

    @Override
    public void setCooking(boolean cooking) {
        this.isCooking = cooking;
    }

    @Override
    public long getCookStartTime() {
        return cookStartTime;
    }

    @Override
    public void setCookStartTime(long cookStartTime) {
        this.cookStartTime = cookStartTime;
    }

    @Override
    public int getCookDuration() {
        return cookDuration;
    }

    @Override
    public void setCookDuration(int cookDuration) {
        this.cookDuration = cookDuration;
    }

    @Override
    public UUID getCookingPlayerUUID() {
        return cookingPlayerUUID;
    }

    @Override
    public void setCookingPlayerUUID(UUID cookingPlayerUUID) {
        this.cookingPlayerUUID = cookingPlayerUUID;
    }

    @Override
    public ItemStack getOutput() {
        return output;
    }

    @Override
    public void setOutput(ItemStack output) {
        this.output = output;
    }

    @Override
    public List<ItemStack> getAllSlotItems() {
        return allSlotItems;
    }

    @Override
    public void setSlotItem(int slotIndex, ItemStack item) {
        if (slotIndex < 0 || slotIndex >= totalSlots) {
            return;
        }
        allSlotItems.set(slotIndex, item);

        onSlotItemChanged(slotIndex, item);
    }

    @Override
    public boolean isCookingComplete() {
        if (!isCooking) return false;

        long cookTimeMs = cookDuration * 50L;
        long currentTime = System.currentTimeMillis();
        return currentTime >= (cookStartTime + cookTimeMs);
    }

    @Override
    public long getRemainingCookTime() {
        if (!isCooking) return 0;

        long cookTimeMs = cookDuration * 50L;
        long elapsed = System.currentTimeMillis() - cookStartTime;
        return Math.max(0, cookTimeMs - elapsed);
    }

    @Override
    public boolean canStartCooking() {
        boolean hasValidInputs = hasValidInputs();

        boolean notCurrentlyCooking = !isCooking;

        boolean noExistingOutput = (output == null || output.getType() == Material.AIR);

        boolean meetsAdditionalRequirements = hasAdditionalRequirements();

        return hasValidInputs && notCurrentlyCooking && noExistingOutput && meetsAdditionalRequirements;
    }

    protected abstract void onSlotItemChanged(int slotIndex, ItemStack item);

    protected abstract boolean hasAdditionalRequirements();

}
