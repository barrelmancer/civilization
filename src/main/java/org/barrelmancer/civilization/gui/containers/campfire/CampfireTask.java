package org.barrelmancer.civilization.gui.containers.campfire;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampfireTask extends BukkitRunnable {
    private static final Logger log = LoggerFactory.getLogger(CampfireTask.class);
    private static CampfireTask instance;

    private CampfireTask() {
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CampfireTask();
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

    public static CampfireTask getInstance() {
        return instance;
    }

    public static void shutdown() {
        if (instance != null && !instance.isCancelled()) {
            instance.cancel();
            instance = null;
            log.info("CampfireTask timer stopped and cleaned up");
        } else {
            log.info("CampfireTask already shut down or was never started");
        }
    }
}