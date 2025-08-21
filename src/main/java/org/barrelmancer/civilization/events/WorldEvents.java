package org.barrelmancer.civilization.events;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.constants.WorldConstants;
import org.barrelmancer.civilization.util.PlayerMemoryUtility;
import org.barrelmancer.civilization.util.UIUtils;
import org.bukkit.Color;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
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
        ItemStack waterBottle = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) waterBottle.getItemMeta();
        meta.setBasePotionType(PotionType.WATER);
        meta.setColor(Color.fromRGB(
                UIConstants.DIRTY_WATER_COLOR.red(),
                UIConstants.DIRTY_WATER_COLOR.green(),
                UIConstants.DIRTY_WATER_COLOR.blue()
        ));
        meta.displayName(
                Component.text("Dirty Water").style(Style.style(UIConstants.DIRTY_WATER_COLOR, TextDecoration.ITALIC.withState(false)))
        );
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        waterBottle.setItemMeta(meta);
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
            log.info("CANCELING EVENT");
            event.setCancelled(true);
            player.sendMessage(Component.text(
                            "You cannot mine " + UIUtils.toLowerCaseWithSpaces(block.getType()) + " in the " + playerAge.getDisplayName() + "!")
                    .color(UIConstants.WARNING_COLOR));
        }
    }

}
