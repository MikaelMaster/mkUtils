package com.mikael.mkutils.api.mkplugin

import com.mikael.mkutils.api.mkplugin.MKPluginSystem.loadedMKPlugins


/**
 * @see MKPlugin
 */
@Suppress("WARNINGS")
object MKPluginSystem {

    internal val loadedMKPlugins = mutableListOf<MKPlugin>()

    init {
        loadedMKPlugins.clear()
    }

    /**
     * @return all loaded [MKPlugin]s.
     * @see loadedMKPlugins
     */
    fun getLoadedMKPlugins(): List<MKPlugin> {
        return loadedMKPlugins
    }

    fun registerMKPlugin(plugin: MKPlugin) {
        loadedMKPlugins.add(plugin)
    }

    fun unregisterMKPlugin(plugin: MKPlugin) {
        loadedMKPlugins.remove(plugin)
    }

}