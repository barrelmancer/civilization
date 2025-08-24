package org.barrelmancer.civilization.utility;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class GUIEventsUtility {

    public static void isClickedOnDecoration(InventoryClickEvent event, Material decorative) {
        if (event.getCurrentItem() != null &&
                event.getCurrentItem().getType() == decorative &&
                event.getClickedInventory() != null &&
                event.getClickedInventory().equals(event.getView().getTopInventory())) {

            //Player player = (Player) event.getWhoClicked();
            //player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
            event.setCancelled(true);
        }
    }

    public static void isClickedOnDecoration(InventoryDragEvent event, Material decorative) {
        boolean hitDecorativeSlot = false;

        for (Integer slot : event.getRawSlots()) {
            if (slot < event.getView().getTopInventory().getSize()) {
                if (event.getView().getTopInventory().getItem(slot) != null &&
                        event.getView().getTopInventory().getItem(slot).getType() == decorative) {
                    hitDecorativeSlot = true;
                    break;
                }
            }
        }

        if (hitDecorativeSlot) {
            //Player player = (Player) event.getWhoClicked();
            //player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
            event.setCancelled(true);
        }
    }
}