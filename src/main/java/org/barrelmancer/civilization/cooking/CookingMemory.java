package org.barrelmancer.civilization.cooking;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public interface CookingMemory {
    Location getLocation();

    boolean isCooking();

    void setCooking(boolean cooking);

    boolean canStartCooking();

    boolean isCookingComplete();

    long getRemainingCookTime();

    long getCookStartTime();

    void setCookStartTime(long time);

    int getCookDuration();

    void setCookDuration(int duration);

    UUID getCookingPlayerUUID();

    void setCookingPlayerUUID(UUID uuid);

    ItemStack getOutput();

    void setOutput(ItemStack output);

    List<ItemStack> getAllSlotItems();

    void setSlotItem(int slot, ItemStack item);

    boolean hasValidInputs();

    int getValidInputCount();
}
