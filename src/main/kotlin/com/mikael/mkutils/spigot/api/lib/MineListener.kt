package com.mikael.mkutils.spigot.api.lib

import com.mikael.mkutils.api.mkplugin.MKPlugin
import org.bukkit.Bukkit
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

open class MineListener : Listener {

    /**
     * MineListener util class
     *
     * This class represents a [Listener].
     *
     * To create a new MineListener, extends it in a Class. As the example below:
     * - class TestListener : MineListener() { *class code* }
     *
     * @author Mikael
     * @see Listener
     */

    var isRegistered: Boolean = false
    var ownerPlugin: MKPlugin? = null

    open fun registerListener(plugin: MKPlugin) {
        unregisterListener()
        Bukkit.getPluginManager().registerEvents(this, plugin.plugin as JavaPlugin)
        this.ownerPlugin = plugin
        this.isRegistered = true
    }

    fun unregisterListener() {
        if (!isRegistered) return
        HandlerList.unregisterAll(this)
        this.ownerPlugin = null
        this.isRegistered = false
    }

}