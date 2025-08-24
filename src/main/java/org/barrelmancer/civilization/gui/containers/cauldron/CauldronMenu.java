package org.barrelmancer.civilization.gui.containers.cauldron;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.configuration.ServerConfiguration;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.gui.Menu;
import org.barrelmancer.civilization.memory.CauldronMemory;
import org.barrelmancer.civilization.utility.GUIEventsUtility;
import org.barrelmancer.civilization.utility.ItemUtility;
import org.barrelmancer.civilization.utility.MaterialUtility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class CauldronMenu extends Menu {
    CauldronMemory memory;

    public CauldronMenu(Location location) {
        this.location = location;
        this.memory = CauldronManager.getInstance().getCauldronData(location);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        GUIEventsUtility.isClickedOnDecoration(e, MaterialUtility.getMaterial("civilization", "ui-materials.empty-item"));

        if (e.isCancelled()) {
            return;
        }

        List<Integer> ingredientSlots = ServerConfiguration.getIntList("cauldron", "cauldron.ingredient-slots");
        int outputSlot = ServerConfiguration.getInt("cauldron", "cauldron.output-slot");
        int clickedSlot = e.getSlot();

        if (e.getClickedInventory() != null && e.getClickedInventory().equals(inventory)) {

            if (clickedSlot == outputSlot) {
                if (e.getClick().isShiftClick() || e.getCursor() != null && e.getCursor().getType() != Material.AIR) {
                    if (memory.getOutput() == null || memory.getOutput().getType() == Material.AIR) {
                        e.setCancelled(true);
                    }
                }
                return;
            }

            if (ingredientSlots.contains(clickedSlot)) {
                ItemStack cursor = e.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    if (cursor.getType() != Material.POTION) {
                        e.setCancelled(true);
                    }
                }
                return;
            }
            e.setCancelled(true);
            return;
        }

        if (e.isShiftClick() && e.getClickedInventory() != inventory) {
            ItemStack item = e.getCurrentItem();
            if (item == null || item.getType() == Material.AIR) {
                return;
            }

            boolean isPotion = item.getType() == Material.POTION;

            if (!isPotion) {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        GUIEventsUtility.isClickedOnDecoration(e, MaterialUtility.getMaterial("civilization", "ui-materials.empty-item"));

        if (e.isCancelled()) {
            return;
        }

        List<Integer> ingredientSlots = ServerConfiguration.getIntList("cauldron", "cauldron.ingredient-slots");
        int outputSlot = ServerConfiguration.getInt("cauldron", "cauldron.output-slot");

        for (Integer rawSlot : e.getRawSlots()) {
            if (rawSlot < inventory.getSize()) {
                if (rawSlot == outputSlot) {
                    e.setCancelled(true);
                    return;
                }

                if (ingredientSlots.contains(rawSlot)) {
                    ItemStack draggedItem = e.getOldCursor();
                    if (draggedItem != null && draggedItem.getType() != Material.AIR) {
                        if (draggedItem.getType() != Material.POTION) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getMenuTitle() {
        return ServerConfiguration.getString("cauldron", "cauldron.title");
    }

    @Override
    public int getSize() {
        return ServerConfiguration.getInt("cauldron", "cauldron.size");
    }
    @Override
    public void handleMenu(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        List<Integer> ingredientSlots = ServerConfiguration.getIntList("cauldron", "cauldron.ingredient-slots");
        int outputSlot = ServerConfiguration.getInt("cauldron", "cauldron.output-slot");
        ItemStack[] inventoryContent = inventory.getContents();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == outputSlot) {
                continue;
            }

            if (ingredientSlots.contains(i)) {
                ItemStack slotItem = inventoryContent[i];
                memory.setSlotItem(i, slotItem);
            } else {
                memory.setSlotItem(i, inventoryContent[i]);
            }
        }

        Location location = this.getLocation();

        ItemStack outputSlotItem = inventoryContent[outputSlot];
        boolean outputWasTaken = (outputSlotItem == null ||
                outputSlotItem.getType() == Material.AIR ||
                MaterialUtility.isItemType(outputSlotItem.getType(), "civilization", "ui-materials.empty-item")
        );

        if (memory.getOutput() != null && memory.getOutput().getType() != Material.AIR && outputWasTaken) {
            memory.setOutput(null);
            memory.setCookingPlayerUUID(null);
            player.sendMessage(
                    Component.text(ServerConfiguration.getString("cauldron", "messages.potion-collection"))
                            .color(UIConstants.NOTIFICATION_COLOR));
        }

        CauldronManager.getInstance().saveCauldronData(memory, location);

        if (memory.isCooking()) {
            Integer seconds = Math.toIntExact(memory.getRemainingCookTime() / 1000);
            player.sendMessage(
                    Component.text(ServerConfiguration.format(ServerConfiguration.getString(
                                            "cauldron", "messages.cooking-in-progress"),
                                    Map.of("seconds", seconds)))
                            .color(UIConstants.INFORMATION_COLOR));
        } else if (memory.canStartCooking()) {
            memory.setCookingPlayerUUID(player.getUniqueId());
            CauldronManager.getInstance().startCooking(memory, location, player.getUniqueId());
            player.sendMessage(
                    Component.text(ServerConfiguration.format(ServerConfiguration.getString(
                                            "cauldron", "messages.cooking-started"),
                                    Map.of("seconds", ServerConfiguration.getInt("cauldron", "cauldron.cook-ticks") / 20)))
                            .color(UIConstants.NOTIFICATION_COLOR));
            player.playSound(player.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1.0f, 1.0f);
        } else {
            boolean hasOutputInSlot = (outputSlotItem != null &&
                    outputSlotItem.getType() != Material.AIR &&
                    !MaterialUtility.isItemType(outputSlotItem.getType(), "civilization", "ui-materials.empty-item"));

            if (!hasOutputInSlot) {
                if (outputSlotItem == null || outputSlotItem.getType() == Material.AIR) {
                    player.sendMessage(
                            Component.text(ServerConfiguration.getString("cauldron", "messages.potion-required"))
                                    .color(UIConstants.WARNING_COLOR));
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        List<Integer> ingredientSlots = ServerConfiguration.getIntList("cauldron", "cauldron.ingredient-slots");
        int outputSlot = ServerConfiguration.getInt("cauldron", "cauldron.output-slot");
        int size = getSize();

        ItemStack emptyItem = ItemUtility.createEmptyItem();

        setIngredientSlots(ingredientSlots);
        setOutputSlot(outputSlot);

        List<Integer> decorativeSlots = IntStream.range(0, size)
                .boxed()
                .filter(slot -> !ingredientSlots.contains(slot) && slot != outputSlot)
                .toList();

        for (int slot : decorativeSlots) {
            inventory.setItem(slot, emptyItem);
        }
    }

    private void setIngredientSlots(List<Integer> slots) {
        for (int slot : slots) {
            ItemStack existingItem = memory.getAllSlotItems().get(slot);
            inventory.setItem(slot, existingItem != null ? existingItem : ItemStack.of(Material.AIR));
        }
    }

    private void setOutputSlot(int slot) {
        ItemStack existingOutput = memory.getOutput();
        if (existingOutput != null && existingOutput.getType() != Material.AIR) {
            inventory.setItem(slot, existingOutput);
        } else {
            inventory.setItem(slot, ItemUtility.createEmptyItem());
        }
    }
}