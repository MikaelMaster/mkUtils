package com.mikael.mkutils.spigot.listener

import com.mikael.mkutils.spigot.UtilsMain
import net.eduard.api.lib.manager.EventsManager
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class GeneralListener : EventsManager() {

    @EventHandler(priority = EventPriority.HIGH)
    fun versionCommand(e: PlayerCommandPreprocessEvent) {
        if (!e.message.equals("/mkutils", true)) return
        e.player.sendMessage("§a${UtilsMain.instance.systemName} §ev${UtilsMain.instance.description.version} §f- §bdeveloped by Mikael.")
        e.isCancelled = true
    }

}