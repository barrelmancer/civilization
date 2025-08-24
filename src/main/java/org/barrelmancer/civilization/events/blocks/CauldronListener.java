package org.barrelmancer.civilization.events.blocks;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.configuration.ServerConfiguration;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.gui.containers.cauldron.CauldronManager;
import org.barrelmancer.civilization.gui.containers.cauldron.CauldronMenu;
import org.barrelmancer.civilization.memory.CauldronMemory;
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

import java.util.Map;

public class CauldronListener implements Listener {
    private static final Logger log = LoggerFactory.getLogger(CauldronListener.class);

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
                    Component.text(ServerConfiguration.getString("cauldron", "messages.light-campfire"))
                            .color(UIConstants.WARNING_COLOR)
            );
            return;
        }
        CauldronMemory memory = CauldronManager.getInstance().getCauldronData(block.getLocation());
        if (memory.isCooking()) {
            Integer seconds = Math.toIntExact(memory.getRemainingCookTime() / 1000);
            player.sendMessage(
                    Component.text(ServerConfiguration.format(ServerConfiguration.getString(
                                            "cauldron", "messages.cooking-in-progress"),
                                    Map.of("seconds", seconds)))
                            .color(UIConstants.INFORMATION_COLOR));
            return;
        }
        CauldronMenu inventory = new CauldronMenu(block.getLocation());
        inventory.open(player, block.getLocation());
    }

    @EventHandler
    private void onCauldronBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.CAMPFIRE) return;
        try {
            CauldronManager.getInstance().removeCauldronData(block.getLocation());
        } catch (IllegalStateException e) {
            log.info("CauldronManager not initialized, cannot clean up data");
        } catch (Exception e) {
            log.info("Error cleaning up cauldron data: {}", e.getMessage());
        }
    }
}
