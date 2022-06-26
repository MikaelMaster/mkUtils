package com.mikael.mkutils.spigot

import com.mikael.mkutils.api.mkplugin.MKPluginSystem
import com.mikael.mkutils.api.UtilsManager
import com.mikael.mkutils.api.formatEN
import com.mikael.mkutils.api.mkplugin.MKPlugin
import com.mikael.mkutils.api.redis.RedisAPI
import com.mikael.mkutils.api.redis.RedisConnectionData
import com.mikael.mkutils.api.utilsmanager
import com.mikael.mkutils.spigot.api.craftapi.CraftAPI
import com.mikael.mkutils.spigot.api.lib.menu.example.SinglePageExampleMenu
import com.mikael.mkutils.spigot.api.lib.menu.example.ExampleMenuCommand
import com.mikael.mkutils.spigot.api.storable.LocationStorable
import com.mikael.mkutils.spigot.listener.GeneralListener
import com.mikael.mkutils.spigot.task.AutoUpdateMenusTask
import com.mikael.mkutils.spigot.task.PlayerTargetAtPlayerTask
import net.eduard.api.core.BukkitReplacers
import net.eduard.api.lib.abstraction.Hologram
import net.eduard.api.lib.bungee.BungeeAPI
import net.eduard.api.lib.config.Config
import net.eduard.api.lib.database.BukkitTypes
import net.eduard.api.lib.database.DBManager
import net.eduard.api.lib.database.HybridTypes
import net.eduard.api.lib.database.SQLManager
import net.eduard.api.lib.hybrid.BukkitServer
import net.eduard.api.lib.hybrid.Hybrid
import net.eduard.api.lib.kotlin.resolvePut
import net.eduard.api.lib.kotlin.store
import net.eduard.api.lib.manager.CommandManager
import net.eduard.api.lib.menu.Menu
import net.eduard.api.lib.modules.*
import net.eduard.api.lib.score.DisplayBoard
import net.eduard.api.lib.storage.StorageAPI
import net.eduard.api.lib.storage.storables.BukkitStorables
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.text.SimpleDateFormat
import kotlin.concurrent.thread

class UtilsMain : JavaPlugin(), MKPlugin, BukkitTimeHandler {
    companion object {
        lateinit var instance: UtilsMain
    }

    lateinit var mySqlUpdaterTimer: Thread
    lateinit var manager: UtilsManager
    lateinit var config: Config
    lateinit var messages: Config
    var serverEnabled = false

    init {
        Hybrid.instance = BukkitServer
    }

    override fun onEnable() {
        instance = this@UtilsMain
        val start = System.currentTimeMillis()

        log("§eStarting loading...")
        HybridTypes // Hybrid types loading
        BukkitTypes.register() // Bukkit types loading
        store<RedisConnectionData>()

        Extra.FORMAT_DATE = SimpleDateFormat("MM/dd/yyyy")
        Extra.FORMAT_DATETIME = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")

        log("§eLoading directories...")
        storage()
        config = Config(this@UtilsMain, "config.yml")
        config.saveConfig()
        reloadConfigs() // x1
        reloadConfigs() // x2
        messages = Config(this@UtilsMain, "messages.yml")
        messages.saveConfig()
        reloadMessages() // x1
        reloadMessages() // x2
        StorageAPI.updateReferences()

        log("§eLoading replacers...")
        replacers()
        log("§eLoading extras...")
        reload()
        log("§eStarting tasks...")
        tasks()

        // BukkitBungeeAPI
        BukkitBungeeAPI.register(this)
        BukkitBungeeAPI.requestCurrentServer()
        BungeeAPI.bukkit.register(this)

        manager = resolvePut(UtilsManager())
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

            syncTimer(20, 20) {
                if (!RedisAPI.testPing()) {
                    try {
                        RedisAPI.connectClient(true)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            }
        } else {
            log("§cThe Redis is not active on the config file. Some plugins and MK systems may not work correctly.")
        }

        log("§eLoading systems...")
        GeneralListener().registerListener(this)

        val endTime = System.currentTimeMillis() - start
        log("§aPlugin loaded with success! (Time taken: §f${endTime}ms§a)"); MKPluginSystem.loadedMKPlugins.add(this@UtilsMain)

        syncDelay(20) {
            MineReflect.getVersion() // Prints the server version
            serverEnabled = true
            log("§aThe server has been marked as available!")

            // Show MK Plugins
            log("§aLoaded MK Plugins:")
            for (mkPlugin in MKPluginSystem.loadedMKPlugins) {
                log(" §7▪ §e${mkPlugin}")
            }

            // MySQL queue updater timer
            if (utilsmanager.sqlManager.hasConnection()) {
                mySqlUpdaterTimer = thread {
                    while (true) {
                        utilsmanager.sqlManager.runChanges()
                        Thread.sleep(1000)
                    }
                }
            }
        }
    }

    override fun onDisable() {
        if (config.getBoolean("CustomKick.isEnabled")) {
            log("§eDisconnecting players...")
            for (playerLoop in Bukkit.getOnlinePlayers()) {
                playerLoop.kickPlayer(config.getString("CustomKick.customKickMessage"))
            }
        }
        log("§eUnloading systems...")
        BungeeAPI.controller.unregister()
        utilsmanager.dbManager.closeConnection()
        RedisAPI.finishConnection()
        CraftAPI.onDisable() // Disable CraftAPI
        log("§cPlugin unloaded!"); MKPluginSystem.loadedMKPlugins.remove(this@UtilsMain)
    }

    private fun storage() {
        StorageAPI.setDebug(false)
        BukkitStorables.load()

        // StorableTypes
        StorageAPI.registerStorable(Location::class.java, LocationStorable())

        StorageAPI.startGson()
    }

    private fun replacers() {
        Mine.addReplacer("mkutils_players") {
            Bukkit.getOnlinePlayers().size.formatEN()
        }
        Mine.addReplacer("mkbungeeapi_players") {
            if (!config.getBoolean("BungeeAPI.isEnabled")) {
                -1
            } else {
                RedisAPI.client!!.get("mkUtils:mkbungeeapi:playercount").toInt().formatEN()
            }
        }
    }

    private fun reload() {
        // Menu System - 'Debug' Mode
        if (config.getBoolean("MenuAPI.debugMode")) {
            SinglePageExampleMenu().registerMenu(this)
            ExampleMenuCommand().registerCommand(this)
        }
        Menu.isDebug = false // another debug type; legacy

        Config.isDebug = false
        Hologram.debug = false
        CommandManager.debugEnabled = false
        CommandManager.DEFAULT_USAGE_PREFIX = "§cUsage: "
        CommandManager.DEFAULT_DESCRIPTION = "Description not defined."
        Copyable.setDebug(false)
        BukkitBungeeAPI.setDebuging(false)
        DisplayBoard.colorFix = true
        DisplayBoard.nameLimit = 40
        DisplayBoard.prefixLimit = 16
        DisplayBoard.suffixLimit = 16
        CraftAPI.onEnable() // Enable CraftAPI
    }

    private fun tasks() {
        resetScoreboards()
        BukkitReplacers()
        if (config.getBoolean("MenuAPI.autoUpdateMenus")) {
            AutoUpdateMenusTask().syncTimer()
        }
        PlayerTargetAtPlayerTask().syncTimer()
    }

    private fun resetScoreboards() {
        for (team in Mine.getMainScoreboard().teams) {
            team.unregister()
        }
        for (objective in Mine.getMainScoreboard().objectives) {
            objective.unregister()
        }
        for (player in Mine.getPlayers()) {
            player.scoreboard = Mine.getMainScoreboard()
            player.maxHealth = 20.0
            player.health = 20.0
            player.isHealthScaled = false
        }
    }

    private fun reloadConfigs() {
        config.add(
            "Database",
            DBManager(),
            "Config of MySQL database.",
            "All the plugins that use the mkUtils will use this MySQL database."
        )
        config.add(
            "Redis",
            RedisConnectionData(),
            "Config of Redis server.",
            "All the plugins that use the mkUtils will use this Redis server."
        )
        config.add(
            "MenuAPI.autoUpdateMenus",
            true,
            "Whether to update the open menus.",
        )
        config.add(
            "MenuAPI.autoUpdateTicks",
            60,
            "Time to refresh players opened menus.",
            "Values less than 20 will cause lag. 20 ticks = 1s."
        )
        config.add(
            "MenuAPI.debugMode",
            false,
            "If true, example/test menus will be registered",
            "and menu test commands will be registered too."
        )
        config.add(
            "BungeeAPI.isEnabled", false, "Whether to activate the BungeeAPI."
        )
        config.add(
            "BungeeAPI.useRedisCache", false,
            "Whether to use Redis to improve the server performance.",
            "You CAN'T active this if you're not using a Redis server on mkUtils."
        )
        config.add(
            "BungeeAPI.currentServerName", "server",
            "The name of this spigot server before bungee.",
            "Put the server name as it is in the bungee config."
        )
        config.add(
            "BungeeAPI.currentServerMaxAmount",
            100,
            "Maximum number of players on this spigot server.",
        )
        config.add(
            "CustomKick.isEnabled",
            true,
            "Whether to kick players on server shutdown."
        )
        config.add(
            "CustomKick.customKickMessage",
            "§cRestarting, we'll back soon!",
            "Kick message sent to players on server shutdown."
        )
        config.add(
            "CustomCrafts.customCraftsMenuAndCommand",
            false,
            "Enable/disable the command and menus to",
            "show mkUtils registered custom item crafts."
        )
        config.saveConfig()
    }

    private fun reloadMessages() {
        messages.add("busy-server-msg", "§cThe server is busy. Try again in a few seconds.")
        messages.saveConfig()
    }

    fun log(msg: String) {
        Bukkit.getConsoleSender().sendMessage("§b[${systemName}] §f${msg}")
    }

    override fun getPlugin(): Any {
        return this
    }

    override fun getSystemName(): String {
        return this.name
    }

    override fun getPluginFolder(): File {
        return this.dataFolder
    }

    override fun getPluginConnected(): Plugin {
        return this
    }
}