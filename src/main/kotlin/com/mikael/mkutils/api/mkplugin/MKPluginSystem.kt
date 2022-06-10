package com.mikael.mkutils.api.mkplugin

import net.eduard.api.lib.plugin.IPluginInstance

object MKPluginSystem {

    val loadedMKPlugins = mutableListOf<IPluginInstance>()

    init {
        loadedMKPlugins.clear()
    }

}