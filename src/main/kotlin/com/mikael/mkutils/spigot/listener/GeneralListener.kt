package com.mikael.mkutils.spigot.listener

import com.mikael.mkutils.spigot.UtilsMain
import com.mikael.mkutils.spigot.api.lib.MineListener
import org.bukkit.entity.Entity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageEvent

class GeneralListener : MineListener() {
    companion object {
        lateinit var instance: GeneralListener
    }

    init {
        instance = this@GeneralListener

        UtilsMain.instance.syncTimer(20 * 3, 20 * 3) { // Invincible Entity System timer
            invincibleEntities.removeIf { it.isDead }
        }
    }

    /**
     * Invincible Entity System.
     */

    val invincibleEntities = mutableSetOf<Entity>()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onEntityDamage(e: EntityDamageEvent) {
        if (!invincibleEntities.contains(e.entity)) return
        e.isCancelled = true
    }

}