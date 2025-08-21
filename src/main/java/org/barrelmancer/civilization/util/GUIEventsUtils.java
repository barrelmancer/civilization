package org.barrelmancer.civilization.util;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIEventsUtils {
    public static void isClickedOnDecoration(InventoryClickEvent event, Material decorative) {
        if (event.getCurrentItem() != null &&
                event.getCurrentItem().getType() == decorative) {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
            event.setCancelled(true);
        }
    }
}
