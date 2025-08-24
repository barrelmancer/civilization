package org.barrelmancer.civilization.gui;


import org.barrelmancer.civilization.utility.ItemUtility;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class MenuListener implements Listener {
    private static final Map<UUID, Menu> activeMenus = new HashMap<>();

    public static void setActiveMenu(Player player, Menu menu) {
        activeMenus.put(player.getUniqueId(), menu);
    }
    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        handleMenuEvent(e.getWhoClicked().getUniqueId(), e.getInventory(), menu -> menu.handleMenu(e));
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent e) {
        if (!ItemUtility.isEmpty(e.getCursor())) {
            handleMenuEvent(e.getWhoClicked().getUniqueId(), e.getInventory(), menu -> menu.handleMenu(e));
        }
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
        handleMenuEvent(e.getPlayer().getUniqueId(), e.getInventory(), menu -> menu.handleMenu(e));
    }

    private void handleMenuEvent(UUID playerId, Inventory inventory, Consumer<Menu> menuAction) {
        Menu activeMenu = activeMenus.get(playerId);
        if (activeMenu != null && inventory.equals(activeMenu.getInventory())) {
            menuAction.accept(activeMenu);
        }
    }
}
