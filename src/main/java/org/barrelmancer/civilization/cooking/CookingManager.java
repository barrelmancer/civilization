package org.barrelmancer.civilization.cooking;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;


public interface CookingManager<T extends CookingMemory> {
    T getCookingMemory(Location location);

    void removeCookingData(Location location);

    void startCooking(T memory, Location location, UUID playerUUID);

    void saveCookingMemory(T memory, Location location);

    void updateCooking();

    ItemStack createOutput(T memory, Location location);

    void cleanup();
}