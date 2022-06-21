package com.mikael.mkutils.api.mkplugin

object MKPluginSystem {

    val loadedMKPlugins = mutableListOf<MKPlugin>()

    init {
        loadedMKPlugins.clear()
    }

}