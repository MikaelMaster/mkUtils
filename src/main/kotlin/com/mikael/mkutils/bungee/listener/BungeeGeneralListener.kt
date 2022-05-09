package com.mikael.mkutils.bungee.listener

import com.mikael.mkutils.api.redis.RedisAPI
import com.mikael.mkutils.api.toTextComponent
import com.mikael.mkutils.bungee.UtilsBungeeMain
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.ServerConnectEvent
import net.md_5.bungee.api.event.ServerConnectedEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class BungeeGeneralListener : Listener {

    @EventHandler
    fun versionCommand(e: ChatEvent) {
        if (!e.message.equals("/mkutilsproxy", true)) return
        val player = e.sender as ProxiedPlayer
        player.sendMessage("§a${UtilsBungeeMain.instance.systemName} §ev${UtilsBungeeMain.instance.description.version} §f- §bdeveloped by Mikael.".toTextComponent())
        e.isCancelled = true
    }

    @EventHandler
    fun onPlayerJoin(e: ServerConnectEvent) {
        if (!RedisAPI.isInitialized() || !RedisAPI.useToSyncBungeePlayers) return
        RedisAPI.updateCounter("mkUtils", "mkbungeeapi:playercount", 1)
        RedisAPI.client!!.set("mkutils:bungee:players:${e.player.name.lowercase()}", "null")
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerDisconnectEvent) {
        if (!RedisAPI.isInitialized() || !RedisAPI.useToSyncBungeePlayers) return
        RedisAPI.updateCounter("mkUtils", "mkbungeeapi:playercount", -1)
        RedisAPI.client!!.del("mkutils:bungee:players:${e.player.name.lowercase()}")
    }

    @EventHandler
    fun onServerChange(e: ServerConnectedEvent) {
        if (!RedisAPI.isInitialized() || !RedisAPI.useToSyncBungeePlayers) return
        RedisAPI.client!!.set("mkutils:bungee:players:${e.player.name.lowercase()}", e.server.info.name)
    }

}