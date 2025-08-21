package org.barrelmancer.civilization.cooking.config;

import net.kyori.adventure.text.Component;
import org.barrelmancer.civilization.constants.CampfireConstants;
import org.barrelmancer.civilization.constants.CauldronConstants;
import org.bukkit.Material;

import java.util.List;

public class CookingGUIConfigFactory {

    public static CookingGUIConfig createCampfireConfig() {
        return new CookingGUIConfig.Builder()
                .rows(3)
                .title(Component.text(CampfireConstants.GUI_TITLE))
                .inputSlots(List.of(1, 10, 19))
                .outputSlot(16)
                .decorativeMaterial(Material.GRAY_STAINED_GLASS_PANE)
                .decorativeModelData(1000)
                .build();
    }

    public static CookingGUIConfig createCauldronConfig() {
        return new CookingGUIConfig.Builder()
                .rows(1)
                .title(Component.text(CauldronConstants.GUI_TITLE))
                .inputSlots(List.of(1))
                .outputSlot(7)
                .decorativeMaterial(Material.GRAY_STAINED_GLASS_PANE)
                .decorativeModelData(1000)
                .build();
    }
}
