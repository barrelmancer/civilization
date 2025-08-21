package org.barrelmancer.civilization.cooking;

import org.barrelmancer.civilization.campfire.CampfireGUI;
import org.barrelmancer.civilization.campfire.CampfireManager;
import org.barrelmancer.civilization.campfire.CampfireCookingTask;
import org.barrelmancer.civilization.cauldron.CauldronGUI;
import org.barrelmancer.civilization.cauldron.CauldronManager;
import org.barrelmancer.civilization.cauldron.CauldronCookingTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class CookingSystemFactory {
    private static CookingSystemFactory instance;
    private final Map<Material, CookingSystemType> blockToCookingSystem;
    private final Map<CookingSystemType, CookingManager<?>> managers;
    private final Map<CookingSystemType, BaseCookingTask> tasks;

    private CookingSystemFactory() {
        this.blockToCookingSystem = new HashMap<>();
        this.managers = new HashMap<>();
        this.tasks = new HashMap<>();
        initializeBlockMappings();
    }

    public static CookingSystemFactory getInstance() {
        if (instance == null) {
            instance = new CookingSystemFactory();
        }
        return instance;
    }

    private void initializeBlockMappings() {
        blockToCookingSystem.put(Material.CAMPFIRE, CookingSystemType.CAMPFIRE);
        blockToCookingSystem.put(Material.CAULDRON, CookingSystemType.CAULDRON);
    }

    public void initializeAllSystems(JavaPlugin plugin) {
        CampfireManager.init(plugin);
        CauldronManager.init(plugin);

        registerCookingSystem(CookingSystemType.CAMPFIRE,
                CampfireManager.getInstance(),
                new CampfireCookingTask());

        registerCookingSystem(CookingSystemType.CAULDRON,
                CauldronManager.getInstance(),
                new CauldronCookingTask());

        initializeTasks(plugin);
    }

    public void registerCookingSystem(CookingSystemType type, CookingManager<?> manager, BaseCookingTask task) {
        managers.put(type, manager);
        tasks.put(type, task);
    }

    public CookingSystemType getCookingSystemType(Material blockType) {
        return blockToCookingSystem.get(blockType);
    }

    public CookingManager<?> getManager(CookingSystemType type) {
        return managers.get(type);
    }

    public CookingGUI createGUI(Material blockType, Location location) {
        CookingSystemType type = getCookingSystemType(blockType);
        if (type == null) return null;

        return switch (type) {
            case CAMPFIRE -> new CampfireGUI(location);
            case CAULDRON -> new CauldronGUI(location);
        };
    }

    public void initializeTasks(JavaPlugin plugin) {
        for (BaseCookingTask task : tasks.values()) {
            if (task != null) {
                task.runTaskTimer(plugin, 0L, 20L);
            }
        }
    }

    public void shutdownTasks() {
        for (BaseCookingTask task : tasks.values()) {
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }
        }

        for (CookingManager<?> manager : managers.values()) {
            if (manager != null) {
                manager.cleanup();
            }
        }
    }
}