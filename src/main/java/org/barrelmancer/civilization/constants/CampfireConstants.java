package org.barrelmancer.civilization.constants;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CampfireConstants {
    //100 ticks = 5 seconds
    public static final int COOK_TICKS = 100;

    public static final int BASE_SATURATION = 5;
    public static final String GUI_TITLE = "Campfire";
    public static final int ROWS = 3;

    public static final Material DECORATIVE = Material.GRAY_STAINED_GLASS_PANE;
    public static final Material DISH_DECORATIVE = Material.WHITE_STAINED_GLASS_PANE;
    public static final ArrayList<Integer> INGREDIENT_SLOTS = new ArrayList<>();

    static {
        INGREDIENT_SLOTS.add(1);
        INGREDIENT_SLOTS.add(10);
        INGREDIENT_SLOTS.add(19);
    }

    public static final int BOWL_SLOT = 12;
    public static final int OUTPUT_SLOT = 14;

    public static final Material DISH_ITEM = Material.RABBIT_STEW;
    public static final Map<Material, Integer> INGREDIENT_SATURATION = new HashMap<>();

    static {
        INGREDIENT_SATURATION.put(Material.CARROT, 3);
        INGREDIENT_SATURATION.put(Material.POTATO, 4);
        INGREDIENT_SATURATION.put(Material.BEETROOT, 2);
        INGREDIENT_SATURATION.put(Material.SWEET_BERRIES, 2);

        INGREDIENT_SATURATION.put(Material.BEEF, 8);
        INGREDIENT_SATURATION.put(Material.PORKCHOP, 7);
        INGREDIENT_SATURATION.put(Material.CHICKEN, 6);
        INGREDIENT_SATURATION.put(Material.MUTTON, 6);
        INGREDIENT_SATURATION.put(Material.RABBIT, 5);
        INGREDIENT_SATURATION.put(Material.COD, 5);
        INGREDIENT_SATURATION.put(Material.SALMON, 6);

        INGREDIENT_SATURATION.put(Material.COOKED_BEEF, 12);
        INGREDIENT_SATURATION.put(Material.COOKED_PORKCHOP, 10);
        INGREDIENT_SATURATION.put(Material.COOKED_CHICKEN, 8);
        INGREDIENT_SATURATION.put(Material.COOKED_MUTTON, 8);
        INGREDIENT_SATURATION.put(Material.COOKED_RABBIT, 7);
        INGREDIENT_SATURATION.put(Material.COOKED_COD, 7);
        INGREDIENT_SATURATION.put(Material.COOKED_SALMON, 8);
        INGREDIENT_SATURATION.put(Material.BAKED_POTATO, 6);

        INGREDIENT_SATURATION.put(Material.RED_MUSHROOM, 3);
        INGREDIENT_SATURATION.put(Material.BROWN_MUSHROOM, 3);
        INGREDIENT_SATURATION.put(Material.BREAD, 5);
        INGREDIENT_SATURATION.put(Material.APPLE, 4);
        INGREDIENT_SATURATION.put(Material.GOLDEN_APPLE, 15);
        INGREDIENT_SATURATION.put(Material.ENCHANTED_GOLDEN_APPLE, 25);
        INGREDIENT_SATURATION.put(Material.KELP, 1);
        INGREDIENT_SATURATION.put(Material.DRIED_KELP, 2);
        INGREDIENT_SATURATION.put(Material.PUMPKIN, 3);
        INGREDIENT_SATURATION.put(Material.MELON_SLICE, 2);
        INGREDIENT_SATURATION.put(Material.CHORUS_FRUIT, 4);
        INGREDIENT_SATURATION.put(Material.SPIDER_EYE, 2);
        INGREDIENT_SATURATION.put(Material.ROTTEN_FLESH, 1);
    }

    public static boolean isValidIngredient(Material material) {
        if (material == null) return false;
        return INGREDIENT_SATURATION.containsKey(material);
    }

    public static boolean isValidBowl(Material material) {
        return material == Material.BOWL;
    }

    public static int getIngredientSaturation(Material material) {
        if (material == null) return 0;
        return INGREDIENT_SATURATION.getOrDefault(material, 0);
    }
}