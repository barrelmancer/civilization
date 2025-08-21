package org.barrelmancer.civilization.cooking.config;

public class CookingConfig {
    private final String cookingType;
    private final String dataFileName;
    private final int cookTicks;

    public CookingConfig(String cookingType, String dataFileName, int cookTicks) {
        this.cookingType = cookingType;
        this.dataFileName = dataFileName;
        this.cookTicks = cookTicks;
    }

    public String getCookingType() {
        return cookingType;
    }

    public String getMemoryFileName() {
        return dataFileName;
    }

    public int getCookTicks() {
        return cookTicks;
    }
}