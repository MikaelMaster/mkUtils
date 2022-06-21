package com.mikael.mkutils.spigot.task

import com.mikael.mkutils.spigot.UtilsMain
import com.mikael.mkutils.spigot.api.lib.menu.MenuSystem
import net.eduard.api.lib.manager.TimeManager
import net.eduard.api.lib.menu.getMenu
import net.eduard.api.lib.modules.Mine

class AutoUpdateMenusTask : TimeManager(UtilsMain.instance.config.getLong("MenuAPI.autoUpdateTicks")) {

    override fun run() {
        for (player in Mine.getPlayers()) {
            try {
                val menu = player.getMenu() ?: continue
                val pageOpened = menu.getPageOpen(player)
                val inventory = player.openInventory.topInventory
                menu.update(inventory, player, pageOpened, false)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            /*
            // My menu system
            try {
                val menu = MenuSystem.openedMenu[player]!!
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
             */
        }
    }
}