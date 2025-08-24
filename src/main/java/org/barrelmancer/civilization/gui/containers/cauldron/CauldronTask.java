package org.barrelmancer.civilization.gui.containers.cauldron;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CauldronTask extends BukkitRunnable {
    private static final Logger log = LoggerFactory.getLogger(CauldronTask.class);
    private static CauldronTask instance;

    private CauldronTask() {
    }

    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CauldronTask();
            instance.runTaskTimer(plugin, 0L, 20L);
        }
    }

    @Override
    public void run() {
        try {
            if (CauldronManager.getInstance() != null) {
                CauldronManager.getInstance().updateCooking();
            }
        } catch (IllegalStateException e) {
            log.info("CauldronManager not initialized yet, skipping cooking update");
        } catch (Exception e) {
            log.info("Error during cooking update: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    public static CauldronTask getInstance() {
        return instance;
    }

    public static void shutdown() {
        if (instance != null && !instance.isCancelled()) {
            instance.cancel();
            instance = null;
            log.info("CauldronTask timer stopped and cleaned up");
        } else {
            log.info("CauldronTask already shut down or was never started");
        }
    }
}