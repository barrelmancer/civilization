package org.barrelmancer.civilization.gui;

import org.bukkit.entity.Player;

public class PlayerMenuUtility {
    private final Player owner;

    public PlayerMenuUtility(Player owner) {
        this.owner = owner;
    }
    public Player getOwner() {
        return this.owner;
    }
}
