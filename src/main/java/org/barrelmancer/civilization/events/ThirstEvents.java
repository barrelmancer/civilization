package org.barrelmancer.civilization.events;

import org.barrelmancer.civilization.Thirst;
import org.barrelmancer.civilization.constants.ThirstConstants;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class ThirstEvents implements Listener {
    @EventHandler
    private void onDrinkingWater(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (item.getType() == Material.POTION && item.getItemMeta() instanceof PotionMeta meta) {
            PotionType type = meta.getBasePotionType();
            if (type == PotionType.WATER) {
                Player p = event.getPlayer();
                Thirst.increaseThirst(p, ThirstConstants.BASE_WATER_THIRST_INCREASE_AMOUNT);
            }
        }
    }
}
