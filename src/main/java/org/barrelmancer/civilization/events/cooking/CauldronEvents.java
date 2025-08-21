package org.barrelmancer.civilization.events.cooking;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.cauldron.CauldronGUI;
import org.barrelmancer.civilization.cauldron.CauldronManager;
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

public class CauldronEvents implements Listener {
    private static final Logger log = LoggerFactory.getLogger(CauldronEvents.class);

    @EventHandler
    private void onPlayerRightClickCauldron(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CAULDRON) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (player.isSneaking() && itemInHand != null && itemInHand.getType() != Material.AIR) {
            event.setCancelled(false);
            return;
        }
        event.setCancelled(true);

        Block below = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());

        boolean isFire = below.getType() == Material.FIRE;

        boolean isLitCampfire = false;
        if (below.getBlockData() instanceof Campfire campfire) {
            isLitCampfire = campfire.isLit();
        }

        if (!(isFire || isLitCampfire)) {
            player.sendMessage(
                    Component.text("Light the campfire or place fire below to boil water!")
                            .color(UIConstants.WARNING_COLOR)
            );
            return;
        }
        CauldronGUI inventory = new CauldronGUI(block.getLocation());
        player.openInventory(inventory.getInventory());
    }

    @EventHandler
    private void onCauldronBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.CAULDRON) return;
        try {
            CauldronManager.getInstance().removeCookingData(block.getLocation());
        } catch (IllegalStateException e) {
            log.info("CauldronManager not initialized, cannot clean up data");
        } catch (Exception e) {
            log.info("Error cleaning up cauldron data: {}", e.getMessage());
        }
    }

}