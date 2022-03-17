package com.mikael.mkutils.spigot.api

import com.mikael.mkutils.api.toTextComponent
import com.mikael.mkutils.spigot.UtilsMain
import net.eduard.api.lib.game.Particle
import net.eduard.api.lib.game.ParticleType
import net.md_5.bungee.api.ChatMessageType
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

fun <T : ItemStack> T.notBreakable(): T {
    val meta = itemMeta!!
    meta.isUnbreakable = true
    itemMeta = meta
    return this
}

fun Location.smokeDenyBuild(player: Player) {
    Particle(ParticleType.LARGE_SMOKE, 1).create(player, this.clone().add(0.5, 1.0, 0.5))
}

fun World.newHologram(loc: Location, line: String): ArmorStand {
    if (!loc.chunk.isLoaded) {
        loc.chunk.load(true)
    }
    val holo = loc.world!!.spawn(loc, ArmorStand::class.java)
    holo.setGravity(false)
    holo.isVisible = false
    holo.isSmall = true
    holo.isMarker = false
    if (line != "null") {
        holo.isCustomNameVisible = true
        holo.customName = line
    } else {
        holo.isCustomNameVisible = false
    }
    return holo
}

fun World.newHologram(loc: Location, toDown: Boolean, vararg lines: String): List<ArmorStand> {
    val holos = mutableListOf<ArmorStand>()
    var location: Location = loc
    for (line in lines) {
        val holo = this.newHologram(location, line)
        holos.add(holo)
        location = if (toDown) {
            loc.subtract(0.0, 0.3, 0.0)
        } else {
            loc.add(0.0, 0.3, 0.0)
        }
    }
    return holos
}

fun Player.soundNo(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_VILLAGER_NO, volume, speed)
}

fun Player.soundYes(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_VILLAGER_YES, volume, speed)
}

fun Player.soundClick(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.BLOCK_LEVER_CLICK, volume, speed)
}

fun Player.soundPickup(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_ITEM_PICKUP, volume, speed)
}

fun Player.soundPling(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.BLOCK_NOTE_BLOCK_PLING, volume, speed)
}

fun Player.notify(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, speed)
}

fun Player.soundTP(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_ENDERMAN_TELEPORT, 2f, 1f)
}

fun Player.giveItem(item: ItemStack): Item? {
    val slot = this.inventory.withIndex().firstOrNull { it.value == null }
        ?: return this.world.dropItemNaturally(this.eyeLocation, item)
    this.inventory.setItem(slot.index, item)
    return null
}

@Deprecated("Does not work on java version 16 or higher.")
inline fun Player.runCommand(thing: (() -> Unit)): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.soundNo()
        this.sendMessage("§cAn internal error occurred while executing this command.")
        false
    }
}

@Deprecated("Does not work on java version 16 or higher.")
inline fun Player.runCommandAsync(sendLoading: Boolean = true, crossinline thing: () -> (Unit)) {
    if (sendLoading) {
        this.sendMessage("§eLoading...")
    }
    UtilsMain.instance.asyncTask {
        try {
            thing.invoke()
        } catch (ex: Exception) {
            ex.printStackTrace()
            this.soundNo()
            this.sendMessage("§cAn internal error occurred while executing this command.")
        }
    }
}

fun Player.clearAllInventory() {
    this.inventory.clear()
    this.inventory.helmet = null
    this.inventory.chestplate = null
    this.inventory.leggings = null
    this.inventory.boots = null
}

fun Player.sendActionBar(msg: String) {
    this.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg.toTextComponent())
}