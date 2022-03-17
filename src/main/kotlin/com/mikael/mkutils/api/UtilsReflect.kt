package com.mikael.mkutils.api

import net.minecraft.server.level.EntityPlayer
import net.minecraft.server.network.PlayerConnection
import org.bukkit.entity.Player
import kotlin.reflect.KFunction

class UtilsReflect {

    fun run() {
        for (member in UtilsReflect::class.members) {
            if (member !is KFunction) continue
            if (member.name != "test") continue
            member.call(this)
        }
    }

    fun Player.getHandle(): EntityPlayer {
        return this::class.members.first { it.name == "getHandle" }.call(this) as EntityPlayer
    }

    fun Player.getConnection(): PlayerConnection {
        return this.getHandle()::class.members.first { it.name == "b" }.call(this) as PlayerConnection
    }

}