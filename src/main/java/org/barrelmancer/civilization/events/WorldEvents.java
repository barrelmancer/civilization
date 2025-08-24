package org.barrelmancer.civilization.events;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.constants.WorldConstants;
import org.barrelmancer.civilization.item.ItemBuilder;
import org.barrelmancer.civilization.utility.PlayerMemoryUtility;
import org.barrelmancer.civilization.utility.UIUtility;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;
import org.bukkit.util.RayTraceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorldEvents implements Listener {
    private static final Logger log = LoggerFactory.getLogger(WorldEvents.class);

    @EventHandler
    private void playerPickWater(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.GLASS_BOTTLE || !event.getAction().isRightClick())
            return;

        Player player = event.getPlayer();

        RayTraceResult result = player.getWorld().rayTraceBlocks(player.getEyeLocation(),
                player.getEyeLocation().getDirection(), player.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE).getValue(),
                FluidCollisionMode.SOURCE_ONLY);
        if (result == null || result.getHitBlock() == null || result.getHitBlock().getType() != Material.WATER)
            return;

        event.setUseItemInHand(Event.Result.DENY);

        if (player.getGameMode() != GameMode.CREATIVE)
            item.subtract();
        ItemStack waterBottle = UIUtility.createDirtyWaterPotion();
        player.getInventory().addItem(waterBottle).values().stream()
                .forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
    }

    @EventHandler
    private void onMiningOre(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Block block = event.getBlock();
        if (!block.getType().toString().contains("ORE") &&
                !block.getType().toString().contains("RAW")) {
            return;
        }
        WorldConstants.AGE playerAge = WorldConstants.AGE.fromInt(
                PlayerMemoryUtility.getSavablePlayerMemory(player).getAge()
        );

        if (!playerAge.getAvailableOres().contains(block.getType())) {
            event.setCancelled(true);
            player.sendMessage(Component.text(
                            "You cannot mine " + UIUtility.toLowerCaseWithSpaces(block.getType()) + " in the " + playerAge.getDisplayName() + "!")
                    .color(UIConstants.WARNING_COLOR));
        }
    }

}
