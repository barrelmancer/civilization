package org.barrelmancer.civilization;

import org.barrelmancer.civilization.constants.TemperatureConstants;
import org.barrelmancer.civilization.memory.DynamicPlayerMemory;
import org.barrelmancer.civilization.util.PlayerMemoryUtility;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Temperature implements Runnable {
    private final static Temperature instance = new Temperature();

    private Temperature() {
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            DynamicPlayerMemory memory = PlayerMemoryUtility.getDynamicPlayerMemory(p);
            float temperature = checkTemperature(p);
            memory.setTemperature(temperature);
        }
    }

    private static float checkTemperature(Player p) {
        World world = p.getWorld();
        float temperature = TemperatureConstants.BASE_TEMPERATURE;
        ItemStack[] armor = p.getInventory().getArmorContents();
        for (ItemStack item : armor) {
            if (item != null) {
                temperature += TemperatureConstants.LEATHER_ARMOR_TEMPERATURE.getOrDefault(item.getType(), 0.0f);
            }
        }

        if (isNearActiveCampfire(p, 5)) {
            temperature += TemperatureConstants.CAMPFIRE_TEMPERATURE_INCREASE;
        }

        if (world.getTime() >= 13000 && world.getTime() <= 23000) {
            temperature += TemperatureConstants.NIGHT_TEMPERATURE_DECREASE;
        }

        if (temperature <= TemperatureConstants.BASE_MIN_TEMPERATURE) {
            p.setFreezeTicks(100);
        }

        if (TemperatureConstants.DESERT_BIOMES.contains(p.getLocation().getBlock().getBiome())) {
            temperature += TemperatureConstants.DESERT_TEMPERATURE_DELTA;
        }

        if (TemperatureConstants.SNOWY_BIOMES.contains(p.getLocation().getBlock().getBiome())) {
            temperature += TemperatureConstants.SNOWY_TEMPERATURE_DELTA;
        }

        if (temperature > TemperatureConstants.BASE_MAX_TEMPERATURE) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 21, 1, false, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 21, 1, false, false, false));
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 21, 1, false, false, false));
        }

        return (float) Math.ceil(temperature * 100.0f) / 100.0f;
    }

    private static boolean isNearActiveCampfire(Player player, int radius) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = world.getBlockAt(loc.clone().add(x, y, z));
                    if (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE) {
                        if (block.getBlockData() instanceof Campfire campfire && campfire.isLit()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    public static Temperature getInstance() {
        return instance;
    }
}
