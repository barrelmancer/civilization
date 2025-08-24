package org.barrelmancer.civilization.constants;

import org.bukkit.Material;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TemperatureConstants {
    public static final float BASE_TEMPERATURE = 37.0f;
    public static final float BASE_MIN_TEMPERATURE = 35.5f;
    public static final float BASE_MAX_TEMPERATURE = 39.0f;
    public static final float NIGHT_TEMPERATURE_DECREASE = -0.5f;
    public static final float CAMPFIRE_TEMPERATURE_INCREASE = 1.0f;

    public static final float DESERT_TEMPERATURE_DELTA = 1.2f;
    public static final float SNOWY_TEMPERATURE_DELTA = -1.5f;

    public static final Map<Material, Float> LEATHER_ARMOR_TEMPERATURE = Map.of(
            Material.LEATHER_HELMET, 0.3f,
            Material.LEATHER_CHESTPLATE, 0.8f,
            Material.LEATHER_LEGGINGS, 0.5f,
            Material.LEATHER_BOOTS, 0.3f
    );

    public static final List<Biome> DESERT_BIOMES = new ArrayList<>(Arrays.asList(
            Biome.DESERT, Biome.BADLANDS, Biome.WOODED_BADLANDS, Biome.ERODED_BADLANDS
    ));

    public static final List<Biome> SNOWY_BIOMES = new ArrayList<>(Arrays.asList(
            Biome.SNOWY_TAIGA, Biome.SNOWY_BEACH, Biome.SNOWY_PLAINS, Biome.SNOWY_SLOPES,
            Biome.ICE_SPIKES, Biome.TAIGA, Biome.OLD_GROWTH_PINE_TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA,
            Biome.FROZEN_OCEAN, Biome.FROZEN_PEAKS, Biome.DEEP_FROZEN_OCEAN,
            Biome.WINDSWEPT_HILLS, Biome.WINDSWEPT_GRAVELLY_HILLS, Biome.JAGGED_PEAKS
    ));
}