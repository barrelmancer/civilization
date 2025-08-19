package org.barrelmancer.civilization.events;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.campfire.CampfireGUI;
import org.barrelmancer.civilization.campfire.CampfireManager;
import org.barrelmancer.civilization.constants.CampfireConstants;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.memory.CampfireMemory;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CampfireGUIEvents implements Listener {
    private static final Logger log = LoggerFactory.getLogger(CampfireGUIEvents.class);

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !(inventory.getHolder(false) instanceof CampfireGUI GUI))
            return;

        isClickedOnDecoration(event);

        if (event.getSlot() == CampfireConstants.OUTPUT_SLOT) {
            if (event.getCurrentItem() != null &&
                    event.getCurrentItem().getType() == CampfireConstants.DISH_ITEM) {
            } else {
                event.setCancelled(true);
            }
        }
    }

    public static void isClickedOnDecoration(InventoryClickEvent event) {
        if (event.getCurrentItem() != null &&
                (event.getCurrentItem().getType() == CampfireConstants.DISH_DECORATIVE ||
                        event.getCurrentItem().getType() == CampfireConstants.DECORATIVE)) {
            Player player = (Player) event.getWhoClicked();
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1.0f);
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();
        if (!(inventory.getHolder(false) instanceof CampfireGUI GUI))
            return;

        CampfireMemory memory = CampfireManager.getInstance().getCampfireData(GUI.getLocation());

        log.info("{} ;;; {}", inventory.getStorageContents(), inventory.getContents());
        ItemStack[] inventoryContent = inventory.getContents();

        for (int i = 0; i < CampfireConstants.ROWS * 9; i++) {
            if (i == CampfireConstants.OUTPUT_SLOT) {
                continue;
            }

            if (CampfireConstants.INGREDIENT_SLOTS.contains(i) || i == CampfireConstants.BOWL_SLOT) {
                ItemStack slotItem = inventoryContent[i];
                memory.setSlotItem(i, slotItem);
            } else {
                memory.setSlotItem(i, inventoryContent[i]);
            }
        }

        Location location = GUI.getLocation();

        ItemStack outputSlotItem = inventoryContent[CampfireConstants.OUTPUT_SLOT];
        boolean outputWasTaken = (outputSlotItem == null ||
                outputSlotItem.getType() == Material.AIR ||
                outputSlotItem.getType() == CampfireConstants.DISH_DECORATIVE);

        if (memory.getOutput() != null && memory.getOutput().getType() != Material.AIR && outputWasTaken) {
            memory.setOutput(null);
            memory.setCookingPlayerUUID(null);
            player.sendMessage(
                    Component.text("You collected your campfire stew!")
                            .color(UIConstants.NOTIFICATION_COLOR));
        }

        CampfireManager.getInstance().saveCampfireData(memory, location);

        if (memory.isCooking()) {
            long seconds = memory.getRemainingCookTime() / 1000;
            player.sendMessage(
                    Component.text("Cooking in progress... " + seconds + " seconds remaining.")
                            .color(UIConstants.INFORMATION_COLOR));
        } else if (memory.canStartCooking()) {
            memory.setCookingPlayerUUID(player.getUniqueId());
            CampfireManager.getInstance().startCooking(memory, location, player.getUniqueId());
            player.sendMessage(
                    Component.text("Cooking started! Wait " + (CampfireConstants.COOK_TICKS / 20) + " seconds.")
                            .color(UIConstants.NOTIFICATION_COLOR));
            player.playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 1.0f, 1.0f);
        } else {
            int ingredientCount = memory.getValidIngredientCount();
            boolean hasBowl = memory.getBowl() != null && memory.getBowl().getType() != Material.AIR;
            if (inventoryContent[CampfireConstants.OUTPUT_SLOT].getType() == Material.AIR) {
                return;
            }
            if (ingredientCount == 0 && !hasBowl) {
                player.sendMessage(
                        Component.text("Need at least 1 ingredient and a bowl to start cooking!")
                                .color(UIConstants.WARNING_COLOR));
            } else if (ingredientCount == 0) {
                player.sendMessage(
                        Component.text("Need at least 1 ingredient to start cooking!")
                                .color(UIConstants.WARNING_COLOR));
            } else if (!hasBowl) {
                player.sendMessage(
                        Component.text("Need a bowl to start cooking!")
                                .color(UIConstants.WARNING_COLOR));
            }
        }
    }
}