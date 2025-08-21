package org.barrelmancer.civilization.cauldron;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.cooking.BaseCookingGUI;
import org.barrelmancer.civilization.cooking.config.CookingGUIConfigFactory;
import org.barrelmancer.civilization.memory.CauldronMemory;
import org.bukkit.Location;

public class CauldronGUI extends BaseCookingGUI<CauldronMemory> {

    public CauldronGUI(Location location) {
        super(location,
                CookingGUIConfigFactory.createCauldronConfig(),
                CauldronManager.getInstance().getCookingMemory(location));
    }

    @Override
    protected Component getDecorativeDisplayName() {
        return Component.text("Cauldron").style(Style.style(UIConstants.SUB_SUB_INFORMATION_COLOR, TextDecoration.ITALIC.withState(false)));
    }

    @Override
    protected void setupCustomSlots() {
    }
}