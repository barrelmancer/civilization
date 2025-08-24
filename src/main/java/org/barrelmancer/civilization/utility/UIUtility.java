package org.barrelmancer.civilization.utility;

import org.barrelmancer.civilization.constants.UIConstants;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.barrelmancer.civilization.item.ItemBuilder;
import org.bukkit.potion.PotionType;

public class UIUtility {
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    public static String toLowerCaseWithSpaces(Material material) {
        return material.toString().toLowerCase().replace("_", " ");
    }

    public static String buildBar(int status) {
        if (status > 95) {
            return "\uE800";
        } else if (status > 49) {
            return "\uE801";
        } else {
            return "\uE802";
        }

    }

    public static String locationToString(Location location) {
        if (location == null || location.getWorld() == null) {
            return "unknown";
        }
        return location.getWorld().getName() + ":" +
                location.getBlockX() + ":" +
                location.getBlockY() + ":" +
                location.getBlockZ();
    }

    public static Location parseLocationKey(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length != 4) return null;

            return new Location(
                    Bukkit.getWorld(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            );
        } catch (Exception e) {
            return null;
        }
    }
    public static ItemStack createCleanWaterPotion() {
        return ItemBuilder.createCustomPotion(
                PotionType.AWKWARD,
                "Clean Water",
                UIConstants.WATER_COLOR,
                UIConstants.WATER_COLOR
        ).build();
    }

    public static ItemStack createDirtyWaterPotion() {
        return ItemBuilder.createCustomPotion(
                PotionType.WATER,
                "Dirty Water",
                UIConstants.DIRTY_WATER_COLOR,
                UIConstants.DIRTY_WATER_COLOR
        ).build();
    }

}
