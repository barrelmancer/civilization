package org.barrelmancer.civilization.campfire;

import org.barrelmancer.civilization.cooking.config.CookingConfig;

public class CampfireConfig extends CookingConfig {
    private final int baseSaturation;

    public CampfireConfig(String cookingType, String dataFileName, int cookTicks, int baseSaturation) {
        super(cookingType, dataFileName, cookTicks);
        this.baseSaturation = baseSaturation;
    }

    public int getBaseSaturation() {
        return baseSaturation;
    }
}