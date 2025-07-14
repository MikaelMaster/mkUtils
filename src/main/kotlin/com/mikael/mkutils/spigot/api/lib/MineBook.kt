@file:Suppress("DEPRECATION")

package com.mikael.mkutils.spigot.api.lib

import com.mikael.mkutils.api.isMultOf
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.ItemMeta

/**
 * [MineBook] util class
 *
 * This class extends a [MineItem].
 *
 * To create/invoke a new MineBook you can use:
 * - MineBook() -> A new [MineItem] with the material [Material.WRITTEN_BOOK] will be given as 'baseItem'.
 * - MineBook(baseItem: [MineItem]) -> This will keep the 'baseItem' [ItemMeta] and properties.
 * - [MineItem.toMineBook] -> This will use the given [MineItem] as 'baseItem'.
 *
 * @author Mikael
 * @see MineItem
 * @see BookMeta
 */
open class MineBook(baseItem: MineItem) : MineItem(baseItem) {

    /**
     * @return [MineBook] giving an 'empty' [MineItem] as 'baseItem' param.
     */
    constructor() : this(MineItem(Material.WRITTEN_BOOK))

    /**
     * @return this ([MineItem.getItemMeta]) as [BookMeta].
     */
    val book get() = this.itemMeta as BookMeta

    /**
     * Sets/returns the [book] title.
     * @see BookMeta.hasTitle
     * @see BookMeta.setTitle
     * @see BookMeta.getTitle
     */
    var title
        get() = book.title
        set(newTitle) {
            setTitle(newTitle)
        }

    /**
     * Sets/returns the [book] author.
     *
     * Important: If the [BookMeta.hasAuthor] is false, "Unknown" will be returned.
     * Calling [BookMeta.getAuthor] without checking it before can throw an [NullPointerException] if the author is null.
     *
     * @see BookMeta.hasAuthor
     * @see BookMeta.getAuthor
     * @see BookMeta.setAuthor
     */
    var visualAuthor
        get() = run {
            if (book.hasAuthor()) {
                book.author
            } else {
                "Unknown"
            }
        }
        set(newAuthor) {
            setAuthor(newAuthor)
        }

    /**
     * @see [BookMeta.getPages]
     */
    val pages get() = book.pages

    /**
     * @see [BookMeta.getPageCount]
     */
    val pageCount get() = book.pageCount

    /**
     * @param title the new title of this book. Cannot have more than 16 characters.
     * @throws IllegalStateException if the given [title] have more than 16 characters.
     */
    fun setTitle(title: String?): MineBook {
        title?.let { if (it.length > 16) error("The title of the book cannot have more than 16 characters") }
        val bookMeta = book
        bookMeta.title = title
        this.itemMeta = bookMeta
        return this
    }

    /**
     * @param author the new author of this book.
     */
    fun setAuthor(author: String?): MineBook {
        val bookMeta = book
        bookMeta.author = author
        this.itemMeta = bookMeta
        return this
    }

    /**
     * @param lines the [String]s to be 'write' in this book.
     * @return this [MineBook].
     * @throws IllegalStateException if the characters sum of the given [lines] is greater than 2,800 (256 * 50).
     * @see [BookMeta.addPage]
     * @see pages
     */
    fun setText(vararg lines: String): MineBook {
        val textBuilder = StringBuilder(); lines.forEach { textBuilder.append(it) }
        val finalText = textBuilder.toString()
        if (finalText.length > (256 * 50)) error("The characters sum of the given lines cannot be greater than 2,800 (256 * 50)") // Verification
        val bookMeta = book
        var pageBuilder = StringBuilder()
        for ((index, char) in finalText.toList().withIndex()) {
            pageBuilder.append(char)
            if (index != 1 && index.isMultOf(255)) {
                bookMeta.addPage(pageBuilder.toString())
                pageBuilder = StringBuilder()

            }
        }
        this.itemMeta = bookMeta
        return this
    }

    /**
     * Important:
     * - A book can have only a MAXIMUM of 50 pages.
     * - Each page cannot have more than 256 characters.
     *
     * @param pages the new pages to set on this book. Each page can have multiple [String] lines.
     * @return this [MineBook].
     * @throws IllegalStateException if the [pages] size is larger than 50.
     * @throws IllegalStateException if any page's sum of all characters is greater than 256.
     * @see [BookMeta.addPage]
     * @see pages
     */
    fun setPages(vararg pages: List<String>): MineBook {

        // Verifications
        if (pages.size > 50) error("A book cannot have more than 50 pages")
        pages.forEach { page ->
            var charCount = 0
            page.forEach { line ->
                charCount += line.length
                charCount += 2 // Line break separator (\n) - 2 chars
            }
            if (charCount > 256) error("The sum of all characters in a book page should not be greater than 256")
        }

        // Action - Change pages
        val bookMeta = book
        bookMeta.pages.clear() // Reset pages
        for (page in pages) {
            val pageLinesBuilder = StringBuilder()
            page.forEach { pageLine ->
                pageLinesBuilder.append(pageLine)
                pageLinesBuilder.append("\n")
            }
            bookMeta.addPage(pageLinesBuilder.toString())
        }
        this.itemMeta = bookMeta
        return this
    }

    /**
     * Note: This function uses NMS. (NMS 1.8_R3)
     *
     * When this function is called, pay attention to some points: ([BookMeta] = [book])
     * - if the [BookMeta.hasTitle] is false, the default book title will be "Blank".
     * - if the [BookMeta.hasAuthor] is false, the default book author will be "Unknown".
     * - if the [BookMeta.hasPages] is false, the book will have one page with the text "Nothing here...".
     *
     * @param player the player to open this book gui.
     * @return this [MineBook].
     * @see Player.openBook
     */
    fun open(player: Player): MineBook {
        // val bookMeta = book // Useless ?
        // if (!bookMeta.hasTitle()) setTitle("Blank") // Useless ?
        // if (!bookMeta.hasAuthor()) setAuthor("Unknown") // Useless ?
        // if (!bookMeta.hasPages()) setPages(listOf("Nothing here...")) // Useless ?
        val holdSlot = player.inventory.heldItemSlot
        val oldInvItem = player.inventory.getItem(0)
        player.inventory.setItem(0, this@MineBook) // This is necessary, otherwise the book gui will not open
        player.inventory.heldItemSlot = 0 // The player needs to hold the given book
        player.openBook(this@MineBook)
        player.inventory.setItem(0, oldInvItem)
        player.inventory.heldItemSlot = holdSlot
        player.updateInventory()
        return this
    }

}