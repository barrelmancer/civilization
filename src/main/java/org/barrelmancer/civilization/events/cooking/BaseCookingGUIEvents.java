package org.barrelmancer.civilization.events.cooking;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.cooking.BaseCookingGUI;
import org.barrelmancer.civilization.cooking.CookingManager;
import org.barrelmancer.civilization.cooking.CookingMemory;
import org.barrelmancer.civilization.cooking.CookingSystemType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCookingGUIEvents<T extends CookingMemory, G extends BaseCookingGUI<T>> implements Listener {
    private static final Logger log = LoggerFactory.getLogger(BaseCookingGUIEvents.class);
    public static Material getDecorative() {
        return UIConstants.DECORATIVE;
    }

    protected abstract Class<G> getGUIClass();

    protected abstract CookingManager<T> getManager();

    protected abstract CookingSystemType getSystemType();

    protected abstract Material getOutputItem();

    protected abstract int getOutputSlot();

    protected abstract boolean isIngredientSlot(int slot);

    protected abstract boolean isInputSlot(int slot);

    protected abstract String getCollectionMessage();

    protected abstract String getCookingMessage();

    protected abstract String getStartCookingMessage();

    protected abstract Sound getStartCookingSound();

    protected abstract void handleSpecificSlotLogic(T memory, ItemStack[] inventoryContent);

    protected abstract void sendSpecificWarningMessages(Player player, T memory, ItemStack[] inventoryContent);

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (!getGUIClass().isInstance(inventory.getHolder(false))) {
            return;
        }

        @SuppressWarnings("unchecked")
        G gui = (G) inventory.getHolder(false);
        Location location = gui.getLocation();

        T memory = getManager().getCookingMemory(location);
        ItemStack[] inventoryContent = inventory.getContents();

        ItemStack outputSlotItem = inventoryContent[getOutputSlot()];
        boolean outputWasTaken = (outputSlotItem == null ||
                outputSlotItem.getType() == Material.AIR);

        if (memory.getOutput() != null && memory.getOutput().getType() != Material.AIR && outputWasTaken) {
            handleOutputCollection(memory, location, player);
            return;
        }

        if (memory.isCooking()) {
            long remainingSeconds = memory.getRemainingCookTime() / 1000;
            player.sendMessage(Component.text(getCookingMessage() + " " + remainingSeconds + " seconds remaining.")
                    .color(UIConstants.INFORMATION_COLOR));
            return;
        }

        handleInventoryClose(player, memory, location, inventoryContent);
    }

    private void handleOutputCollection(T memory, Location location, Player player) {
        memory.setOutput(null);
        memory.setCookingPlayerUUID(null);

        clearConsumedIngredients(memory);

        player.sendMessage(Component.text(getCollectionMessage())
                .color(UIConstants.NOTIFICATION_COLOR));

        getManager().saveCookingMemory(memory, location);
    }

    protected void clearConsumedIngredients(T memory) {
    }

    private void handleInventoryClose(Player player, T memory, Location location, ItemStack[] inventoryContent) {
        saveSlotItems(memory, inventoryContent);

        handleSpecificSlotLogic(memory, inventoryContent);

        getManager().saveCookingMemory(memory, location);

        handleCookingLogic(player, memory, location, inventoryContent);
    }

    public void saveSlotItems(T memory, ItemStack[] inventoryContent) {
        for (int i = 0; i < inventoryContent.length; i++) {
            if (i != getOutputSlot()) {
                ItemStack item = inventoryContent[i];
                if (item == null) {
                    item = new ItemStack(Material.AIR);
                }
                memory.setSlotItem(i, item);
            }
        }
    }

    private void handleCookingLogic(Player player, T memory, Location location, ItemStack[] inventoryContent) {
        if (memory.canStartCooking()) {
            memory.setCookingPlayerUUID(player.getUniqueId());
            getManager().startCooking(memory, location, player.getUniqueId());

            player.sendMessage(Component.text(getStartCookingMessage())
                    .color(UIConstants.NOTIFICATION_COLOR));
            player.playSound(player.getLocation(), getStartCookingSound(), 1.0f, 1.0f);
        } else {

            if (hasAnyInputItems(inventoryContent)) {
                sendSpecificWarningMessages(player, memory, inventoryContent);
            }
        }
    }

    private boolean hasAnyInputItems(ItemStack[] inventoryContent) {
        for (int i = 0; i < inventoryContent.length; i++) {
            if (isInputSlot(i)) {
                ItemStack item = inventoryContent[i];
                if (item != null && item.getType() != Material.AIR) {
                    return true;
                }
            }
        }
        return false;
    }
}