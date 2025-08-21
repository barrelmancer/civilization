package org.barrelmancer.civilization.events.cooking;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.campfire.CampfireGUI;
import org.barrelmancer.civilization.campfire.CampfireManager;
import org.barrelmancer.civilization.constants.UIConstants;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampfireEvents implements Listener {
    private static final Logger log = LoggerFactory.getLogger(CampfireEvents.class);

    @EventHandler
    private void onPlayerRightClickCampfire(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CAMPFIRE) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (player.isSneaking() && itemInHand != null && itemInHand.getType() != Material.AIR) {
            event.setCancelled(false);
            return;
        }
        event.setCancelled(true);

        Campfire campfireData = (Campfire) block.getBlockData();
        if (!campfireData.isLit()) {
            player.sendMessage(
                    Component.text("Campfire must be lit to cook!")
                            .color(UIConstants.WARNING_COLOR));
            return;
        }
        CampfireGUI inventory = new CampfireGUI(block.getLocation());
        player.openInventory(inventory.getInventory());
    }

    @EventHandler
    private void onCampfireBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.CAMPFIRE) return;
        try {
            CampfireManager.getInstance().removeCookingData(block.getLocation());
        } catch (IllegalStateException e) {
            log.info("CampfireManager not initialized, cannot clean up data");
        } catch (Exception e) {
            log.info("Error cleaning up campfire data: {}", e.getMessage());
        }
    }

}