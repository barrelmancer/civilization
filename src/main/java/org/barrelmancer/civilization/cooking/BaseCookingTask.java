package org.barrelmancer.civilization.cooking;

import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCookingTask extends BukkitRunnable {
    private static final Logger log = LoggerFactory.getLogger(BaseCookingTask.class);

    protected abstract CookingManager<?> getCookingManager();
    protected abstract String getTaskName();

    @Override
    public void run() {
        try {
            CookingManager<?> manager = getCookingManager();
            if (manager != null) {
                manager.updateCooking();
            }
        } catch (IllegalStateException e) {
            log.info("{} not initialized yet, skipping cooking update", getTaskName());
        } catch (Exception e) {
            log.info("Error during {} update: {} - {}", getTaskName(), e.getClass().getSimpleName(), e.getMessage());
        }
    }
}