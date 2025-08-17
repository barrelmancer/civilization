package org.barrelmancer.civilization.events;

import org.barrelmancer.civilization.memory.DynamicPlayerMemory;
import org.barrelmancer.civilization.memory.SavablePlayerMemory;
import org.barrelmancer.civilization.util.PlayerMemoryUtility;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class GeneralEvents implements Listener {
    private static final Logger log = LoggerFactory.getLogger(GeneralEvents.class);

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        SavablePlayerMemory savableMemory = PlayerMemoryUtility.getSavablePlayerMemory(event.getPlayer());
        PlayerMemoryUtility.setDynamicPlayerMemory(event.getPlayer(), new DynamicPlayerMemory());
        File f = new File(PlayerMemoryUtility.getFolderPath(event.getPlayer()) + ".yml");

        if (f.exists()) {
            FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
            savableMemory.setThirst(cfg.getInt("stats.thirst"));
        } else {
            savableMemory.setThirst(100);
        }
        PlayerMemoryUtility.setSavablePlayerMemory(event.getPlayer(), savableMemory);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        SavablePlayerMemory savableMemory = PlayerMemoryUtility.getSavablePlayerMemory(event.getPlayer());
        File f = new File(PlayerMemoryUtility.getFolderPath(event.getPlayer()) + ".yml");
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        cfg.set("stats.thirst", savableMemory.getThirst());
        try {
            cfg.save(f);
            log.info(event.getPlayer().getName() + " " + savableMemory.getThirst());
        } catch (IOException e) {
            log.info(e.toString());
        }
    }
}
