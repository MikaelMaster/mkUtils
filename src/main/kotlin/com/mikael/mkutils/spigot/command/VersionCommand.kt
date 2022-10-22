package com.mikael.mkutils.spigot.command

import com.mikael.mkutils.spigot.UtilsMain
import net.eduard.api.lib.manager.CommandManager
import org.bukkit.command.CommandSender

class VersionCommand : CommandManager("mkutils") {

    private val versionMsg get() = "§a${UtilsMain.instance.systemName} §ev${UtilsMain.instance.description.version} §f- §bdeveloped with §c❤ §bby Mikael."

    init {
        usage = "/mkutils"
        permission = "mkutils.defaultperm"
        permissionMessage = versionMsg
        this.command.setExecutor(this@VersionCommand)
    }

    override fun command(sender: CommandSender, args: Array<String>) {
        sender.sendMessage(versionMsg)
    }

}