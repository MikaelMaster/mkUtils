package com.mikael.mkutils.spigot.api.lib

import com.mikael.mkutils.spigot.api.soundNo
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

open class MineCommand(command: String, vararg aliases: String) : CommandExecutor {

    /**
     * MineCommand util class
     *
     * *NOT FINISHED YET! DON'T USE IT!*
     *
     * This class represents an [CommandExecutor].
     *
     * To create a new MineCommand, extends it in a Class. As the example below:
     * - class TestCommand : MineCommand(command: [String]) { *class code* } * No aliasses
     * - class TestCommand : MineCommand(command: [String], vararg aliasses: [String]) { *class code* }
     *
     * @author Mikael
     * @see CommandExecutor
     * @see command
     * @see playerCommand
     */

    /**
     * The command permission.
     * If null, everyone will be able to use this command.
     */
    var permission: String? = null

    /**
     * The command permission message.
     * Message sent when a player don't have permission to use it.
     */
    var permissionMessage: String = "§cYou don't have permission to use this command."

    /**
     * The command usage. Mesage sent when a player use the command incorrectly.
     *
     * @see sendUsage
     */
    var usage: String = "/${command}"

    /**
     * Sends the Command [usage] to the [sender] (Player or Console) using a message.
     */
    fun sendUsage(sender: CommandSender) {
        sender.sendMessage(usage)
    }

    open fun playerCommand(player: Player, args: List<String>) {
        command(player, args)
    }

    open fun command(sender: CommandSender, args: List<String>) {
        sendUsage(sender)
    }

    // label = subcommand
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        try {
            if (permission != null && !sender.hasPermission(permission!!)) {
                if (sender is Player) {
                    sender.soundNo()
                }
                sender.sendMessage(permissionMessage)
                return true
            }
            val finalArgs: List<String> = args?.toList() ?: listOf()
            if (sender is Player) {
                playerCommand(sender, finalArgs)
            } else {
                command(sender, finalArgs)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return true
    }

}