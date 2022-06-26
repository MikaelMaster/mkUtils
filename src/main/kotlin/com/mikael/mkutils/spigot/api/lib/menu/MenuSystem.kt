package com.mikael.mkutils.spigot.api.lib.menu

import com.mikael.mkutils.spigot.api.openedMineMenu
import org.bukkit.entity.Player

object MenuSystem {

    val registeredMenus = mutableSetOf<MineMenu>()

    val openedMenu = mutableMapOf<Player, MineMenu>()
    val openedPage = mutableMapOf<Player, MenuPage>()

    fun isMenuOpen(menu: MineMenu, player: Player): Boolean {
        return player.openedMineMenu != null && player.openedMineMenu!! == menu
    }

}