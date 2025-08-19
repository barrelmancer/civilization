package org.barrelmancer.civilization;

import org.barrelmancer.civilization.campfire.CampfireCooking;
import org.barrelmancer.civilization.campfire.CampfireManager;
import org.barrelmancer.civilization.constants.ThirstConstants;
import org.barrelmancer.civilization.events.CampfireEvents;
import org.barrelmancer.civilization.events.CampfireGUIEvents;
import org.barrelmancer.civilization.events.GeneralEvents;
import org.barrelmancer.civilization.events.ThirstEvents;
import org.barrelmancer.civilization.util.StatusBar;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Civilization extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(Civilization.class);
    public static Civilization civilization;

    private static BukkitTask thirstTask;

    private static BukkitTask temperatureTask;

    private static BukkitTask statusBarTask;

    public static Civilization getCivilization() {
        return civilization;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        CampfireManager.initialize(this);
        CampfireCooking.initialize(this);

        getServer().getPluginManager().registerEvents(new CampfireGUIEvents(), this);
        getServer().getPluginManager().registerEvents(new CampfireEvents(), this);

        getServer().getPluginManager().registerEvents(new GeneralEvents(), this);
        getServer().getPluginManager().registerEvents(new ThirstEvents(), this);

        thirstTask = getServer().getScheduler().runTaskTimer(this, Thirst.getInstance(), 20, ThirstConstants.BASE_THIRST_DECREASE_RATE);
        statusBarTask = getServer().getScheduler().runTaskTimer(this, StatusBar.getInstance(), 0, 5);
        temperatureTask = getServer().getScheduler().runTaskTimer(this, Temperature.getInstance(), 0, 5);
        log.info("Plugin has enabled successfully!");
        civilization = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            CampfireCooking.shutdown();
            log.info("Stopped campfire cooking timer");

            if (CampfireManager.getInstance() != null) {
                CampfireManager.getInstance().cleanup();
                log.info("CampfireManager cleaned up successfully");
            }

        } catch (Exception e) {
            log.info("Error during plugin shutdown: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
        log.info("Plugin has disabled successfully!");
    }
}
