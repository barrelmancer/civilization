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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Random;

public class ThirstEvents implements Listener {
    @EventHandler
    private void onDrinkingWater(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        if (item.getType() == Material.POTION && item.getItemMeta() instanceof PotionMeta meta) {
            PotionType type = meta.getBasePotionType();
            if (type == PotionType.WATER) {
                Random random = new Random();
                Player player = event.getPlayer();
                Thirst.increaseThirst(player, ThirstConstants.BASE_DIRTY_WATER_THIRST_INCREASE_AMOUNT);
                if (random.nextInt() % 3 == 0) {
                    player.addPotionEffect(
                            new PotionEffect(PotionEffectType.WITHER, 61,
                                    1, false, false, false));
                }
            } else if (type == PotionType.AWKWARD) {
                Player player = event.getPlayer();
                Thirst.increaseThirst(player, ThirstConstants.BASE_WATER_THIRST_INCREASE_AMOUNT);
            }
        }
    }
}
