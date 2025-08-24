package org.barrelmancer.civilization.gui.containers.campfire;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.configuration.ServerConfiguration;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.gui.Menu;
import org.barrelmancer.civilization.memory.CampfireMemory;
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

public class CampfireMenu extends Menu {
    CampfireMemory memory;

    public CampfireMenu(Location location) {
        this.location = location;
        this.memory = CampfireManager.getInstance().getCampfireData(location);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        GUIEventsUtility.isClickedOnDecoration(e, MaterialUtility.getMaterial("civilization", "ui-materials.empty-item"));

        if (e.isCancelled()) {
            return;
        }

        List<Integer> ingredientSlots = ServerConfiguration.getIntList("campfire", "campfire.ingredient-slots");
        int bowlSlot = ServerConfiguration.getInt("campfire", "campfire.bowl-slot");
        int outputSlot = ServerConfiguration.getInt("campfire", "campfire.output-slot");
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
                    if (!MaterialUtility.isInConfigKeys("food", "food", cursor.getType())) {
                        e.setCancelled(true);
                    }
                }
                return;
            }

            if (clickedSlot == bowlSlot) {
                ItemStack cursor = e.getCursor();
                if (cursor != null && cursor.getType() != Material.AIR) {
                    if (cursor.getType() != Material.BOWL) {
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

            boolean isFood = MaterialUtility.isInConfigKeys("food", "food", item.getType());
            boolean isBowl = item.getType() == Material.BOWL;

            if (!isFood && !isBowl) {
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

        List<Integer> ingredientSlots = ServerConfiguration.getIntList("campfire", "campfire.ingredient-slots");
        int bowlSlot = ServerConfiguration.getInt("campfire", "campfire.bowl-slot");
        int outputSlot = ServerConfiguration.getInt("campfire", "campfire.output-slot");

        for (Integer rawSlot : e.getRawSlots()) {
            if (rawSlot < inventory.getSize()) {
                if (rawSlot == outputSlot) {
                    e.setCancelled(true);
                    return;
                }

                if (!ingredientSlots.contains(rawSlot) && rawSlot != bowlSlot) {
                    e.setCancelled(true);
                    return;
                }

                if (ingredientSlots.contains(rawSlot)) {
                    ItemStack draggedItem = e.getOldCursor();
                    if (draggedItem != null && draggedItem.getType() != Material.AIR) {
                        if (!MaterialUtility.isInConfigKeys("food", "food", draggedItem.getType())) {
                            e.setCancelled(true);
                            return;
                        }
                    }
                }

                if (rawSlot == bowlSlot) {
                    ItemStack draggedItem = e.getOldCursor();
                    if (draggedItem != null && draggedItem.getType() != Material.BOWL) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public String getMenuTitle() {
        return "\uF101\uF001";
    }

    @Override
    public int getSize() {
        return ServerConfiguration.getInt("campfire", "campfire.size");
    }

    @Override
    public void handleMenu(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        List<Integer> ingredientSlots = ServerConfiguration.getIntList("campfire", "campfire.ingredient-slots");
        int bowlSlot = ServerConfiguration.getInt("campfire", "campfire.bowl-slot");
        int outputSlot = ServerConfiguration.getInt("campfire", "campfire.output-slot");
        ItemStack[] inventoryContent = inventory.getContents();

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == outputSlot) {
                continue;
            }

            ItemStack slotItem = inventoryContent[i];
            if (slotItem != null && slotItem.getType() != Material.AIR && slotItem.getAmount() > 1) {
                int excess = slotItem.getAmount() - 1;
                ItemStack excessStack = slotItem.clone();
                excessStack.setAmount(excess);

                player.getInventory().addItem(excessStack).values().forEach(
                        item -> player.getWorld().dropItemNaturally(player.getLocation(), item)
                );

                slotItem.setAmount(1);
                inventory.setItem(i, slotItem);

                player.sendMessage(Component.text(ServerConfiguration.getString("campfire", "messages.items-returned"))
                        .color(UIConstants.WARNING_COLOR));
            }
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i == outputSlot) {
                continue;
            }

            if (ingredientSlots.contains(i) || i == bowlSlot) {
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
                    Component.text(ServerConfiguration.getString("campfire", "messages.stew-collection"))
                            .color(UIConstants.NOTIFICATION_COLOR));
        }

        CampfireManager.getInstance().saveCampfireData(memory, location);

        if (memory.isCooking()) {
            Integer seconds = Math.toIntExact(memory.getRemainingCookTime() / 1000);
            player.sendMessage(
                    Component.text(ServerConfiguration.format(ServerConfiguration.getString(
                                            "campfire", "messages.cooking-in-progress"),
                                    Map.of("seconds", seconds)))
                            .color(UIConstants.INFORMATION_COLOR));
        } else if (memory.canStartCooking()) {
            memory.setCookingPlayerUUID(player.getUniqueId());
            CampfireManager.getInstance().startCooking(memory, location, player.getUniqueId());
            player.sendMessage(
                    Component.text(ServerConfiguration.format(ServerConfiguration.getString(
                                            "campfire", "messages.cooking-started"),
                                    Map.of("seconds", ServerConfiguration.getInt("campfire", "campfire.cook-ticks") / 20)))
                            .color(UIConstants.NOTIFICATION_COLOR));
            player.playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 1.0f, 1.0f);
        } else {
            boolean hasOutputInSlot = (inventoryContent[outputSlot] != null &&
                    inventoryContent[outputSlot].getType() != Material.AIR &&
                    !MaterialUtility.isItemType(inventoryContent[outputSlot].getType(), "civilization", "ui-materials.empty-item"));

            if (!hasOutputInSlot) {
                int ingredientCount = memory.getValidIngredientCount();
                boolean hasBowl = memory.getBowl() != null && memory.getBowl().getType() != Material.AIR;

                if (ingredientCount == 0 && !hasBowl) {
                    player.sendMessage(
                            Component.text(ServerConfiguration.getString("campfire", "messages.ingredient-bowl-required"))
                                    .color(UIConstants.WARNING_COLOR));
                } else if (ingredientCount == 0) {
                    player.sendMessage(
                            Component.text(ServerConfiguration.getString("campfire", "messages.ingredient-required"))
                                    .color(UIConstants.WARNING_COLOR));
                } else if (!hasBowl) {
                    player.sendMessage(
                            Component.text(ServerConfiguration.getString("campfire", "messages.bowl-required"))
                                    .color(UIConstants.WARNING_COLOR));
                }
            }
        }
    }

    @Override
    public void setMenuItems() {
        List<Integer> ingredientSlots = ServerConfiguration.getIntList("campfire", "campfire.ingredient-slots");
        int bowlSlot = ServerConfiguration.getInt("campfire", "campfire.bowl-slot");
        int outputSlot = ServerConfiguration.getInt("campfire", "campfire.output-slot");
        int size = getSize();

        ItemStack emptyItem = ItemUtility.createEmptyItem();

        setIngredientSlots(ingredientSlots);
        setBowlSlot(bowlSlot);
        setOutputSlot(outputSlot);

        List<Integer> decorativeSlots = IntStream.range(0, size)
                .boxed()
                .filter(slot -> !ingredientSlots.contains(slot) && slot != bowlSlot && slot != outputSlot)
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

    private void setBowlSlot(int slot) {
        ItemStack existingBowl = memory.getAllSlotItems().get(slot);
        inventory.setItem(slot, existingBowl != null ? existingBowl : ItemStack.of(Material.AIR));
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