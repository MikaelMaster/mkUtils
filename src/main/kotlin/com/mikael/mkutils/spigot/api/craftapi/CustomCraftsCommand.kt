package com.mikael.mkutils.spigot.api.craftapi

import com.mikael.mkutils.spigot.api.runCommand
import com.mikael.mkutils.spigot.api.soundClick
import net.eduard.api.lib.manager.CommandManager
import org.bukkit.entity.Player

class CustomCraftsCommand : CommandManager("customcrafts", "customcraftrecipes", "customrecipes") {

    init {
        usage = "/customcrafts"
        permission = "mkutils.cmd.customcrafts"
        permissionMessage = "§cYou don't have permission to use this command."
        this.command.setExecutor(this@CustomCraftsCommand)
    }

    override fun playerCommand(player: Player, args: Array<String>) {
        player.runCommand {
            player.soundClick(2f, 2f)
            CustomRecipesMenu.instance.update()
            CustomRecipesMenu.instance.open(player)
        }
    }

}