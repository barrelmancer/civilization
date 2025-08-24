package org.barrelmancer.civilization.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMenuUtilityManager {
    private static final Map<UUID, PlayerMenuUtility> playerMenuMap = new HashMap<>();

    public static PlayerMenuUtility getPlayerMenuUtility(Player player){
        playerMenuMap.putIfAbsent(player.getUniqueId(), new PlayerMenuUtility(player));
        return playerMenuMap.get(player.getUniqueId());
    }

    public static void removePlayerMenuUtility(UUID uuid){
        playerMenuMap.remove(uuid);
    }
}
