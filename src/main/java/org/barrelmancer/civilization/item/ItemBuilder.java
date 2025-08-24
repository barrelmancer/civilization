package org.barrelmancer.civilization.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.utility.ItemUtility;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.FoodComponent;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    private ItemStack item;
    private ItemMeta itemMeta;
    private String itemName;
    private List<Component> itemLore;

    public ItemBuilder(Material material) {
        this.item = ItemStack.of(material);
        this.itemMeta = ItemUtility.getItemMeta(item);
        this.itemLore = new ArrayList<>();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.itemMeta = ItemUtility.getItemMeta(item);
        this.itemLore = this.itemMeta.hasLore() ? new ArrayList<>(this.itemMeta.lore()) : new ArrayList<>();
        this.itemName = this.itemMeta.hasDisplayName() ? this.itemMeta.displayName().toString() : null;
    }

    public ItemBuilder(ItemStack item, ItemMeta meta) {
        this.item = item.clone();
        this.itemMeta = meta.clone();
        this.itemLore = this.itemMeta.hasLore() ? new ArrayList<>(this.itemMeta.lore()) : new ArrayList<>();
        this.itemName = this.itemMeta.hasDisplayName() ? this.itemMeta.displayName().toString() : null;
    }

    public ItemBuilder setAmount(int amount) {
        this.item.setAmount(amount);
        return this;
    }

    public ItemBuilder setCustomModelData(int customModelData) {
        this.itemMeta.setCustomModelData(customModelData);
        return this;
    }

    public ItemBuilder setDisplayName(String name) {
        this.itemName = name;
        return this;
    }

    public ItemBuilder setDisplayName(Component name) {
        this.itemMeta.displayName(name);
        return this;
    }

    public ItemBuilder setDisplayName(String name, TextColor color) {
        Component displayName = Component.text(name)
                .style(Style.style(color, TextDecoration.ITALIC.withState(false)));
        this.itemMeta.displayName(displayName);
        return this;
    }

    public ItemBuilder setDisplayName(String name, TextColor color, TextDecoration... decorations) {
        Style.Builder styleBuilder = Style.style(color).toBuilder();

        for (TextDecoration decoration : decorations) {
            styleBuilder.decoration(decoration, TextDecoration.State.TRUE);
        }

        if (Arrays.stream(decorations).noneMatch(d -> d == TextDecoration.ITALIC)) {
            styleBuilder.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE);
        }

        Component displayName = Component.text(name).style(styleBuilder.build());
        this.itemMeta.displayName(displayName);
        return this;
    }

    public ItemBuilder addLore(String line) {
        this.itemLore.add(Component.text(line));
        return this;
    }

    public ItemBuilder addLore(Component line) {
        this.itemLore.add(line);
        return this;
    }

    public ItemBuilder addLore(String line, TextColor color) {
        Component loreLine = Component.text(line)
                .style(Style.style(color, TextDecoration.ITALIC.withState(false)));
        this.itemLore.add(loreLine);
        return this;
    }

    public ItemBuilder addLore(List<Component> lines) {
        this.itemLore.addAll(lines);
        return this;
    }

    public ItemBuilder setLore(List<Component> lore) {
        this.itemLore = new ArrayList<>(lore);
        return this;
    }

    public ItemBuilder clearLore() {
        this.itemLore.clear();
        return this;
    }

    public ItemBuilder setFood(int nutrition, float saturation) {
        if (this.itemMeta instanceof ItemMeta meta) {
            FoodComponent food = meta.getFood();
            if (food == null) {
                food = this.itemMeta.getFood();
            }
            food.setNutrition(nutrition);
            food.setSaturation(saturation);
            meta.setFood(food);
            this.itemMeta = meta;
        }
        return this;
    }

    public ItemBuilder setFood(int nutrition) {
        return setFood(nutrition, (float) nutrition / 2);
    }

    public ItemBuilder createPotion(PotionType potionType, String name, TextColor nameColor, TextColor potionColor) {
        if (this.item.getType() != Material.POTION) {
            throw new IllegalStateException("Item must be a potion to use potion methods");
        }

        if (this.itemMeta instanceof PotionMeta potionMeta) {
            potionMeta.displayName(Component.text(name)
                    .style(Style.style(nameColor, TextDecoration.ITALIC.withState(false))));

            Color color = Color.fromRGB(potionColor.red(), potionColor.green(), potionColor.blue());
            potionMeta.setColor(color);

            potionMeta.setBasePotionType(potionType);

            potionMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

            this.itemMeta = potionMeta;
        }
        return this;
    }
    public ItemBuilder addItemFlags(ItemFlag... flags) {
        this.itemMeta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder setHideTooltip(boolean hide) {
        this.itemMeta.setHideTooltip(hide);
        return this;
    }

    public ItemStack build() {
        if (!itemLore.isEmpty()) {
            this.itemMeta.lore(itemLore);
        }

        this.item.setItemMeta(this.itemMeta);
        return this.item;
    }

    public List<Component> getItemLore() {
        return itemLore;
    }

    public String getItemName() {
        return itemName;
    }

    public ItemMeta getItemMeta() {
        return itemMeta;
    }

    public ItemStack getItem() {
        return item;
    }

    public static ItemBuilder create(Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder from(ItemStack item) {
        return new ItemBuilder(item);
    }

    public static ItemBuilder createFood(Material material, int nutrition, float saturation, String name, TextColor nameColor) {
        return new ItemBuilder(material)
                .setDisplayName(name, nameColor)
                .setFood(nutrition, saturation);
    }

    public static ItemBuilder createCustomPotion(PotionType potionType, String name, TextColor nameColor, TextColor potionColor) {
        return new ItemBuilder(Material.POTION)
                .createPotion(potionType, name, nameColor, potionColor);
    }

    public static ItemBuilder createDecorativeItem(Material material, int customModelData) {
        return new ItemBuilder(material)
                .setCustomModelData(customModelData)
                .setHideTooltip(true);
    }
}