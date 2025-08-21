package org.barrelmancer.civilization.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.constants.UIConstants;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UIUtils {
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] words = input.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
    public static String toLowerCaseWithSpaces(Material material) {
        return material.toString().toLowerCase().replace("_", " ");
    }
    public static ItemStack createCustomWaterPotion(TextColor textColor, String name) {
        ItemStack potion = new ItemStack(Material.POTION);

        if (potion.getItemMeta() instanceof PotionMeta meta) {
            meta.displayName(
                    Component.text(name)
                            .style(Style.style(UIConstants.STEW_COLOR, TextDecoration.ITALIC.withState(false))));

            Color potionColor = Color.fromRGB(textColor.red(), textColor.green(), textColor.blue());
            meta.setColor(potionColor);

            meta.setBasePotionType(PotionType.WATER);
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

            potion.setItemMeta(meta);
        }
        return potion;
    }
    public static String buildBar(int x) {
        int tens = x / 10;
        int remainder = x % 10;

        int filled = tens + (remainder >= 5 ? 1 : 0);

        return IntStream.range(0, 10)
                .mapToObj(i -> {
                    if (i < tens) return UIConstants.THIRST_FULL;
                    if (i == tens && remainder >= 5) return UIConstants.THIRST_HALF;
                    return UIConstants.THIRST_NONE;
                })
                .collect(Collectors.joining());
    }

}
