package com.github.tartaricacid.netmusic.client.init;

import com.github.tartaricacid.netmusic.client.gui.CDBurnerMenuScreen;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import net.minecraft.client.gui.screens.MenuScreens;

public class InitContainerGui {
    public static void init() {
        MenuScreens.register(CDBurnerMenu.TYPE, CDBurnerMenuScreen::new);
    }
}
