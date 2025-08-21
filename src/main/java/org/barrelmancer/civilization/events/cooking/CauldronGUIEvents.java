package org.barrelmancer.civilization.events.cooking;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.cauldron.CauldronGUI;
import org.barrelmancer.civilization.cauldron.CauldronManager;
import org.barrelmancer.civilization.constants.CauldronConstants;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.cooking.CookingManager;
import org.barrelmancer.civilization.cooking.CookingSystemType;
import org.barrelmancer.civilization.memory.CauldronMemory;
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

public class CauldronGUIEvents extends BaseCookingGUIEvents<CauldronMemory, CauldronGUI> implements Listener {

    @Override
    protected Class<CauldronGUI> getGUIClass() {
        return CauldronGUI.class;
    }

    @Override
    protected CookingManager<CauldronMemory> getManager() {
        return CauldronManager.getInstance();
    }

    @Override
    protected CookingSystemType getSystemType() {
        return CookingSystemType.CAULDRON;
    }

    @Override
    protected Material getOutputItem() {
        return CauldronConstants.OUTPUT_ITEM;
    }

    @Override
    protected int getOutputSlot() {
        return CauldronConstants.OUTPUT_SLOT;
    }

    @Override
    protected boolean isIngredientSlot(int slot) {
        return slot == CauldronConstants.INPUT_SLOT;
    }

    @Override
    protected boolean isInputSlot(int slot) {
        return slot == CauldronConstants.INPUT_SLOT;
    }

    @Override
    protected String getCollectionMessage() {
        return "You collected the boiling water.";
    }

    @Override
    protected String getCookingMessage() {
        return "Boiling in progress...";
    }

    @Override
    protected String getStartCookingMessage() {
        return "Started boiling water!";
    }

    @Override
    protected Sound getStartCookingSound() {
        return Sound.BLOCK_BREWING_STAND_BREW;
    }

    @Override
    protected void handleSpecificSlotLogic(CauldronMemory memory, ItemStack[] contents) {
        ItemStack potion = contents[CauldronConstants.INPUT_SLOT];
        memory.setWaterPotion((potion != null && potion.getType() != Material.AIR) ? potion : null);
    }

    @Override
    protected void clearConsumedIngredients(CauldronMemory memory) {
        memory.setSlotItem(CauldronConstants.INPUT_SLOT, new ItemStack(Material.AIR));
        memory.setWaterPotion(null);
    }

    @Override
    protected void sendSpecificWarningMessages(Player player, CauldronMemory memory, ItemStack[] contents) {
        boolean hasInputPotion = memory.getWaterPotion() != null && memory.getWaterPotion().getType() != Material.AIR;
        if (!hasInputPotion) {
            player.sendMessage(Component.text("Need water potion to start boiling!")
                    .color(UIConstants.WARNING_COLOR));
        }
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || !(inventory.getHolder(false) instanceof CauldronGUI GUI))
            return;

        GUIEventsUtils.isClickedOnDecoration(event, UIConstants.DECORATIVE);
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();
        if (!(inventory.getHolder(false) instanceof CauldronGUI GUI))
            return;

        CauldronMemory memory = CauldronManager.getInstance().getCookingMemory(GUI.getLocation());

        ItemStack[] inventoryContent = inventory.getContents();

        for (int i = 0; i < inventoryContent.length; i++) {
            memory.setSlotItem(i, inventoryContent[i]);
        }

        Location location = GUI.getLocation();

        ItemStack outputSlotItem = inventoryContent[CauldronConstants.OUTPUT_SLOT];
        if (outputSlotItem != null && outputSlotItem.getType() != Material.AIR) {
            player.sendMessage(
                    Component.text("Remove item from output slot!")
                            .color(UIConstants.WARNING_COLOR)
            );
            memory.setOutput(outputSlotItem);
        } else {
            memory.setOutput(ItemStack.of(Material.AIR));
        }
        CauldronManager.getInstance().saveCookingMemory(memory, location);

        if (memory.isCooking()) {
            long seconds = memory.getRemainingCookTime() / 1000;
            player.sendMessage(
                    Component.text("Boiling in progress... " + seconds + " seconds remaining.")
                            .color(UIConstants.INFORMATION_COLOR));
        } else if (memory.canStartCooking()) {
            memory.setCookingPlayerUUID(player.getUniqueId());
            CauldronManager.getInstance().startCooking(memory, location, player.getUniqueId());
            player.sendMessage(
                    Component.text("Boiling started! Wait " + (CauldronConstants.COOK_TICKS / 20) + " seconds.")
                            .color(UIConstants.NOTIFICATION_COLOR));
            player.playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 1.0f, 1.0f);
        } else {
            if (inventoryContent[CauldronConstants.OUTPUT_SLOT].getType() == Material.AIR) {
                return;
            }
            player.sendMessage(
                    Component.text("Need dirty water to start boiling!")
                            .color(UIConstants.WARNING_COLOR));

        }
    }
}