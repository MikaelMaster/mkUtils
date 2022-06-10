package com.mikael.mkutils.spigot.listener

import com.mikael.mkutils.spigot.UtilsMain
import net.eduard.api.lib.manager.EventsManager
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent

class GeneralListener : EventsManager() {
    companion object {
        lateinit var instance: GeneralListener
    }

    init {
        instance = this@GeneralListener

        UtilsMain.instance.syncTimer(20 * 5, 20 * 5) { // Invincible Entity System Timer
            invincibleEntities.removeIf { it.isDead }
        }
    }

    // Invincible Entity System - Start

    val invincibleEntities = mutableSetOf<Entity>()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamage(e: EntityDamageEvent) {
        if (!invincibleEntities.contains(e.entity)) return
        e.isCancelled = true
    }

    // Invincible Entity System - End

    @EventHandler(priority = EventPriority.HIGH)
    fun versionCommand(e: PlayerCommandPreprocessEvent) {
        if (!e.message.equals("/mkutils", true)) return
        e.player.sendMessage("§a${UtilsMain.instance.systemName} §ev${UtilsMain.instance.description.version} §f- §bdeveloped by Mikael.")
        e.isCancelled = true
    }

}