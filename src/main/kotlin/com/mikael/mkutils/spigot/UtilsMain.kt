package com.mikael.mkutils.spigot

import com.mikael.mkutils.api.UtilsManager
import com.mikael.mkutils.api.formatValue
import com.mikael.mkutils.api.mkplugin.MKPlugin
import com.mikael.mkutils.api.mkplugin.MKPluginSystem
import com.mikael.mkutils.api.redis.RedisAPI
import com.mikael.mkutils.api.redis.RedisBungeeAPI
import com.mikael.mkutils.api.redis.RedisConnectionData
import com.mikael.mkutils.spigot.api.lib.craft.CraftAPI
import com.mikael.mkutils.spigot.api.lib.menu.MenuSystem
import com.mikael.mkutils.spigot.api.lib.menu.example.ExampleMenuCommand
import com.mikael.mkutils.spigot.api.lib.menu.example.SinglePageExampleMenu
import com.mikael.mkutils.spigot.api.storable.LocationStorable
import com.mikael.mkutils.spigot.command.VersionCommand
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
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.text.SimpleDateFormat

@Suppress("DEPRECATION", "WARNINGS")
class UtilsMain : JavaPlugin(), MKPlugin, BukkitTimeHandler {
    companion object {
        lateinit var instance: UtilsMain
    }

    private var mySqlQueueUpdater: BukkitTask? = null
    lateinit var config: Config

    init {
        Hybrid.instance = BukkitServer // EduardAPI
    }

    override fun onEnable() {
        instance = this@UtilsMain
        val loadStart = System.currentTimeMillis()

        log("§eLoading basics...")
        UtilsManager.mkUtilsVersion = this.description.version
        prepareStorageAPI() // EduardAPI
        HybridTypes // {static} # Hybrid types - Load
        BukkitTypes.register() // Bukkit types - Load
        store<RedisConnectionData>()

        Extra.FORMAT_DATE = SimpleDateFormat("MM/dd/yyyy") // EduardAPI
        Extra.FORMAT_DATETIME = SimpleDateFormat("MM/dd/yyyy HH:mm:ss") // EduardAPI

        log("§eLoading directories...")
        config = Config(this@UtilsMain, "config.yml")
        config.saveConfig()
        reloadConfigs() // x1
        reloadConfigs() // x2
        StorageAPI.updateReferences() // EduardAPI

        log("§eLoading extras...")
        preparePlaceholders(); prepareBasics(); prepareTasks()

        log("§eLoading APIs...")
        MenuSystem.onEnable()
        CraftAPI.onEnable()

        // BukkitBungeeAPI
        BukkitBungeeAPI.register(this) // EduardAPI
        BukkitBungeeAPI.requestCurrentServer() // EduardAPI
        BungeeAPI.bukkit.register(this) // EduardAPI

        log("§eLoading systems...")
        prepareDebugs(); prepareMySQL(); prepareRedis()

        // Commands
        VersionCommand().registerCommand(this)

        // Listeners
        GeneralListener().registerListener(this)

        val endTime = System.currentTimeMillis() - loadStart
        log("§aPlugin loaded with success! (Time taken: §f${endTime}ms§a)")
        MKPluginSystem.loadedMKPlugins.add(this@UtilsMain)

        syncDelay(20) {
            log("§aPreparing MineReflect...")
            try {
                MineReflect.getVersion() // Prints the server (reflect) version
            } catch (ex: Exception) {
                Mine.console("§b[MineReflect] §cThe current version is not supported. Some custom features will not work, mkUtils will run with default Paper ones.")
            }

            // Show MK Plugins
            log("§aLoaded MK Plugins:")
            for (mkPlugin in MKPluginSystem.loadedMKPlugins) {
                log(" §7▪ §e${mkPlugin}")
            }

            // MySQL queue updater timer
            if (UtilsManager.sqlManager.hasConnection()) {
                mySqlQueueUpdater = asyncTimer(20, 20) {
                    if (!UtilsManager.sqlManager.hasConnection()) return@asyncTimer
                    UtilsManager.sqlManager.runChanges()
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

        if (RedisBungeeAPI.isEnabled) {
            log("§6[RedisBungeeAPI] §eMarking server as disabled on Redis...")
            RedisBungeeAPI.updateSpigotServerState(false)
        }

        log("§eUnloading APIs...")
        CraftAPI.onDisable()
        MenuSystem.onDisable()

        log("§eUnloading systems...")
        BungeeAPI.controller.unregister() // EduardAPI
        RedisBungeeAPI.bukkitServerPubSubThread?.interrupt()
        if (RedisAPI.isInitialized()) {
            RedisAPI.jedisPool.destroy()
        }
        mySqlQueueUpdater?.cancel()
        UtilsManager.dbManager.closeConnection()

        log("§cPlugin unloaded!")
        MKPluginSystem.loadedMKPlugins.remove(this@UtilsMain)
    }

    private fun prepareRedis() {
        RedisAPI.managerData = config["Redis", RedisConnectionData::class.java]
        if (!RedisAPI.managerData.isEnabled) {
            log("§cRedis is not active on the config file. Some plugins and MK systems may not work correctly.")
            return
        }
        log("§eConnecting to Redis server...")
        RedisAPI.onEnablePrepareRedisAPI()
        if (!RedisAPI.isInitialized()) error("Cannot connect to Redis server")
        RedisAPI.useToSyncBungeePlayers = RedisAPI.managerData.syncBungeeDataUsingRedis
        if (RedisBungeeAPI.isEnabled) {
            RedisBungeeAPI.bukkitServerOnEnable()
            syncDelay(1) {
                RedisBungeeAPI.updateSpigotServerState(true)
            } // This will be executed after server is done loading
        }
        log("§aConnected to Redis server!")
    }

    private fun prepareMySQL() {
        UtilsManager.sqlManager = SQLManager(config["Database", DBManager::class.java])
        if (!UtilsManager.sqlManager.dbManager.isEnabled) {
            log("§cThe MySQL is not active on the config file. Some plugins and MK systems may not work correctly.")
            return
        }
        log("§eConnecting to MySQL database...")
        UtilsManager.dbManager.openConnection()
        if (!UtilsManager.sqlManager.hasConnection()) error("Cannot connect to MySQL database")
        log("§aConnected to MySQL database!")
    }

    private fun prepareStorageAPI() {
        StorageAPI.setDebug(false) // EduardAPI
        BukkitStorables.load() // EduardAPI

        // Storable Custom Objects
        StorageAPI.registerStorable(Location::class.java, LocationStorable())
        // StorageAPI.registerStorable(MineItemStorable::class.java, MineItemStorable()) // Unfinished

        StorageAPI.startGson() // EduardAPI
    }

    private fun preparePlaceholders() { // mkUtils default placeholders
        Mine.addReplacer("mkutils_players") {
            Bukkit.getOnlinePlayers().size.formatValue()
        }
        Mine.addReplacer("mkbungeeapi_players") {
            try {
                RedisBungeeAPI.getGlobalPlayerAmount()
            } catch (ex: Exception) {
                -1
            }
        }
    }

    private fun prepareDebugs() {
        if (config.getBoolean("MenuAPI.debugMode")) {
            SinglePageExampleMenu().registerMenu(this)
            ExampleMenuCommand().registerCommand(this)
        }
    }

    private fun prepareBasics() {
        Menu.isDebug = false // EduardAPI legacy Menu System - Debug Mode
        DBManager.setDebug(false) // EduardAPI
        Config.isDebug = false // EduardAPI
        Hologram.debug = false // EduardAPI
        CommandManager.debugEnabled = false // EduardAPI
        CommandManager.DEFAULT_USAGE_PREFIX = "§cUsage: " // EduardAPI
        CommandManager.DEFAULT_DESCRIPTION = "Description not defined." // EduardAPI
        Copyable.setDebug(false) // EduardAPI
        BukkitBungeeAPI.setDebuging(false) // EduardAPI
        DisplayBoard.colorFix = true // EduardAPI
        DisplayBoard.nameLimit = 40 // EduardAPI
        DisplayBoard.prefixLimit = 16 // EduardAPI
        DisplayBoard.suffixLimit = 16 // EduardAPI
    }

    private fun prepareTasks() {
        resetScoreboards() // EduardAPI
        BukkitReplacers() // EduardAPI
        if (config.getBoolean("MenuAPI.autoUpdateMenus")) {
            AutoUpdateMenusTask().syncTimer()
        }
        PlayerTargetAtPlayerTask().asyncTimer()
    }

    private fun resetScoreboards() { // EduardAPI
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
        // config.setHeader("mkUtils v${description.version} config file.") // It's bugged

        config.add(
            "Database",
            DBManager(),
            "Config of MySQL database.",
            "All plugins that use mkUtils DB will use this MySQL database."
        )
        config.add(
            "Redis",
            RedisConnectionData(),
            "Config of Redis server.",
            "All plugins that use mkUtils RedisAPI will use this Redis server."
        )
        config.add(
            "RedisBungeeAPI.spigotServerName",
            "lobby-1",
            "The name of this spigot server defined on Proxy config file."
        )
        config.add(
            "MenuAPI.autoUpdateMenus",
            true,
            "If true, mkUtils MenuAPI menus will auto update while open.",
        )
        config.add(
            "MenuAPI.autoUpdateTicks",
            60L,
            "Time to update players opened mkUtils MenuAPI menus.",
            "Values less than 20 may cause lag. 20 ticks = 1s."
        )
        config.add(
            "MenuAPI.debugMode",
            false,
            "If true, example/test menus and test menus commands will be registered.",
            "Also, some mkUtils MenuAPI actions will be logged to console."
        )
        config.add(
            "CraftAPI.customCraftsMenuAndCommand",
            false,
            "If true, mkUtils CraftAPI Custom Crafts Menu will be registered.",
            "Also, the command '/customcrafts' will be registered too. It can be used to acces the menu."
        )
        config.add(
            "CustomKick.isEnabled",
            true,
            "If true, mkUtils will kick all online players with a custom message."
        )
        config.add(
            "CustomKick.customKickMessage",
            "§cRestarting, we'll back soon!",
            "Kick message used to kick players on server shutdown."
        )
        config.saveConfig()
    }

    override fun log(vararg msg: String) {
        msg.forEach {
            Bukkit.getConsoleSender().sendMessage("§b[${systemName}] §f${it}")
        }
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