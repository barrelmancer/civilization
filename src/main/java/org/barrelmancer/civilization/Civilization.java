package org.barrelmancer.civilization;

import org.barrelmancer.civilization.configuration.Configuration;
import org.barrelmancer.civilization.constants.ThirstConstants;
import org.barrelmancer.civilization.events.*;
import org.barrelmancer.civilization.events.blocks.CampfireListener;
import org.barrelmancer.civilization.events.blocks.CauldronListener;
import org.barrelmancer.civilization.gui.MenuListener;
import org.barrelmancer.civilization.gui.containers.campfire.CampfireManager;
import org.barrelmancer.civilization.gui.containers.campfire.CampfireTask;
import org.barrelmancer.civilization.gui.containers.cauldron.CauldronManager;
import org.barrelmancer.civilization.gui.containers.cauldron.CauldronTask;
import org.barrelmancer.civilization.utility.MaterialUtility;
import org.barrelmancer.civilization.utility.SetAgeCommand;
import org.barrelmancer.civilization.misc.StatusBar;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class Civilization extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(Civilization.class);
    public static Civilization civilization;
    private MaterialUtility materialUtility;

    private static BukkitTask thirstTask;

    private static BukkitTask temperatureTask;

    private static BukkitTask statusBarTask;

    public static Civilization getCivilization() {
        return civilization;
    }

    @Override
    public void onEnable() {
        Map<String, String> configs = Map.of(
                "civilization", "configuration/configuration.yml",
                "campfire", "configuration/campfire.yml",
                "cauldron", "configuration/cauldron.yml",
                "food", "configuration/food.yml"
        );
        Configuration.initializeAll(this, configs);

        MaterialUtility.loadMaterialSetFromKeys("food", "food", "food");
        MaterialUtility.loadMaterialSetFromKeys("civilization", "ui-materials", "ui-materials");

        CampfireManager.initialize(this);
        CampfireTask.initialize(this);

        CauldronManager.initialize(this);
        CauldronTask.initialize(this);

        getServer().getPluginManager().registerEvents(new GeneralEvents(), this);
        getServer().getPluginManager().registerEvents(new ThirstEvents(), this);
        getServer().getPluginManager().registerEvents(new WorldEvents(), this);
        getServer().getPluginManager().registerEvents(new CampfireListener(), this);
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new CauldronListener(), this);

        thirstTask = getServer().getScheduler().runTaskTimer(this, Thirst.getInstance(), 20, ThirstConstants.BASE_THIRST_DECREASE_RATE);
        statusBarTask = getServer().getScheduler().runTaskTimer(this, StatusBar.getInstance(), 0, 5);
        temperatureTask = getServer().getScheduler().runTaskTimer(this, Temperature.getInstance(), 0, 5);

        getCommand("setage").setExecutor(new SetAgeCommand());

        log.info("Plugin has enabled successfully!");
        civilization = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            CampfireTask.shutdown();
            CauldronTask.shutdown();
            log.info("Stopped cooking timers");

            if (CampfireManager.getInstance() != null && CauldronManager.getInstance() != null) {
                CampfireManager.getInstance().cleanup();
                CauldronManager.getInstance().cleanup();
                log.info("CookingManagers cleaned up successfully");
            }

        } catch (Exception e) {
            log.info("Error during plugin shutdown: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
        log.info("Plugin has disabled successfully!");
    }
    public MaterialUtility getMaterialUtility() {
        return materialUtility;
    }
}
