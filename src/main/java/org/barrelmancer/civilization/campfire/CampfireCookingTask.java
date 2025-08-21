package org.barrelmancer.civilization.campfire;

import org.barrelmancer.civilization.cooking.BaseCookingTask;
import org.barrelmancer.civilization.cooking.CookingManager;

public class CampfireCookingTask extends BaseCookingTask {

    @Override
    protected CookingManager<?> getCookingManager() {
        try {
            return CampfireManager.getInstance();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    @Override
    protected String getTaskName() {
        return "CampfireManager";
    }
}