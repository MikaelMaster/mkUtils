package com.mikael.mkutils.bungee

import com.mikael.mkutils.api.UtilsManager
import com.mikael.mkutils.api.mkplugin.MKPlugin
import com.mikael.mkutils.api.mkplugin.MKPluginSystem
import com.mikael.mkutils.api.redis.RedisAPI
import com.mikael.mkutils.api.redis.RedisConnectionData
import com.mikael.mkutils.api.toTextComponent
import com.mikael.mkutils.api.utilsmanager
import com.mikael.mkutils.bungee.listener.BungeeGeneralListener
import net.eduard.api.lib.bungee.BungeeAPI
import net.eduard.api.lib.command.Command
import net.eduard.api.lib.config.Config
import net.eduard.api.lib.database.DBManager
import net.eduard.api.lib.database.HybridTypes
import net.eduard.api.lib.database.SQLManager
import net.eduard.api.lib.hybrid.BungeeServer
import net.eduard.api.lib.hybrid.Hybrid
import net.eduard.api.lib.kotlin.register
import net.eduard.api.lib.kotlin.resolvePut
import net.eduard.api.lib.kotlin.store
import net.eduard.api.lib.modules.Copyable
import net.eduard.api.lib.storage.StorageAPI
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.scheduler.ScheduledTask
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class UtilsBungeeMain : Plugin(), MKPlugin {
    companion object {
        lateinit var instance: UtilsBungeeMain
    }

    var redisVerifier: ScheduledTask? = null
    lateinit var mySqlUpdaterTimer: Thread
    lateinit var manager: UtilsManager
    lateinit var config: Config

    init {
        Hybrid.instance = BungeeServer
    }

    override fun onEnable() {
        instance = this@UtilsBungeeMain
        val start = System.currentTimeMillis()
        manager = resolvePut(UtilsManager()) // Needs to be here

        log("§eStarting loading...")
        HybridTypes // Hybrid types loading
        store<RedisConnectionData>()

        log("§eloading directories...")
        storage()
        config = Config(this, "config.yml")
        config.saveConfig()
        reloadConfig() // x1
        reloadConfig() // x2
        StorageAPI.updateReferences()

        log("§eLoading extras...")
        reload()

        DBManager.setDebug(false)
        manager.sqlManager = SQLManager(config["Database", DBManager::class.java])
        if (manager.sqlManager.dbManager.isEnabled) {
            log("§eConnecting to MySQL database...")
            utilsmanager.dbManager.openConnection()
            if (!utilsmanager.sqlManager.hasConnection()) error("Cannot connect to MySQL database")
            log("§aConnected to MySQL database!")
        } else {
            log("§cThe MySQL is not active on the config file. Some plugins and MK systems may not work correctly.")
        }

        RedisAPI.managerData = config["Redis", RedisConnectionData::class.java]
        if (RedisAPI.managerData.isEnabled) {
            log("§eConnecting to Redis server...")
            RedisAPI.createClient(RedisAPI.managerData)
            RedisAPI.connectClient()
            if (!RedisAPI.isInitialized()) error("Cannot connect to Redis server")
            RedisAPI.useToSyncBungeePlayers = RedisAPI.managerData.syncBungeeDataUsingRedis
            log("§aConnected to Redis server!")
            redisVerifier = ProxyServer.getInstance().scheduler.schedule(
                this, {
                    if (!RedisAPI.testPing()) {
                        log("§cThe connection with redis server is broken. :c")
                        try {
                            log("§eTrying to reconnect to redis server...")
                            RedisAPI.createClient(RedisAPI.managerData) // Recreate a redis client
                            RedisAPI.connectClient(true) // Reconnect redis client
                            log("§aReconnected to redis server! (Some data may not have been synced)")
                        } catch (ex: Exception) {
                            error("Cannot reconnect to redis server: ${ex.stackTrace}")
                        }
                    }
                }, 1, 1, TimeUnit.SECONDS
            )
        } else {
            log("§cRedis is not active on the config file. Some plugins and MK systems may not work correctly.")
        }

        // BungeeAPI
        BungeeAPI.bungee.register(this)

        log("§eLoading systems...")
        BungeeGeneralListener().register(this)

        val endTime = System.currentTimeMillis() - start
        log("§aPlugin loaded with success! (Time taken: §f${endTime}ms§a)"); MKPluginSystem.loadedMKPlugins.add(this@UtilsBungeeMain)

        // MySQL queue updater timer
        if (utilsmanager.sqlManager.hasConnection()) {
            mySqlUpdaterTimer = thread {
                while (utilsmanager.sqlManager.hasConnection()) {
                    utilsmanager.sqlManager.runChanges()
                    Thread.sleep(1000)
                }
            }
        }
    }

    override fun onDisable() {
        log("§eUnloading systems...")
        redisVerifier?.cancel()
        utilsmanager.dbManager.closeConnection()
        RedisAPI.finishConnection()
        log("§cPlugin unloaded!"); MKPluginSystem.loadedMKPlugins.remove(this@UtilsBungeeMain)
    }

    private fun storage() {
        StorageAPI.setDebug(false)
        StorageAPI.startGson()
    }

    private fun reloadConfig() {
        config.add(
            "Database",
            DBManager(),
            "Config of MySQL database.",
            "All the plugins that use the mkUtilsProxy will use this MySQL database."
        )
        config.add(
            "Redis",
            RedisConnectionData(),
            "Config of Redis server.",
            "All the plugins that use the mkUtilsProxy will use this Redis server."
        )
        config.saveConfig()
    }

    private fun reload() {
        Config.isDebug = false
        Copyable.setDebug(false)
        Command.MESSAGE_PERMISSION = "§cYou don't have permission to use this command."
    }

    fun log(msg: String) {
        ProxyServer.getInstance().console.sendMessage("§b[${systemName}] §f${msg}".toTextComponent())
    }

    override fun getPlugin(): Any {
        return this
    }

    override fun getSystemName(): String {
        return this.description.name
    }

    override fun getPluginFolder(): File {
        return this.dataFolder
    }

}