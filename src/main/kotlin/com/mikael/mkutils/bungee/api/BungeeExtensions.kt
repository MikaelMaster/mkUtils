package com.mikael.mkutils.bungee.api

import com.mikael.mkutils.api.toTextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer

/**
 * Use in a proxy command to run the Unit using a try catch. If any error occur,
 * the proxied player will receive a message telling he that an error has been occured.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was runned with no problems. Otherwise, false.
 */
inline fun ProxiedPlayer.runCommand(crossinline thing: (() -> Unit)): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.sendMessage("§cAn internal error occurred while executing this command.".toTextComponent())
        false
    }
}