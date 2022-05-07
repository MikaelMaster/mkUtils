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
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * @return True if the player has the needed amount of the needed ItemStack on his inventory.
 */
fun Player.hasAmountOfItemOnInv(needed: ItemStack, neededAmount: Int): Boolean {
    return this.inventory.hasAmountOfItem(needed, neededAmount)
}

/**
 * @return True if the inventory has the needed amount of the needed ItemStack.
 */
fun Inventory.hasAmountOfItem(needed: ItemStack, neededAmount: Int): Boolean {
    if (this.isEmpty) return false
    var amount = 0
    for (item in this) {
        if (item == null) continue
        if (needed == item) {
            amount++
            if (amount >= neededAmount) return true
        }
    }
    return false
}

/**
 * Make an ItemStack and similars not breakable.
 *
 * @return The new not breakable ItemStack.
 */
fun <T : ItemStack> T.notBreakable(): T {
    val meta = itemMeta!!
    meta.isUnbreakable = true
    itemMeta = meta
    return this
}

@Deprecated(
    "Does NOT work on new versions of minecraft that not use 1.8 protocol.", ReplaceWith(
        "Particle(ParticleType.LARGE_SMOKE, 1).create(player, this.clone().add(0.5, 1.0, 0.5))",
        "net.eduard.api.lib.game.Particle",
        "net.eduard.api.lib.game.ParticleType"
    )
)
fun Location.smokeDenyBuild(player: Player) {
    Particle(ParticleType.LARGE_SMOKE, 1).create(player, this.clone().add(0.5, 1.0, 0.5))
}

/**
 * @see World.newHologram
 */
fun Location.newHologram(line: String): ArmorStand {
    if (this.world == null) error("Cannot spawn a hologram on a unloaded world")
    return this.world!!.newHologram(this, line)
}

/**
 * Extra of see also: (loc: Location, toDown: Boolean, vararg lines: String): List<ArmorStand>
 * @see World.newHologram
 */
fun Location.newHologram(toDown: Boolean, vararg lines: String): List<ArmorStand> {
    if (this.world == null) error("Cannot spawn a hologram on a unloaded world")
    return this.world!!.newHologram(this, toDown, *lines)
}

/**
 * Spawn a new hologram with just one line.
 *
 * @return The spawned ArmorStand that compose this hologram.
 */
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

/**
 * Spawn a new hologram with multiple lines.
 *
 * @return A list of all spawned ArmorStands that compose the hologram.
 */
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
    this.playSound(this.location, Sound.ENTITY_ENDERMAN_TELEPORT, volume, speed)
}

/**
 * Gives an item to a player if there is an available slot on his inventory.
 * If there is no empty slot, the ItemStack will be dropped on the world, using the player's eye location.
 *
 * @param item the ItemStack to be add on player's inventory. (It will be dropped if the inventory is full)
 * @return A dropped Item?-- if the invetory is full.
 */
fun Player.giveItem(item: ItemStack): Item? {
    val slot = this.inventory.withIndex().firstOrNull { it.value == null }
        ?: return this.world.dropItemNaturally(this.eyeLocation, item)
    this.inventory.setItem(slot.index, item)
    return null
}

/**
 * Use in a command to run the Unit using a try catch. If any error occur,
 * the player will receive a message telling he that an error has been occured.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was runned with no problems. Otherwise, false.
 */
inline fun Player.runCommand(crossinline thing: (() -> Unit)): Boolean {
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

/**
 * Use in a command to run the Unit using async and a try catch. If any error occur,
 * the player will receive a message telling he that an error has been occured.
 *
 * @param sendLoading if is to send a 'Loading...' message before try to load the Unit using async.
 * @param thing the block code to run using async and try catch.
 */
inline fun Player.runCommandAsync(sendLoading: Boolean = true, crossinline thing: () -> (Unit)) {
    if (sendLoading) this.sendMessage("§eLoading...")
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

/**
 * Clear all the player's inventory and armors.
 *
 * @param resetHoldSlot if is to set the player's hold item slot to 0.
 */
fun Player.clearAllInventory(resetHoldSlot: Boolean = true) {
    if (resetHoldSlot) this.inventory.heldItemSlot = 0
    this.inventory.clear()
    this.inventory.setArmorContents(arrayOf()) // Clear armors
}

/**
 * Clear the player's action bar.
 */
fun Player.clearActionBar() {
    this.spigot().sendMessage(ChatMessageType.ACTION_BAR, " ".toTextComponent())
}

/**
 * Send an action bar to the player.
 *
 * @param msg the message to send on player's action bar.
 */
fun Player.actionBar(msg: String) {
    this.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg.toTextComponent())
}

/**
 * Send a title and subtitle to the player.
 *
 * @see Player.sendTitle
 */
fun Player.title(title: String?, subtitle: String?, fadeIn: Int = 10, stay: Int = 20 * 2, fadeOut: Int = 10) {
    this.sendTitle(title, subtitle, fadeIn, stay, fadeOut)
}