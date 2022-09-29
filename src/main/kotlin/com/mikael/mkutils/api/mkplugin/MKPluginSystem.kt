package com.mikael.mkutils.api.mkplugin

object MKPluginSystem {

    val loadedMKPlugins = mutableListOf<MKPlugin>()

    init {
        loadedMKPlugins.clear()
    }

    fun activeMKPLugin(plugin: MKPlugin): Boolean {
        // do code here
        return true
    }
}