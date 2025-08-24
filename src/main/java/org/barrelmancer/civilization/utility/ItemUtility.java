package org.barrelmancer.civilization.utility;

import org.barrelmancer.civilization.configuration.ServerConfiguration;
import org.barrelmancer.civilization.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemUtility {
    public static boolean isEmpty(ItemStack i){
        return i == null || i.getType().isAir() || i.getAmount() <= 0;
    }
    public static ItemMeta getItemMeta(ItemStack i){
        return (isEmpty(i)) ? null : i.getItemMeta();
    }
    public static ItemStack createEmptyItem() {
        int customModelData = ServerConfiguration.getInt("civilization", "ui-materials.empty-item-custom-model-data");
        Material material = MaterialUtility.getMaterial("civilization", "ui-materials.empty-item");
        return ItemBuilder.createDecorativeItem(material, customModelData).build();
    }
}
