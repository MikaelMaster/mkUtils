package com.mikael.mkutils.api.mkplugin

import net.eduard.api.lib.plugin.IPluginInstance

/**
 * Represents an MK Plugin. Extends an [IPluginInstance].
 *
 * @author Mikael
 */

interface MKPlugin: IPluginInstance {

     /**
      * Use to log plugin messages to console.
      *
      *
      * Example for Spigot:
      * ```
      *  override fun log(vararg msg: String) {
      *      Bukkit.getConsoleSender().sendMessage(msg)
      *  }
      *```
      *
      * Example for Bungee:
      * ```
      *  override fun log(vararg msg: String) {
      *      ProxyServer.getInstance().console.sendMessage(*msg.map { it.toTextComponent() }.toTypedArray())
      *  }
      *  ```
      *
      * @param msg the messages to log.
      */
     fun log(vararg msg: String)
}