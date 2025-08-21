package org.barrelmancer.civilization.events.cooking;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.cooking.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class CookingEvents implements Listener {

    @EventHandler
    private void onPlayerRightClickCookingBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        CookingSystemFactory factory = CookingSystemFactory.getInstance();
        CookingSystemType systemType = factory.getCookingSystemType(block.getType());

        if (systemType == null) return;

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (player.isSneaking() && itemInHand != null && itemInHand.getType() != Material.AIR) {
            return;
        }

        event.setCancelled(true);

        if (!validateCookingConditions(block, player, systemType)) {
            return;
        }

        CookingManager<?> manager = factory.getManager(systemType);
        if (manager != null) {
            CookingMemory memory = manager.getCookingMemory(block.getLocation());
            if (memory.isCooking()) {
                long remainingSeconds = memory.getRemainingCookTime() / 1000;
                player.sendMessage(Component.text("Cooking in progress... " + remainingSeconds + " seconds remaining.")
                        .color(UIConstants.INFORMATION_COLOR));
                return;
            }
        }

        CookingGUI gui = factory.createGUI(block.getType(), block.getLocation());
        if (gui != null) {
            player.openInventory(gui.getInventory());
        }
    }

    @EventHandler
    private void onCookingBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        CookingSystemFactory factory = CookingSystemFactory.getInstance();
        CookingSystemType systemType = factory.getCookingSystemType(block.getType());

        if (systemType == null) return;

        CookingManager<?> manager = factory.getManager(systemType);
        if (manager != null) {
            CookingMemory memory = manager.getCookingMemory(block.getLocation());
            if (memory.isCooking()) {
                Player player = event.getPlayer();
                player.sendMessage(Component.text("Cannot break block while cooking is in progress!")
                        .color(UIConstants.WARNING_COLOR));
                event.setCancelled(true);
                return;
            }

            manager.removeCookingData(block.getLocation());
        }
    }

    private boolean validateCookingConditions(Block block, Player player, CookingSystemType systemType) {
        return switch (systemType) {
            case CAMPFIRE -> CampfireValidation.validateConditions(block, player);
            case CAULDRON -> CauldronValidation.validateConditions(block, player);
        };
    }

    private static class CampfireValidation {
        static boolean validateConditions(Block block, Player player) {
            if (block.getBlockData() instanceof org.bukkit.block.data.type.Campfire campfire) {
                if (!campfire.isLit()) {
                    player.sendMessage(Component.text("Campfire must be lit to cook!")
                            .color(UIConstants.WARNING_COLOR));
                    return false;
                }
            }
            return true;
        }
    }

    private static class CauldronValidation {
        static boolean validateConditions(Block block, Player player) {
            Block below = block.getWorld().getBlockAt(block.getX(), block.getY() - 1, block.getZ());

            boolean isFire = below.getType() == Material.FIRE;
            boolean isLitCampfire = false;

            if (below.getBlockData() instanceof org.bukkit.block.data.type.Campfire campfire) {
                isLitCampfire = campfire.isLit();
            }

            if (!(isFire || isLitCampfire)) {
                player.sendMessage(
                        Component.text("Light the campfire or place fire below to boil water!")
                                .color(UIConstants.WARNING_COLOR));
                return false;
            }
            return true;
        }
    }
}