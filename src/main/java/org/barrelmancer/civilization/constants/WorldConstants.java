package org.barrelmancer.civilization.constants;

import org.bukkit.Material;

import java.util.List;

public class WorldConstants {
    public enum AGE {
        STONE_AGE(
                0,
                List.of(Material.COAL_ORE, Material.IRON_ORE)
        ),
        BRONZE_AGE(
                1,
                List.of(Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE)
        ),
        IRON_AGE(
                2,
                List.of(Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE, Material.GOLD_ORE)
        );

        private final int age;
        private final List<Material> availableOres;

        AGE(int age, List<Material> availableOres) {
            this.age = age;
            this.availableOres = availableOres;
        }

        public int getAge() {
            return age;
        }

        public List<Material> getAvailableOres() {
            return availableOres;
        }

        public static AGE fromInt(int age) {
            for (AGE value : AGE.values()) {
                if (value.getAge() == age) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Invalid age: " + age);
        }
        public String getDisplayName() {
            return this.name().toLowerCase().replace("_", " ");
        }
    }
}
