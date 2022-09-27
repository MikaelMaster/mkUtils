package com.mikael.mkutils.api.redis

import com.mikael.mkutils.api.Redis
import com.mikael.mkutils.api.redis.RedisAPI.client
import com.mikael.mkutils.api.redis.RedisAPI.clientConnection
import com.mikael.mkutils.api.syncMysqUpdatesKey
import com.mikael.mkutils.api.toTextComponent
import com.mikael.mkutils.bungee.UtilsBungeeMain
import com.mikael.mkutils.spigot.UtilsMain
import net.eduard.api.lib.config.ConfigUtil.removeChar
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.scheduler.ScheduledTask
import org.bukkit.scheduler.BukkitTask
import redis.clients.jedis.JedisPubSub
import java.util.concurrent.TimeUnit

object RedisBungeeAPI {

    /**
     * Use to connect a player to a  specific Spigot server.
     * It'll send a message to Proxy(s) to connect the given [playerName] to the given [serverName].
     * If the proxy that receive this message don't have the given player online, nothing will happen.
     *
     * @param playerName the player to connect.
     * @param serverName the server to connect the given [playerName].
     * @return True if the request has been sent with success. Otherwise, false.
     * @throws IllegalStateException if the [RedisAPI] [client] or the [clientConnection] is null.
     */
    fun connectToServer(playerName: String, serverName: String): Boolean {
        return RedisAPI.sendEvent("mkUtils:BungeeAPI:Action:Connect", "${playerName};${serverName}")
    }

    /**
     * Get player amount of the given [serverName].
     *
     * @param serverName the server to request data to Proxy server(s).
     * @return The player amount (Int). It'll return -1 if that server does not exist.
     */
    fun getPlayerAmount(serverName: String): Int {
        synchronized(syncMysqUpdatesKey) { return playersCache[serverName.lowercase()]?.size ?: -1 }
    }

    fun getOnlinePlayers(): List<String> {
        synchronized(syncMysqUpdatesKey) {
            val online = mutableListOf<String>()
            playersCache.values.forEach { stringMutableList ->
                stringMutableList.forEach {
                    online.add(it)
                }
            }
            return online
        }
    }

    /**
     * null
     */
    fun kickPlayer(playerName: String, kickReason: String, bypassPerm: String? = "none"): Boolean {
        return synchronized(syncMysqUpdatesKey) {
            Redis.client!!.publish("mkUtils:BungeeAPI:Action:KickPlayer", "${playerName};${kickReason};${bypassPerm}")
            true
        }
    }

    /**
     * Get player amount of all Proxy(ies). (Global player amount)
     *
     * @return The global player amount (Int).
     */
    fun getGlobalPlayerAmount(): Int {
        synchronized(syncMysqUpdatesKey) {
            return getOnlinePlayers().size
        }
    }

    // EXTRA SECTION

    val playersCache = mutableMapOf<String, MutableList<String>>()

    var bukkitServerTask: BukkitTask? = null

    fun bukkitServerOnEnable() {
        bukkitServerTask = UtilsMain.instance.asyncTask {
            val connectionData = UtilsMain.instance.config["Redis", RedisConnectionData::class.java]
            val extraJedis = RedisAPI.createExtraClient(connectionData)
            RedisAPI.connectExtraClient(extraJedis, connectionData)

            extraJedis.subscribe(object : JedisPubSub() {
                override fun onMessage(channel: String, message: String) {
                    if (channel != "mkUtils:BungeeAPI:ServerData:Players") return
                    val data = message.split(";")
                    val server = data[0]
                    val playersString = data[1]
                    if (playersString.isEmpty()) {
                        playersCache[server.lowercase()] = mutableListOf()
                        return
                    }
                    val players = playersString.split(",")
                    synchronized(syncMysqUpdatesKey) {
                        playersCache[server.lowercase()] = players.toMutableList()
                    }
                }
            }, "mkUtils:BungeeAPI:ServerData:Players")
        }
    }

    var proxyServerTask: ScheduledTask? = null

    fun proxyServerOnEnable() {
        proxyServerTask = ProxyServer.getInstance().scheduler.schedule(UtilsBungeeMain.instance, {
            val connectionData = UtilsBungeeMain.instance.config["Redis", RedisConnectionData::class.java]
            val extraJedis = RedisAPI.createExtraClient(connectionData)
            RedisAPI.connectExtraClient(extraJedis, connectionData)

            ProxyServer.getInstance().servers.values.forEach { serverInfo ->
                val playersName = mutableListOf<String>()
                serverInfo.players.forEach { playersName.add(it.name) }
                val players = playersName.joinToString(",")
                players.lastOrNull()?.let { players.removeChar(it) }
                extraJedis.publish(
                    "mkUtils:BungeeAPI:ServerData:Players",
                    "${serverInfo.name};${players}"
                )
            } // Send player list update to Redis every 1 second
        }, 1, 1, TimeUnit.SECONDS)

        ProxyServer.getInstance().scheduler.runAsync(UtilsBungeeMain.instance) {
            val connectionData = UtilsBungeeMain.instance.config["Redis", RedisConnectionData::class.java]
            val extraJedis = RedisAPI.createExtraClient(connectionData)
            RedisAPI.connectExtraClient(extraJedis, connectionData)

            extraJedis.subscribe(
                object : JedisPubSub() {
                    override fun onMessage(channel: String, message: String) {
                        if (channel == "mkUtils:BungeeAPI:Action:Connect") {
                            val data = message.split(";")
                            val player = ProxyServer.getInstance().getPlayer(data[0]) ?: return
                            val server = ProxyServer.getInstance().getServerInfo(data[1]) ?: return
                            player.connect(server)
                        }
                        if (channel == "mkUtils:BungeeAPI:ServerData:Players") {
                            val data = message.split(";")
                            val server = data[0]
                            val playersString = data[1]
                            if (playersString.isEmpty()) {
                                playersCache[server.lowercase()] = mutableListOf()
                                return
                            }
                            val players = playersString.split(",")
                            synchronized(syncMysqUpdatesKey) {
                                playersCache[server.lowercase()] = players.toMutableList()
                            }
                        }
                        if (channel == "mkUtils:BungeeAPI:Action:KickPlayer") {
                            val data = message.split(";")
                            val player = ProxyServer.getInstance().getPlayer(data[0]) ?: return
                            val reason = data[1].toTextComponent()
                            val bypassPerm = data[2]
                            if (bypassPerm != "none" && player.hasPermission(bypassPerm)) return
                            player.disconnect(reason)
                        }
                    }
                }, "mkUtils:BungeeAPI:Action:Connect",
                "mkUtils:BungeeAPI:ServerData:Players",
                "mkUtils:BungeeAPI:Action:KickPlayer"
            )
        }
    }

}