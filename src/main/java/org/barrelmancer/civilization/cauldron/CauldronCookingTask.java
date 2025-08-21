package org.barrelmancer.civilization.cauldron;

import org.barrelmancer.civilization.cooking.BaseCookingTask;
import org.barrelmancer.civilization.cooking.CookingManager;

public class CauldronCookingTask extends BaseCookingTask {

    @Override
    protected CookingManager<?> getCookingManager() {
        try {
            return CauldronManager.getInstance();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Override
    protected String getTaskName() {
        return "CauldronManager";
    }
}