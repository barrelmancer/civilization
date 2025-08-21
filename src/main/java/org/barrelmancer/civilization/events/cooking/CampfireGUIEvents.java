package org.barrelmancer.civilization.events.cooking;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.campfire.CampfireGUI;
import org.barrelmancer.civilization.campfire.CampfireManager;
import org.barrelmancer.civilization.constants.CampfireConstants;
import org.barrelmancer.civilization.constants.CauldronConstants;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.cooking.CookingManager;
import org.barrelmancer.civilization.cooking.CookingSystemType;
import org.barrelmancer.civilization.memory.CampfireMemory;
import org.barrelmancer.civilization.util.GUIEventsUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CampfireGUIEvents extends BaseCookingGUIEvents<CampfireMemory, CampfireGUI> implements Listener {

    @Override
    protected Class<CampfireGUI> getGUIClass() {
        return CampfireGUI.class;
    }

    @Override
    protected CookingManager<CampfireMemory> getManager() {
        return CampfireManager.getInstance();
    }

    @Override
    protected CookingSystemType getSystemType() {
        return CookingSystemType.CAMPFIRE;
    }

    @Override
    protected Material getOutputItem() {
        return CampfireConstants.OUTPUT_ITEM;
    }

    @Override
    protected int getOutputSlot() {
        return CampfireConstants.OUTPUT_SLOT;
    }

    @Override
    protected boolean isIngredientSlot(int slot) {
        return CampfireConstants.INGREDIENT_SLOTS.contains(slot);
    }

    @Override
    protected boolean isInputSlot(int slot) {
        return CampfireConstants.INGREDIENT_SLOTS.contains(slot) || slot == CampfireConstants.BOWL_SLOT;
    }

    @Override
    protected String getCollectionMessage() {
        return "You collected the stew.";
    }

    @Override
    protected String getCookingMessage() {
        return "Cooking in progress...";
    }

    @Override
    protected String getStartCookingMessage() {
        return "Started cooking stew!";
    }

    @Override
    protected Sound getStartCookingSound() {
        return Sound.BLOCK_CAMPFIRE_CRACKLE;
    }

    @Override
    protected void handleSpecificSlotLogic(CampfireMemory memory, ItemStack[] contents) {
        ItemStack bowl = contents[CampfireConstants.BOWL_SLOT];
        memory.setBowl((bowl != null && bowl.getType() != Material.AIR) ? bowl : null);

        for (int slot : CampfireConstants.INGREDIENT_SLOTS) {
            ItemStack it = contents[slot];
            memory.setIngredient(slot, (it != null && it.getType() != Material.AIR) ? it : null);
        }
    }

    @Override
    protected void clearConsumedIngredients(CampfireMemory memory) {
        for (int slot : CampfireConstants.INGREDIENT_SLOTS) {
            memory.setSlotItem(slot, new ItemStack(Material.AIR));
        }
        memory.setSlotItem(CampfireConstants.BOWL_SLOT, new ItemStack(Material.AIR));
        memory.setBowl(null);
        memory.getIngredients().clear();
    }

    @Override
    protected void sendSpecificWarningMessages(Player player, CampfireMemory memory, ItemStack[] contents) {
        boolean hasBowl = memory.getBowl() != null && memory.getBowl().getType() != Material.AIR;
        int ingredientCount = memory.getValidInputCount();

        if (ingredientCount == 0 && !hasBowl) {
            player.sendMessage(Component.text("Need at least 1 ingredient and a bowl to start cooking!")
                    .color(UIConstants.WARNING_COLOR));
        } else if (ingredientCount == 0) {
            player.sendMessage(Component.text("Need at least 1 ingredient to start cooking!")
                    .color(UIConstants.WARNING_COLOR));
        } else if (!hasBowl) {
            player.sendMessage(Component.text("Need a bowl to start cooking!")
                    .color(UIConstants.WARNING_COLOR));
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !(inventory.getHolder(false) instanceof CampfireGUI GUI))
            return;
        GUIEventsUtils.isClickedOnDecoration(event, UIConstants.DECORATIVE);
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();
        if (!(inventory.getHolder(false) instanceof CampfireGUI GUI))
            return;

        CampfireMemory memory = CampfireManager.getInstance().getCookingMemory(GUI.getLocation());
        ItemStack[] inventoryContent = inventory.getContents();

        saveSlotItems(memory, inventoryContent);

        Location location = GUI.getLocation();

        ItemStack outputSlotItem = inventoryContent[CampfireConstants.OUTPUT_SLOT];
        if (outputSlotItem != null && outputSlotItem.getType() != Material.AIR) {
            memory.setOutput(outputSlotItem);
            player.sendMessage(Component.text("Remove item from the output slot!").color(UIConstants.WARNING_COLOR));
        }
        boolean outputWasTaken = (outputSlotItem == null ||
                outputSlotItem.getType() == Material.AIR);

        if (memory.getOutput() != null && memory.getOutput().getType() != Material.AIR && outputWasTaken) {
            memory.setOutput(null);
            memory.setCookingPlayerUUID(null);
            player.sendMessage(
                    Component.text("You collected your campfire stew!")
                            .color(UIConstants.NOTIFICATION_COLOR));
        }

        CampfireManager.getInstance().saveCookingMemory(memory, location);

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
            int ingredientCount = memory.getValidInputCount();
            boolean hasBowl = memory.getBowl() != null && memory.getBowl().getType() != Material.AIR;
            if (inventoryContent[CampfireConstants.OUTPUT_SLOT].getType() != Material.AIR) {
                player.sendMessage(
                        Component.text("Remove item from output slot!")
                                .color(UIConstants.WARNING_COLOR)
                );
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