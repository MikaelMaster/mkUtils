package com.mikael.mkutils.spigot.api

import com.mikael.mkutils.api.toTextComponent
import com.mikael.mkutils.spigot.UtilsMain
import com.mikael.mkutils.spigot.api.lib.MineItem
import com.mikael.mkutils.spigot.api.lib.menu.MenuPage
import com.mikael.mkutils.spigot.api.lib.menu.MenuSystem
import com.mikael.mkutils.spigot.api.lib.menu.MineMenu
import com.mikael.mkutils.spigot.listener.GeneralListener
import net.eduard.api.lib.game.ItemBuilder
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatMessageType
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.Openable
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * @return a Paper [Component] with the given [String].
 */
fun String?.toPaperComponent(): Component {
    return if (this != null) {
        Component.text(this)
    } else {
        Component.text("")
    }
}

/**
 * Sets/returns player's opened [MineMenu].
 *
 * @return Player's opened [MineMenu]?.
 */
var Player.openedMineMenu: MineMenu?
    get() = MenuSystem.openedMenu[this]
    internal set(value) {
        if (value == null) {
            MenuSystem.openedMenu.remove(this)
        } else {
            MenuSystem.openedMenu[this] = value
        }
    }

/**
 * Sets/returns player's opened [MenuPage].
 *
 * @return Player's opened [MenuPage]?.
 */
var Player.openedMineMenuPage: MenuPage?
    get() = MenuSystem.openedPage[this]
    internal set(value) {
        if (value == null) {
            MenuSystem.openedPage.remove(this)
        } else {
            MenuSystem.openedPage[this] = value
        }
    }

/**
 * @return The player that clicked the menu. ([InventoryClickEvent.getWhoClicked] as [Player])
 */
val InventoryClickEvent.player get() = this.whoClicked as Player

fun <T : ItemStack> T.addLore(vararg lines: String): T {
    val meta = this.itemMeta!!
    if (meta.lore == null) meta.lore = emptyList()
    val newLore = mutableListOf<String>()
    for (line in meta.lore!!) {
        newLore.add(line)
    }
    for (newLine in lines) {
        newLore.add(newLine)
    }
    meta.lore = newLore
    this.itemMeta = meta
    return this
}

/**
 * @return A new [ItemBuilder] cloning the given [ItemStack].
 */
@Deprecated("Deprecated since mkUtils v1.1; Use MineItem instead ItemBuilder.", ReplaceWith("this.toMineItem()"))
fun ItemStack.toItemBuilder(): ItemBuilder {
    return ItemBuilder(this)
}

/**
 * @return A new [MineItem] cloning the given [ItemStack].
 */
fun ItemStack.toMineItem(): MineItem {
    return MineItem(this)
}

fun Entity.setInvincible(isInvincible: Boolean): Entity {
    if (isInvincible) {
        GeneralListener.instance.invincibleEntities.add(this)
    } else {
        GeneralListener.instance.invincibleEntities.remove(this)
    }
    return this
}

/**
 * @return True if this entity is a peceful entity. Otherwise, false.
 */
val Entity.isPeaceful: Boolean
    get() {
        if (this is Creature) {
            return this !is Monster
        }
        return true
    }

/**
 * @return True if the player's inventory have free slots. Otherwise, false.
 */
val Player.hasFreeSlots: Boolean get() = this.freeSlots > 0

/**
 * @return The amount of free slots in this player's inventory.
 */
val Player.freeSlots: Int get() = 36 - this.inventory.contents.size

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
 * Set if a block is waterlogged or not.
 * If this block doesn't support be waterlogged, nothing will happen.
 *
 * @param isWatterlogged if this block is to be waterlogged or not.
 * @return The new waterlogged (or not) [Block].
 */
fun Block.waterlogged(isWatterlogged: Boolean): Block {
    val blockData = this.blockData
    if (blockData !is Waterlogged) return this
    blockData.isWaterlogged = isWatterlogged
    this.blockData = blockData
    return this
}

/**
 * Set if a block is opened or not.
 * If this block doesn't support be opned, nothing will happen.
 * This is to be used with trapdors, for example.
 *
 * @param isOpened if this block is opened or not.
 * @return The new opened (or not) [Block].
 */
fun Block.opened(isOpened: Boolean): Block {
    val blockData = this.blockData
    if (blockData !is Openable) return this
    blockData.isOpen = isOpened
    this.blockData = blockData
    return this
}

/**
 * Sets a Custom Model Data to a ItemStack.
 *
 * @param data the Int or null to be set Custom Model Data.
 * @return The new [ItemStack] with the set Custom Model Data.
 */
fun <T : ItemStack> T.customModelData(data: Int?): T {
    val meta = itemMeta!!
    meta.setCustomModelData(data)
    itemMeta = meta
    return this
}

/**
 * Make an ItemStack and similars not breakable.
 *
 * @param isUnbreakable if the item will be or not unbreakable. By default, True.
 * @return The new not breakable [ItemStack].
 */
fun <T : ItemStack> T.notBreakable(isUnbreakable: Boolean = true): T {
    val meta = itemMeta!!
    meta.isUnbreakable = isUnbreakable
    itemMeta = meta
    return this
}

/**
 * Extra of see also: (loc: Location, vararg lines: String?): ArmorStand
 *
 * @see World.newHologram
 */
fun Location.newHologram(line: String?): ArmorStand {
    if (this.world == null) error("Cannot spawn a hologram on a unloaded world")
    return this.world!!.newHologram(this, line)
}

/**
 * Extra of see also: (loc: Location, toDown: Boolean, vararg lines: String?): List<ArmorStand>
 *
 * @see World.newHologram
 */
fun Location.newHologram(toDown: Boolean, vararg lines: String?): List<ArmorStand> {
    if (this.world == null) error("Cannot spawn a hologram in a unloaded world")
    return this.world!!.newHologram(this, toDown, *lines)
}

/**
 * Spawn a new hologram with just one line.
 *
 * @return The spawned [ArmorStand] that compose this hologram.
 */
fun World.newHologram(loc: Location, line: String?): ArmorStand {
    if (!loc.chunk.isLoaded) {
        loc.chunk.load(true)
    }
    val holo = loc.world!!.spawn(loc, ArmorStand::class.java)
    holo.setGravity(false)
    holo.isVisible = false
    holo.isSmall = true
    holo.isMarker = false
    if (line != null) {
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
 * @return A [List] of all spawned [ArmorStand] that compose the hologram.
 */
fun World.newHologram(loc: Location, toDown: Boolean, vararg lines: String?): List<ArmorStand> {
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
 * @param item the ItemStack to be added on player's inventory. (It will be dropped if the inventory is full)
 * @return A dropped [Item] if the invetory is full. Otherwise, false.
 */
fun Player.giveItem(item: ItemStack): Item? {
    val slot = this.inventory.contents.withIndex().firstOrNull { it.value == null }
        ?: return this.world.dropItemNaturally(this.eyeLocation, item)
    this.inventory.setItem(slot.index, item)
    return null
}

/**
 * Runs a loading animation to the player using the main thread (sync), while execute the given [thing] using async.
 *
 * @param loadingMsg the message to show with the loading animation. By default, the msg is 'Loading...' and the color is yellow (§e).
 * @param small if True, the loading animation will NOT show  the [loadingMsg], and will play the animation on the actionBar. If false, the animation will be played using titles.
 * @param thing the block code to run using try catch and the load animation.
 */
inline fun Player.asyncLoading(
    loadingMsg: String? = "§eLoading...",
    small: Boolean = true,
    crossinline thing: (() -> Unit)
) {
    var step = 0
    val runnable = UtilsMain.instance.syncTimer(0, 2) {
        if (!small) {
            when (step) {
                0 -> {
                    this.title("§a∎§7∎∎∎∎", "§e${loadingMsg}", 0, 10, 0)
                }
                1 -> {
                    this.title("§7∎§a∎§7∎∎∎", "§e${loadingMsg}", 0, 10, 0)
                }
                2 -> {
                    this.title("§7∎∎§a∎§7∎∎", "§e${loadingMsg}", 0, 10, 0)
                }
                3 -> {
                    this.title("§7∎∎∎∎§a∎", "§e${loadingMsg}", 0, 10, 0)
                }
            }
        } else {
            when (step) {
                0 -> {
                    this.actionBar("§a∎§7∎∎∎∎")
                }
                1 -> {
                    this.actionBar("§7∎§a∎§7∎∎∎")
                }
                2 -> {
                    this.actionBar("§7∎∎§a∎§7∎∎")
                }
                3 -> {
                    this.actionBar("§7∎∎∎§a∎§7∎")
                }
                4 -> {
                    this.actionBar("§7∎∎∎∎§a∎")
                }
            }
        }
        if (step == 3) step = 0 else step++
    }
    UtilsMain.instance.asyncTask {
        try {
            thing.invoke()
        } catch (ex: Exception) {
            ex.printStackTrace()
            this.soundNo()
            this.sendMessage("§cAn internal error occurred while executing something to you.")
        }
        UtilsMain.instance.syncTask {
            runnable.cancel()
            if (!small) {
                this.title("§a§l∎∎∎∎∎", "§7${loadingMsg} §a§lCompleted.", 5, 20, 5)
            } else {
                this.clearActionBar()
            }
        }
    }
}

/**
 * Use it anywhere to run the Unit using a try catch. If any error occur,
 * the player will receive a message telling he that an error has been occured.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was runned with no problems. Otherwise, false.
 */
inline fun Player.runBlock(crossinline thing: (() -> Unit)): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.soundNo()
        this.sendMessage("§cAn internal error occurred while executing something to you.")
        false
    }
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
@Deprecated("Deprecated since mkUtils v1.1; Use Player.runCommand instead, intering an Async Block")
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
    this.inventory.armorContents = arrayOf() // Clear armors
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

/**
 * Don't use it, this is not a usefull good method.
 *
 * @return the chunk blocks.
 * @see Chunk
 * @see Block
 */
val Chunk.blocks: List<Block>
    get() {
        val blocs = mutableListOf<Block>()
        for (x in 0..15) {
            for (y in 0..255) {
                for (z in 0..15) {
                    blocs.add(this.getBlock(x, y, z))
                }
            }
        }
        return blocs
    }