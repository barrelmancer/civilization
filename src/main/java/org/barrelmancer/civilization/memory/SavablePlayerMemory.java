package org.barrelmancer.civilization.memory;

public class SavablePlayerMemory {
    public int getThirst() {
        return thirst;
    }

    public void setThirst(int thirst) {
        this.thirst = thirst;
    }

    private int thirst = 100;
}
