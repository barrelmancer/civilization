package org.barrelmancer.civilization;

import org.barrelmancer.civilization.constants.ThirstConstants;
import org.barrelmancer.civilization.memory.SavablePlayerMemory;
import org.barrelmancer.civilization.util.PlayerMemoryUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Thirst implements Runnable {
    private final static Thirst instance = new Thirst();

    private Thirst() {
    }

    @Override
    public void run() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            SavablePlayerMemory memory = PlayerMemoryUtility.getSavablePlayerMemory(p);
            if (memory.getThirst() <= 20) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ThirstConstants.BASE_THIRST_DECREASE_RATE + 1, 1, false, false, false));
            }
            decreaseThirst(p, ThirstConstants.BASE_THIRST_DECREASE_AMOUNT);
        }
    }

    public static void decreaseThirst(Player p, int value) {
        SavablePlayerMemory memory = PlayerMemoryUtility.getSavablePlayerMemory(p);
        if (memory.getThirst() <= 0) {
            return;
        } else if ((memory.getThirst() - value) < 0) {
            memory.setThirst(0);
            return;
        }
        memory.setThirst(memory.getThirst() - value);
        PlayerMemoryUtility.setSavablePlayerMemory(p, memory);
    }

    public static void increaseThirst(Player p, int value) {
        SavablePlayerMemory memory = PlayerMemoryUtility.getSavablePlayerMemory(p);
        if (memory.getThirst() >= 100) {
            return;
        } else if ((memory.getThirst() + value) > 100) {
            memory.setThirst(100);
            PlayerMemoryUtility.setSavablePlayerMemory(p, memory);
            return;
        }
        memory.setThirst(memory.getThirst() + value);
        PlayerMemoryUtility.setSavablePlayerMemory(p, memory);
    }

    public static Thirst getInstance() {
        return instance;
    }
}
