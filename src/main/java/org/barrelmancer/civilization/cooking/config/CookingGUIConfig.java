package org.barrelmancer.civilization.cooking.config;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;

import java.util.List;

public class CookingGUIConfig {
    private final int rows;
    private final Component title;
    private final List<Integer> inputSlots;
    private final int outputSlot;
    private final Material decorativeMaterial;
    private final int decorativeModelData;

    public CookingGUIConfig(Builder builder) {
        this.rows = builder.rows;
        this.title = builder.title;
        this.inputSlots = List.copyOf(builder.inputSlots);
        this.outputSlot = builder.outputSlot;
        this.decorativeMaterial = builder.decorativeMaterial;
        this.decorativeModelData = builder.decorativeModelData;
    }
    public int getRows() {
        return rows;
    }

    public Component getTitle() {
        return title;
    }

    public List<Integer> getInputSlots() {
        return inputSlots;
    }

    public int getOutputSlot() {
        return outputSlot;
    }

    public Material getDecorativeMaterial() {
        return decorativeMaterial;
    }

    public int getDecorativeModelData() {
        return decorativeModelData;
    }


    public static class Builder {
        private int rows = 6;
        private Component title = Component.text("Cooking");
        private List<Integer> inputSlots = List.of(1);
        private int outputSlot = 22;
        private Material decorativeMaterial = Material.GRAY_STAINED_GLASS_PANE;
        private int decorativeModelData = 1000;

        public Builder rows(int rows) {
            this.rows = rows;
            return this;
        }

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder inputSlots(List<Integer> slots) {
            this.inputSlots = slots;
            return this;
        }

        public Builder outputSlot(int slot) {
            this.outputSlot = slot;
            return this;
        }

        public Builder decorativeMaterial(Material material) {
            this.decorativeMaterial = material;
            return this;
        }

        public Builder decorativeModelData(int data) {
            this.decorativeModelData = data;
            return this;
        }
        public CookingGUIConfig build() {
            return new CookingGUIConfig(this);
        }
    }
}