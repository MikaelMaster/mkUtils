package com.mikael.mkutils.api.mkplugin

import net.eduard.api.lib.plugin.IPluginInstance

interface MKPlugin : IPluginInstance {

     val isFree: Boolean
     fun log(msg: String)
}