package com.mikael.mkutils.spigot.api.lib.menu

import com.mikael.mkutils.spigot.api.openedMineMenu
import org.bukkit.entity.Player

object MenuSystem {

    val registeredMenus = mutableSetOf<MineMenu>()

    val openedMenu = mutableMapOf<Player, MineMenu>()
    val openedPage = mutableMapOf<Player, MenuPage>()

    fun isMenuOpen(menu: MineMenu, player: Player): Boolean {
        val openedMenu = player.openedMineMenu
        return openedMenu != null && openedMenu == menu
    }

    fun onEnable() {
        onDisable() // Same as onDisable
    }

    fun onDisable() {
        val registeredMenusCopy = mutableSetOf<MineMenu>(); registeredMenusCopy.addAll(registeredMenus)
        registeredMenusCopy.forEach { it.unregisterMenu() }; registeredMenusCopy.clear()
        registeredMenus.clear()
        openedMenu.clear()
        openedPage.clear()
    }

}