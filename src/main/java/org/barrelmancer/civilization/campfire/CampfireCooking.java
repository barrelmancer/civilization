package org.barrelmancer.civilization.campfire;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampfireCooking extends BukkitRunnable {
    private static final Logger log = LoggerFactory.getLogger(CampfireCooking.class);
    private static CampfireCooking instance;

    private CampfireCooking() {
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CampfireCooking();
            instance.runTaskTimer(plugin, 0L, 20L);
        }
    }

    @Override
    public void run() {
        try {
            if (CampfireManager.getInstance() != null) {
                CampfireManager.getInstance().updateCooking();
            }
        } catch (IllegalStateException e) {
            log.info("CampfireManager not initialized yet, skipping cooking update");
        } catch (Exception e) {
            log.info("Error during cooking update: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public static CampfireCooking getInstance() {
        return instance;
    }

    public static void shutdown() {
        if (instance != null && !instance.isCancelled()) {
            instance.cancel();
            instance = null;
            log.info("CampfireCooking timer stopped and cleaned up");
        } else {
            log.info("CampfireCooking already shut down or was never started");
        }
    }
}