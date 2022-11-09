package com.mikael.mkutils.spigot.api

import com.mikael.mkutils.api.formatEN
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
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.Openable
import org.bukkit.block.data.Waterlogged
import org.bukkit.command.CommandSender
import org.bukkit.entity.*
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

/**
 * Transforms a [String]? into a [Component].
 *
 * @return a Paper [Component] with the given [String], or empty if null is given.
 * @see Component
 */
fun String?.toPaperComponent(): Component {
    return if (this != null) {
        Component.text(this)
    } else {
        Component.text("")
    }
}

/**
 * @return if [Ageable.isAdult] 'Adult' else 'Baby'.
 */
fun Ageable.formatAgeText(): String {
    return if (this.isAdult) "Adult" else "Baby"
}

/**
 * It'll check if the value returned from [Player.openedMineMenu] is not null.
 * If it's not, and the returned menu is the given [menu] the [player] have an opened [MineMenu].
 *
 * Note: This is a shortcut of [MenuSystem.isMenuOpen].
 *
 * @return True if the given [Player] is with the given [menu] opened. Otherwise, false.
 * @see MineMenu
 * @see Player.openedMineMenu
 * @see MenuSystem
 */
fun Player.isMineMenuOpen(menu: MineMenu): Boolean {
    return MenuSystem.isMenuOpen(menu, this)
}

/**
 * Sets/returns player's opened [MineMenu].
 *
 * Note: 'Set' option is *internal only*.
 *
 * @return Player's opened [MineMenu]?.
 * @see MineMenu
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
 * Note: 'Set' option is *internal only*.
 *
 * @return Player's opened [MenuPage]?.
 * @see MenuPage
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
 * @see InventoryClickEvent.getWhoClicked
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
 * Deprecated in favor of new mkUtils [MineItem], a new class to manage and use [ItemStack]s.
 *
 * @return A new [ItemBuilder] cloning the given [ItemStack].
 * @see ItemBuilder
 */
@Deprecated("Deprecated since mkUtils v1.1", ReplaceWith("ItemBuilder(this)", "net.eduard.api.lib.game.ItemBuilder"))
fun ItemStack.toItemBuilder(): ItemBuilder {
    return ItemBuilder(this)
}

/**
 * @return A new [MineItem] cloning the given [ItemStack].
 * @see MineItem
 */
fun ItemStack.toMineItem(): MineItem {
    return MineItem(this)
}

/**
 * Deprecated because some entities don't work well with this function, and may throw errors.
 * You should use your own method instead.
 *
 * @return the given [Entity], now invincible.
 * @see GeneralListener.invincibleEntities
 */
@Deprecated("Deprecated since mkUtils v1.1; Use your own method instead.")
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
 * @return True if the given [Inventory] has the needed amount of the needed [ItemStack].
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
 * @see Block
 * @see Waterlogged
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
 * @see Block
 * @see Openable
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
 * @param line the line string to spawn. If you give as null, the line will be empty.
 * @see World.newHologram
 */
fun Location.newHologram(line: String?): ArmorStand {
    if (this.world == null) error("Cannot spawn a hologram on a unloaded world")
    return this.world!!.newHologram(this, line)
}

/**
 * Extra of see also: (loc: Location, toDown: Boolean, vararg lines: String?): List<ArmorStand>
 *
 * @param toDown if the holograms should be spawned from up to down, or from down to up.
 * @param lines the list of lines string to spawn. If you give as null, the line will be empty.
 * @see World.newHologram
 */
fun Location.newHologram(toDown: Boolean, vararg lines: String?): List<ArmorStand> {
    if (this.world == null) error("Cannot spawn a hologram in a unloaded world")
    return this.world!!.newHologram(this, toDown, *lines)
}

/**
 * Spawn a new hologram with just one line.
 *
 * @param loc the location to spawn the holograms.
 * @param line the line string to spawn. If you give as null, the line will be empty.
 * @return The spawned [ArmorStand] that compose this hologram.
 * @see ArmorStand
 */
fun World.newHologram(loc: Location, line: String?): ArmorStand {
    loc.chunk.isForceLoaded = true // this is needed; without it, the chunk will unload
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
 * @param loc the location to spawn the holograms.
 * @param toDown if the holograms should be spawned from up to down, or from down to up.
 * @param lines the list of lines string to spawn. If you give as null, the line will be empty.
 * @return A [List] of all spawned [ArmorStand] that compose the hologram.
 * @see ArmorStand
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

/**
 * Runs an 'blood' effect on a [Player]'s body.
 *
 * @param allBody if is to spawn the 'blood' particle on player Head AND Foot. False = 'blood' particle JUST on player Head.
 * @return True if the player is not dead, and the effect was played. Otherwise, false.
 * @see World.playEffect
 */
fun Player.bloodEffect(allBody: Boolean = false): Boolean {
    if (this.isDead) return false
    this.world.playEffect(this.eyeLocation, Effect.STEP_SOUND, Material.REDSTONE_BLOCK)
    if (allBody) {
        this.world.playEffect(this.location, Effect.STEP_SOUND, Material.REDSTONE_BLOCK)
    }
    return true
}

/**
 * Plays the sound [Sound.ENTITY_VILLAGER_NO] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundNo(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_VILLAGER_NO, volume, speed)
}

/**
 * Plays the sound [Sound.ENTITY_VILLAGER_YES] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundYes(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_VILLAGER_YES, volume, speed)
}

/**
 * Plays the sound [Sound.BLOCK_LEVER_CLICK] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundClick(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.BLOCK_LEVER_CLICK, volume, speed)
}

/**
 * Plays the sound [Sound.ENTITY_ITEM_PICKUP] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundPickup(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_ITEM_PICKUP, volume, speed)
}

/**
 * Plays the sound [Sound.BLOCK_NOTE_BLOCK_PLING] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundPling(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.BLOCK_NOTE_BLOCK_PLING, volume, speed)
}

/**
 * Plays the sound [Sound.ENTITY_EXPERIENCE_ORB_PICKUP] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.notify(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, volume, speed)
}

/**
 * Plays the sound [Sound.ENTITY_ENDERMAN_TELEPORT] to the given [Player].
 *
 * @param volume the Float as the sound volume to play to the player.
 * @param speed the Float as the sound volume to play to the player.
 * @see Player.playSound
 */
fun Player.soundTP(volume: Float = 2f, speed: Float = 1f) {
    this.playSound(this.location, Sound.ENTITY_ENDERMAN_TELEPORT, volume, speed)
}

/**
 * Gives an item to a [Player] if there is an available slot on his inventory.
 * If there is no empty slot, the [ItemStack] will be dropped on the world, using the [Player]'s eye location.
 *
 * @param item the [ItemStack] to be added on [Player]'s [Inventory]. (It will be dropped if the inventory is full)
 * @return A dropped [Item] if the [Player]'s [Inventory] is full. Otherwise, null.
 * @see Item
 */
fun Player.giveItem(item: ItemStack): Item? {
    invSlot@ for (invItem in this.inventory.contents) {
        if (invItem == null) continue@invSlot
        if (item.isSimilar(invItem) && invItem.amount < 64) {
            invItem.amount++
            return null
        }
    }
    val slot = this.inventory.contents.withIndex().firstOrNull { it.value == null && it.index < 36 }
        ?: return this.world.dropItemNaturally(this.eyeLocation, item)
    this.inventory.setItem(slot.index, item)
    this.updateInventory()
    return null
}

/**
 * Gives an Armor Set to a player if all his equipment slots is available.
 * If there is no equipment slots available, the [ItemStack]s will be dropped on the world, using the given [Player]'s eye location.
 *
 * Tip: To know if all the armor has been successfully set on the player, just verify if the returned [List] with [Item]s is empty.
 *
 * @return A list of dropped [Item]s with the Armors that cannot be given to the [Player].
 * @see Item
 */
fun Player.giveArmorSet(
    helmet: ItemStack?,
    chestplate: ItemStack?,
    leggings: ItemStack?,
    boots: ItemStack?
): List<Item> {
    val droppedArmor = mutableListOf<Item>()
    if (helmet != null) {
        if (this.inventory.helmet != null) {
            droppedArmor.add(this.world.dropItemNaturally(this.eyeLocation, helmet))
        } else {
            this.inventory.helmet = helmet
        }
    }
    if (chestplate != null) {
        if (this.inventory.chestplate != null) {
            droppedArmor.add(this.world.dropItemNaturally(this.eyeLocation, chestplate))
        } else {
            this.inventory.chestplate = chestplate
        }
    }
    if (leggings != null) {
        if (this.inventory.leggings != null) {
            droppedArmor.add(this.world.dropItemNaturally(this.eyeLocation, leggings))
        } else {
            this.inventory.leggings = leggings
        }
    }
    if (boots != null) {
        if (this.inventory.boots != null) {
            droppedArmor.add(this.world.dropItemNaturally(this.eyeLocation, boots))
        } else {
            this.inventory.boots = boots
        }
    }
    return droppedArmor
}

/**
 * Runs a loading animation to the player using the main thread (sync), while execute the given [thing] using async.
 *
 * @param thing the block code to run using async, try catch and the load animation.
 */
inline fun Player.asyncLoading(
    errorMessage: String = "§cAn internal error occurred while executing something to you.",
    crossinline thing: (() -> Unit)
) {
    val runStart = System.currentTimeMillis()
    var step = 0
    val runnable = UtilsMain.instance.syncTimer(0, 2) {
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
        if (step == 4) step = 0 else step++
    }
    UtilsMain.instance.asyncTask {
        try {
            thing.invoke()
        } catch (ex: Exception) {
            ex.printStackTrace()
            this.soundNo()
            this.sendMessage(errorMessage)
        } finally {
            UtilsMain.instance.syncTask {
                runnable.cancel()
                this.actionBar("§a∎∎∎∎∎ §8${(System.currentTimeMillis() - runStart).toInt().formatEN()} ms")
            }
        }
    }
}

/**
 * Runs a loading animation to the player using an async thread, while execute the given [thing] using sync (main thread).
 *
 * Please note that the given [thing] will be run 5 ticks (approximately 250ms) after the function ram.
 * This is to avoid animation internal erros.
 *
 * @param thing the block code to run using the main thread (sync), try catch and the load animation.
 */
inline fun Player.syncLoading(
    errorMessage: String = "§cAn internal error occurred while executing something to you.",
    crossinline thing: (() -> Unit)
) {
    val runStart = System.currentTimeMillis()
    var step = 0
    var animating = true
    UtilsMain.instance.asyncTask {
        while (animating) {
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
            if (step == 4) step = 0 else step++
            Thread.sleep(100)
        }
    }
    UtilsMain.instance.syncDelay(5) {
        try {
            thing.invoke()
        } catch (ex: Exception) {
            ex.printStackTrace()
            this.soundNo()
            this.sendMessage(errorMessage)
        } finally {
            animating = false
            this.actionBar("§a∎∎∎∎∎ §8${(System.currentTimeMillis() - runStart).toInt().formatEN()} ms")
        }
    }
}

/**
 * Use it anywhere to run the Unit using a try catch. If any error occur,
 * the given [Player] will receive a message saying that an error occurred..
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun Player.runBlock(
    errorMessage: String = "§cAn internal error occurred while executing something to you.",
    crossinline thing: (() -> Unit)
): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.soundNo()
        this.sendMessage(errorMessage)
        false
    }
}

/**
 * Use in a command to run the Unit using a try catch. If any error occur,
 * the given [CommandSender] (can be the Console) will receive a message saying that an error occurred.
 *
 * If the given [CommandSender] is a [Player], [Player.runCommand] will be called internally.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun CommandSender.runCommand(
    errorMessage: String = "§cAn internal error occurred while executing this command.",
    crossinline thing: (() -> Unit)
): Boolean {
    if (this is Player) {
        return this.runCommand(errorMessage, thing)
    }
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.sendMessage(errorMessage)
        false
    }
}

/**
 * Use in a command to run the Unit using a try catch. If any error occur,
 * the given [Player] will receive a message saying that an error occurred.
 *
 * @param thing the block code to run using try catch.
 * @return True if the block code was run with no errors. Otherwise, false.
 */
inline fun Player.runCommand(
    errorMessage: String = "§cAn internal error occurred while executing this command.",
    crossinline thing: (() -> Unit)
): Boolean {
    return try {
        thing.invoke()
        true
    } catch (ex: Exception) {
        ex.printStackTrace()
        this.soundNo()
        this.sendMessage(errorMessage)
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
@Deprecated("Deprecated since mkUtils v1.1; Use Player.runCommand instead, inside an Async Block.")
inline fun Player.runCommandAsync(
    sendLoading: Boolean = true,
    errorMessage: String = "§cAn internal error occurred while executing this command.",
    crossinline thing: () -> (Unit)
) {
    if (sendLoading) this.sendMessage("§eLoading...")
    UtilsMain.instance.asyncTask {
        try {
            thing.invoke()
        } catch (ex: Exception) {
            ex.printStackTrace()
            this.soundNo()
            this.sendMessage(errorMessage)
        }
    }
}

/**
 * Clear all the player's inventory and armors.
 *
 * @param resetHoldSlot if is to set the player's hold item slot to 0.
 */
fun Player.clearAllInventory(resetHoldSlot: Boolean = true) {
    if (resetHoldSlot) {
        this.inventory.heldItemSlot = 0
    }
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
 * Send an action bar to the given player.
 *
 * @param msg the message to send on player's action bar.
 */
fun Player.actionBar(msg: String) {
    this.spigot().sendMessage(ChatMessageType.ACTION_BAR, msg.toTextComponent())
}

/**
 * Clears the given [Player] client title field.
 *
 * @see Player.resetTitle
 */
fun Player.clearTitle() {
    this.resetTitle()
}

/**
 * Sends a [title] and [subtitle] to the given [Player].
 *
 * @see Player.sendTitle
 */
fun Player.title(title: String?, subtitle: String?, fadeIn: Int = 10, stay: Int = 20 * 2, fadeOut: Int = 10) {
    this.sendTitle(title, subtitle, fadeIn, stay, fadeOut)
}

/**
 * Get all blocks inside the given chunk.
 *
 * Use with moderation.
 * May lag the server with many usages at the same time.
 *
 * @return all the chunk [Block]s.
 * @see Chunk
 * @see Block
 */
val Chunk.blocks: List<Block>
    get() {
        val blocks = mutableListOf<Block>()
        for (x in 0..15) {
            for (y in 0..255) {
                for (z in 0..15) {
                    blocks.add(this.getBlock(x, y, z))
                }
            }
        }
        return blocks
    }