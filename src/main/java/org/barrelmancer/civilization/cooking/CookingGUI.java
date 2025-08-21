package org.barrelmancer.civilization.cooking;

import org.bukkit.Location;

public interface CookingGUI {
    Location getLocation();

    org.bukkit.inventory.Inventory getInventory();
}