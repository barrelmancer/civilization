package org.barrelmancer.civilization.util;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.constants.WorldConstants;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetAgeCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(Component.text("Usage: /setage <player> <age>")
                    .color(UIConstants.INFORMATION_COLOR));
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!")
                    .color(UIConstants.WARNING_COLOR));
            return true;
        }

        WorldConstants.AGE age;
        try {
            age = WorldConstants.AGE.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Invalid age! Options: STONE_AGE, BRONZE_AGE, IRON_AGE")
                    .color(UIConstants.WARNING_COLOR));
            return true;
        }

        PlayerMemoryUtility.getSavablePlayerMemory(target).setAge(age.getAge());

        sender.sendMessage("Set " + target.getName() + " to " + age.getDisplayName());
        target.sendMessage(Component.text(
                "Your age has been set to " + age.getDisplayName() + "!")
                .color(UIConstants.NOTIFICATION_COLOR));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> players = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                    players.add(p.getName());
                }
            }
            return players;
        } else if (args.length == 2) {
            List<String> ages = new ArrayList<>();
            for (WorldConstants.AGE age : WorldConstants.AGE.values()) {
                if (age.name().toLowerCase().startsWith(args[1].toLowerCase())) {
                    ages.add(age.name());
                }
            }
            return ages;
        }
        return List.of();
    }
}