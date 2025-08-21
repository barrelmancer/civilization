package org.barrelmancer.civilization.campfire;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.barrelmancer.civilization.constants.CampfireConstants;
import org.barrelmancer.civilization.constants.UIConstants;
import org.barrelmancer.civilization.cooking.BaseCookingGUI;
import org.barrelmancer.civilization.cooking.config.CookingGUIConfigFactory;
import org.barrelmancer.civilization.memory.CampfireMemory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CampfireGUI extends BaseCookingGUI<CampfireMemory> {

    public CampfireGUI(Location location) {
        super(location,
                CookingGUIConfigFactory.createCampfireConfig(),
                CampfireManager.getInstance().getCookingMemory(location));
    }

    @Override
    protected Component getDecorativeDisplayName() {
        return Component.text("Campfire").style(Style.style(UIConstants.SUB_SUB_INFORMATION_COLOR, TextDecoration.ITALIC.withState(false)));
    }

    @Override
    protected void setupCustomSlots() {
        ItemStack existingBowl = memory.getAllSlotItems().get(CampfireConstants.BOWL_SLOT);
        inventory.setItem(CampfireConstants.BOWL_SLOT,
                existingBowl != null ? existingBowl : ItemStack.of(Material.AIR));
    }
}