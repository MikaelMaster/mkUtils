package com.mikael.mkutils.api.lib

import com.mikael.mkutils.api.isProxyServer
import com.mikael.mkutils.api.toTextComponent
import net.md_5.bungee.api.ProxyServer
import org.bukkit.Bukkit

class MineCooldown(var duration: Long) {

    var messageOnCooldown: String? = "§cPlease wait to use this again."

    fun noMessages() {
        messageOnCooldown = null
    }

    @Transient
    val cooldowns = mutableMapOf<String, Map<Long, Long>>()

    fun cooldown(playerName: String): Boolean {
        if (onCooldown(playerName)) {
            sendOnCooldown(playerName)
            return false
        }
        setOnCooldown(playerName)
        return true
    }

    fun stopCooldown(playerName: String) {
        cooldowns.remove(playerName)
    }

    fun onCooldown(playerName: String): Boolean {
        return getResult(playerName) > 0
    }

    fun setOnCooldown(playerName: String): MineCooldown {
        if (onCooldown(playerName)) {
            stopCooldown(playerName)
        }
        cooldowns[playerName] = mapOf(System.currentTimeMillis() to duration)
        return this
    }

    fun sendOnCooldown(playerName: String) {
        messageOnCooldown?.let {
            if (isProxyServer) {
                ProxyServer.getInstance().getPlayer(playerName)
                    ?.sendMessage(it.replace("%time", "${getCooldown(playerName)}").toTextComponent())
            } else {
                Bukkit.getOnlinePlayers().firstOrNull { player -> player.name == playerName }
                    ?.sendMessage(it.replace("%time", "${getCooldown(playerName)}"))
            }
        }
    }

    fun getResult(playerName: String): Long {
        if (cooldowns.containsKey(playerName)) {
            val now = System.currentTimeMillis()
            val timeManager = cooldowns[playerName]!!
            val before = timeManager.keys.first()
            val cooldownDuration = timeManager.values.first() * 50
            val endOfCooldown = before + cooldownDuration
            val durationLeft = endOfCooldown - now
            return if (durationLeft <= 0) {
                0
            } else durationLeft / 50
        }
        return 0
    }

    fun getCooldown(playerName: String): Int {
        return (getResult(playerName) / 20).toInt() + 1
    }
}