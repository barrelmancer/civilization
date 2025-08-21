package org.barrelmancer.civilization.cooking;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.barrelmancer.civilization.Civilization;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.cooking.config.CookingGUIConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class BaseCookingGUI<T extends CookingMemory> implements CookingGUI, InventoryHolder {
    protected final Location location;
    protected final Inventory inventory;
    protected final CookingGUIConfig config;
    protected final T memory;

    public BaseCookingGUI(Location location, CookingGUIConfig config, T memory) {
        this.location = location;
        this.config = config;
        this.memory = memory;
        this.inventory = Civilization.getCivilization().getServer().createInventory(
                this, config.getRows() * 9, config.getTitle().color(TextColor.color(UIConstants.TITLE_COLOR)));

        initializeGUI();
    }

    protected void initializeGUI() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (config.getInputSlots().contains(i)) {
                ItemStack existingItem = (i < memory.getAllSlotItems().size())
                        ? memory.getAllSlotItems().get(i)
                        : null;
                if (existingItem != null && existingItem.getType() != Material.AIR) {
                    inventory.setItem(i, existingItem.clone());
                } else {
                    inventory.setItem(i, new ItemStack(Material.AIR));
                }
            } else if (i == config.getOutputSlot()) {
                setupOutputSlot(i);
            } else {
                setupDecorativeSlot(i);
            }
        }
        setupCustomSlots();

    }

    protected void setupOutputSlot(int slot) {
        ItemStack existingOutput = memory.getOutput();
        if (existingOutput != null && existingOutput.getType() != Material.AIR) {
            inventory.setItem(slot, existingOutput.clone());
        } else {
            inventory.setItem(slot, new ItemStack(Material.AIR));
        }
    }

    protected void setupDecorativeSlot(int slot) {
        ItemStack decorativeItem = ItemStack.of(config.getDecorativeMaterial());
        ItemMeta decorativeItemMeta = decorativeItem.getItemMeta();
        if (decorativeItemMeta != null) {
            decorativeItemMeta.displayName(getDecorativeDisplayName());
            decorativeItemMeta.setCustomModelData(config.getDecorativeModelData());
            decorativeItemMeta.setHideTooltip(true);
            decorativeItem.setItemMeta(decorativeItemMeta);
        }
        inventory.setItem(slot, decorativeItem);
    }

    protected abstract Component getDecorativeDisplayName();

    protected abstract void setupCustomSlots();

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}