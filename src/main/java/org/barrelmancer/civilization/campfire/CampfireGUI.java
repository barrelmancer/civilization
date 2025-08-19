package org.barrelmancer.civilization.campfire;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.Civilization;
import org.barrelmancer.civilization.constants.CampfireConstants;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.memory.CampfireMemory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class CampfireGUI implements InventoryHolder {
    private final Location location;
    private final Inventory inventory;

    public CampfireGUI(Civilization civilization, Location location) {
        this.inventory = civilization.getServer().createInventory(this,
                27, Component.text(CampfireConstants.GUI_TITLE).color(TextColor.color(UIConstants.TITLE_COLOR)));
        this.location = location;

        CampfireMemory memory = CampfireManager.getInstance().getCampfireData(location);

        for (int i = 0; i < CampfireConstants.ROWS * 9; i++) {
            if (CampfireConstants.INGREDIENT_SLOTS.contains(i)) {
                ItemStack existingItem = memory.getAllSlotItems().get(i);
                this.inventory.setItem(i, existingItem != null ? existingItem : ItemStack.of(Material.AIR));
            } else if (i == CampfireConstants.BOWL_SLOT) {
                ItemStack existingBowl = memory.getAllSlotItems().get(i);
                this.inventory.setItem(i, existingBowl != null ? existingBowl : ItemStack.of(Material.AIR));
            } else if (i == CampfireConstants.OUTPUT_SLOT) {
                ItemStack existingOutput = memory.getOutput();
                if (existingOutput != null && existingOutput.getType() != Material.AIR) {
                    this.inventory.setItem(i, existingOutput);
                } else {
                    this.inventory.setItem(i, createOutputPlaceholder(memory));
                }
            } else {
                ItemStack item = ItemStack.of(CampfireConstants.DECORATIVE);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.displayName(Component.text(""));
                    item.setItemMeta(meta);
                }
                this.inventory.setItem(i, item);
            }
        }
    }

    public Location getLocation() {
        return location;
    }

    private ItemStack createOutputPlaceholder(CampfireMemory memory) {
        ItemStack placeholder = new ItemStack(CampfireConstants.DISH_DECORATIVE);
        ItemMeta meta = placeholder.getItemMeta();

        if (meta != null) {
            if (memory.isCooking()) {
                long remainingSeconds = memory.getRemainingCookTime() / 1000;
                meta.displayName(
                        Component.text("Cooking... " + remainingSeconds + "s remaining")
                                .color(UIConstants.INFORMATION_COLOR));
                meta.lore(List.of(
                        Component.text("Your stew is being prepared...")
                                .color(UIConstants.INFORMATION_COLOR)));
            } else if (memory.canStartCooking()) {
                meta.displayName(
                        Component.text("Ready to Cook"));
                meta.lore(List.of(
                        Component.text("Close the GUI to start cooking")
                                .color(UIConstants.INFORMATION_COLOR)));
            } else {
                meta.displayName(
                        Component.text("Need Ingredients")
                                .style(Style.style(UIConstants.INFORMATION_COLOR, TextDecoration.ITALIC.withState(false)))
                );

                meta.lore(List.of(
                        Component.text("Add ingredients and a bowl")
                                .style(Style.style(UIConstants.SUB_INFORMATION_COLOR, TextDecoration.ITALIC.withState(false)))
                ));
            }
            placeholder.setItemMeta(meta);
        }

        return placeholder;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}