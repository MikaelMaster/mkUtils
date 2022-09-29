package com.mikael.mkutils.bungee.command

import com.mikael.mkutils.bungee.UtilsBungeeMain
import net.eduard.api.lib.command.Command
import net.eduard.api.lib.hybrid.ISender

class BungeeVersionCommand : Command("mkutilsproxy", "mkutilsbungee") {

    private val versionMsg get() = "§amkUtilsProxy §ev${UtilsBungeeMain.instance.description.version} §f- §bdeveloped with §c❤ §bby Mikael."

    init {
        usage = "/mkutilsproxy"
        permission = "mkutils.defaultperm"
        permissionMessage = versionMsg
    }

    override fun onCommand(sender: ISender, args: List<String>) {
        sender.sendMessage(versionMsg)
    }

}