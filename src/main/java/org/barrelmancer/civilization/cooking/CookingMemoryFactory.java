package org.barrelmancer.civilization.cooking;

import org.barrelmancer.civilization.memory.CampfireMemory;
import org.barrelmancer.civilization.memory.CauldronMemory;
import org.bukkit.Location;

public class CookingMemoryFactory {

    public static BaseCookingMemory createMemory(CookingSystemType type, Location location) {
        return switch (type) {
            case CAMPFIRE -> new CampfireMemory(location);
            case CAULDRON -> new CauldronMemory(location);
        };
    }
}