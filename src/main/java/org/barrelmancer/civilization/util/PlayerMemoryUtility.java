package org.barrelmancer.civilization.util;

import org.barrelmancer.civilization.memory.DynamicPlayerMemory;
import org.barrelmancer.civilization.memory.SavablePlayerMemory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMemoryUtility {
    private static final Map<UUID, SavablePlayerMemory> savablePlayerMemory = new HashMap<>();
    private static final Map<UUID, DynamicPlayerMemory> dynamicPlayerMemory = new HashMap<>();

    public static DynamicPlayerMemory getDynamicPlayerMemory(Player player) {
        return dynamicPlayerMemory.get(player.getUniqueId());
    }

    public static void setDynamicPlayerMemory(Player player, DynamicPlayerMemory memory) {
        dynamicPlayerMemory.put(player.getUniqueId(), memory);
    }

    public static SavablePlayerMemory getSavablePlayerMemory(Player player) {
        if (!savablePlayerMemory.containsKey(player.getUniqueId())) {
            SavablePlayerMemory m = new SavablePlayerMemory();
            savablePlayerMemory.put(player.getUniqueId(), m);
            return m;
        }
        return savablePlayerMemory.get(player.getUniqueId());
    }

    public static void setSavablePlayerMemory(Player p, SavablePlayerMemory memory) {
        savablePlayerMemory.put(p.getUniqueId(), memory);
    }

    public static String getFolderPath(Player p) {
        return Bukkit.getPluginsFolder().getAbsolutePath() + "/civilization/player/" + p.getUniqueId();
    }
}
