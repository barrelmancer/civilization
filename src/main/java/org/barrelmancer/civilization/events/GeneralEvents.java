package org.barrelmancer.civilization.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.constants.TemperatureConstants;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.memory.DynamicPlayerMemory;
import org.barrelmancer.civilization.memory.SavablePlayerMemory;
import org.barrelmancer.civilization.util.PlayerMemoryUtility;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

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
        } catch (IOException e) {
            log.info(e.toString());
        }
    }

    @EventHandler
    private void onClickInventory(InventoryClickEvent event) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        Float temperature = TemperatureConstants.LEATHER_ARMOR_TEMPERATURE.get(clickedItem.getType());
        if (clickedItem.getType() == Material.CAMPFIRE) {
            temperature = TemperatureConstants.CAMPFIRE_TEMPERATURE_INCREASE;
        }

        if (temperature != null) {
            List<Component> lore = Collections.singletonList(
                    Component.text("+ " + temperature + "°C")
                            .style(Style.style(TextColor.color(UIConstants.INFORMATION_COLOR), TextDecoration.ITALIC.withState(false)))
            );
            meta.lore(lore);
            clickedItem.setItemMeta(meta);
            event.setCurrentItem(clickedItem);
        }
    }
}

