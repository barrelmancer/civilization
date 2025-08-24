package org.barrelmancer.civilization.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.barrelmancer.civilization.Civilization;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu {
    protected Inventory inventory;
    protected Location location;
    protected int size;

    public abstract String getMenuTitle();
    public abstract int getSize();
    public abstract void handleMenu(InventoryClickEvent e);
    public abstract void handleMenu(InventoryDragEvent e);
    public abstract void setMenuItems();
    public abstract void handleMenu(InventoryCloseEvent e);

    public void open(Player player, Location location) {
        this.inventory = Civilization.getCivilization().getServer().createInventory(
                null, getSize(), Component.text(getMenuTitle()).color(NamedTextColor.WHITE));
        this.location = location;

        MenuListener.setActiveMenu(player, this);

        player.openInventory(this.inventory);
        this.setMenuItems();
    }

    public Location getLocation() {
        return location;
    }

    public Inventory getInventory() {
        return inventory;
    }
}