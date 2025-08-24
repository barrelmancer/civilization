package org.barrelmancer.civilization.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.memory.DynamicPlayerMemory;
import org.barrelmancer.civilization.memory.SavablePlayerMemory;
import org.barrelmancer.civilization.utility.PlayerMemoryUtility;
import org.barrelmancer.civilization.utility.UIUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusBar implements Runnable {
    private final static StatusBar instance = new StatusBar();
    private static final Logger log = LoggerFactory.getLogger(StatusBar.class);

    private StatusBar() {
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            SavablePlayerMemory savablePlayerMemory = PlayerMemoryUtility.getSavablePlayerMemory(p);
            DynamicPlayerMemory dynamicPlayerMemory = PlayerMemoryUtility.getDynamicPlayerMemory(p);

            updateStatusBar(p, savablePlayerMemory.getThirst(), dynamicPlayerMemory.getTemperature());
        }
    }

    private static void updateStatusBar(Player player, int thirst, float temperature) {
        //String thirstBar = "[" + thirst + " / 100]";
        String thirstBar = UIUtility.buildBar(thirst);
        String temperatureBar = "[" + temperature + "°C]";
        Component statusBarText = Component
                .text(thirstBar)
                .style(Style.style(TextColor.color(NamedTextColor.WHITE)))
                .append(Component
                        .text(" ".repeat(48 - thirstBar.length() - temperatureBar.length())))
                .append(
                        Component
                                .text(temperatureBar)
                                .style(Style.style(TextColor.color(0xDB7827), TextDecoration.BOLD))
                );
        player.sendActionBar(statusBarText);
    }

    public static StatusBar getInstance() {
        return instance;
    }
}
