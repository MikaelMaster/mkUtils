package com.mikael.mkutils.spigot.api.lib.menu

import com.mikael.mkutils.api.isMultOf
import com.mikael.mkutils.api.mkplugin.MKPlugin
import com.mikael.mkutils.spigot.api.lib.MineItem
import com.mikael.mkutils.spigot.api.lib.MineListener
import com.mikael.mkutils.spigot.api.player
import com.mikael.mkutils.spigot.api.soundClick
import com.mikael.mkutils.spigot.listener.GeneralListener
// import net.eduard.api.lib.modules.Mine
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory

open class MineMenu(var title: String, var lineAmount: Int) : MineListener() {

    var isAutoUpdate = true // Auto update all opened menus every 3s

    // Auto Align options - Start
    var isAutoAlignItems = false
    var autoAlignIgnoreColumns = true
    var autoAlignSkipLines: List<Int> = emptyList()
    private val autoAlignPerPage: Int
        get() {
            var amount = 54
            if (!isAutoAlignItems) return amount
            for (n in 1..autoAlignSkipLines.size) amount -= 9
            if (autoAlignIgnoreColumns) amount -= lineAmount.minus(autoAlignSkipLines.size).times(2)
            return amount
        }
    // Auto Align options - End

    // Back and Skip Page buttons options - Start
    var backPageButtonPosX = 1
    var backPageButtonPosY = 1
    var backPageButtonItem = MineItem(Material.ARROW).name("§aPage %page%")
    var nextPageButtonPosX = 9
    var nextPageButtonPosY = 1
    var nextPageButtonItem = MineItem(Material.ARROW).name("§aPage %page%")
    // Back and Skip Page buttons options - End

    private val pages = mutableSetOf<MenuPage>()
    private val buttonsToRegister = mutableSetOf<MenuButton>()
    private val inventories = mutableMapOf<Int, Inventory>()

    val buttons: List<MenuButton>
        get() {
            val allButtons = mutableListOf<MenuButton>()
            for (page in pages) {
                for (button in page.buttons) {
                    allButtons.add(button)
                }
            }
            return allButtons
        }

    open fun registerMenu(plugin: MKPlugin) {
        if (lineAmount < 1 || lineAmount > 6) error("menu lineAmount must be between 1 and 6")
        this.registerListener(plugin)
        MenuSystem.registeredMenus.add(this)
    }

    open fun unregisterMenu(plugin: MKPlugin) {
        MenuSystem.openedMenu.keys.removeIf {// Remove menu, pages and close it to players
            val menu = MenuSystem.openedMenu[it]!!
            if (menu == this) it.closeInventory()
            MenuSystem.openedPage.keys.removeIf { pagePlayer ->
                pages.contains(MenuSystem.openedPage[pagePlayer]!!)
            }
            MenuSystem.openedMenu[it]!! == this
        }
        this.unregisterListener()
        MenuSystem.registeredMenus.remove(this)
    }

    open fun removeAllButtons() {
        for (page in pages) {
            page.buttons.clear()
        }
    }

    open fun update(player: Player) {
        // Do something
    }

    private fun invokePageNextAndBackButtons(page: MenuPage) {
        val inv = page.inventory ?: error("page inventory can't be null")
        if (page.hasBackPage) {
            val backPageInv = page.backPage?.inventory ?: error("menu previous page/inventory is null")
            val backButton = MenuButton("back-page").apply {
                autoEffectiveSlot = 0
                fixed = true
                icon = backPageButtonItem.clone().name(
                    backPageButtonItem.getName().replace("%page%", "${page.backPage!!.pageId}", true)
                )
                click = {
                    val player = it.player
                    player.soundClick()
                    player.openInventory(backPageInv)
                }
            }
            inv.setItem(
                backButton.effectiveSlot,
                backButton.icon
            )
            page.backPageButton = backButton
            page.buttons.add(backButton)
        }
        if (page.hasNextPage) {
            val nextPageInv = page.nextPage?.inventory ?: error("menu next page/inventory is null")
            val nextButton = MenuButton("next-page").apply {
                autoEffectiveSlot = 8
                fixed = true
                icon = nextPageButtonItem.clone().name(
                    nextPageButtonItem.getName().replace("%page%", "${page.nextPage!!.pageId}", true)
                )
                click = {
                    val player = it.player
                    player.soundClick()
                    player.openInventory(nextPageInv)
                }
            }
            inv.setItem(
                nextButton.effectiveSlot,
                nextButton.icon
            )
            page.nextPageButton = nextButton
            page.buttons.add(nextButton)
        }
    }

    open fun open(player: Player, pageToOpen: Int): Inventory {
        update(player) // Rebuilds menu
        if (pageToOpen < 1) error("cannot open a menu; the page must be higher than 0")
        if (!isAutoAlignItems && pageToOpen != 1) error("cannot open a non-autoAlignItems menu with a page different than 1")
        if (pageToOpen > 1 && inventories[pageToOpen] == null) error("the required page $pageToOpen is not registered; pages size: ${pages.size}")
        autoAlignSkipLines.forEach {
            if (it != 1 && it != 2 && it != 3 && it != 4 && it != 5 && it != 6) error("menu autoAlignSkipLines can't constains any int different than 1, 2, 3, 4, 5 and 6")
            if (it > lineAmount) error("this menu just have $lineAmount lines, can't apply rule to skip line $it")
        }

        for (page in pages) {
            page.buttons.clear()
            page.inventory = null
        }
        pages.clear()
        inventories.clear()

        if (isAutoAlignItems) {
            var lastInv: Inventory =
                Bukkit.createInventory(
                    null,
                    9 * lineAmount,
                    title.replace("%page%", pages.size.plus(1).toString(), true)
                )
            var lastPage = MenuPage()
            lastPage.pageId = pages.size.plus(1)
            lastPage.inventory = lastInv
            pages.add(lastPage)
            inventories[pages.size] = lastInv
            var lastSlot = 0
            var buttonId = 1

            for (button in buttonsToRegister.filter { !it.fixed }) {
                // Mine.broadcast("autoAlignPerPage: $autoAlignPerPage")
                if (lastPage.buttons.filter { !it.fixed }.size >= autoAlignPerPage) {
                    lastInv =
                        Bukkit.createInventory(
                            null,
                            9 * lineAmount,
                            title.replace("%page%", pages.size.plus(1).toString(), true)
                        )
                    val lp = lastPage
                    lastPage.hasNextPage = true
                    lastPage = MenuPage()
                    lastPage.pageId = pages.size.plus(1)
                    lastPage.inventory = lastInv
                    lastPage.hasBackPage = true
                    lastPage.backPage = lp
                    lp.nextPage = lastPage
                    pages.add(lastPage)
                    inventories[pages.size] = lastInv
                    lastSlot = 0 // reset count
                    buttonId = 1 // reset count
                    //Mine.broadcast("nova page sendo criada")
                }
                if (button.icon != null) {
                    var buttonSlot = button.effectiveSlot
                    if (autoAlignSkipLines.isNotEmpty()) {
                        if (lastSlot == 0) {
                            //Mine.broadcast("slot = 1, trabalhando nele")
                            var lastSkip = 0
                            skip@ for (skip in autoAlignSkipLines) {
                                if (lastSkip.plus(1) != skip) break@skip
                                lastSlot += if (autoAlignIgnoreColumns && lastSlot == 1) 10 else 9
                                lastSkip++
                            }
                            // Mine.broadcast("slot agora é: ")
                        }
                    } else if (lastSlot == 0 && autoAlignIgnoreColumns) {
                        lastSlot = 1
                    }
                    if (lastSlot.plus(1) < 9 * lineAmount) {
                        lastSlot++
                        // Mine.broadcast("subindo slot: $lastSlot")
                    } else {
                        //Mine.broadcast("não ta subindo slot; valor menor")
                    }
                    button.menuId = buttonId
                    val idToVerify = buttonId.minus(1)
                    if (autoAlignIgnoreColumns && idToVerify != 0 &&
                        idToVerify != 1 &&
                        idToVerify.isMultOf(7) && lastSlot.plus(1) < 9 * lineAmount
                    ) {
                        lastSlot += 2
                        //  Mine.broadcast("colocando +2, é múltiplo de 7; id: ${button.menuId}")
                    }
                    buttonSlot = lastSlot
                    button.autoEffectiveSlot = buttonSlot
                    buttonId++
                    lastInv.setItem(
                        buttonSlot,
                        button.icon
                    ); //Mine.broadcast("setando item no inventário, slot $buttonSlot; id: ${button.menuId}")
                    lastPage.buttons.add(button)
                }
            }
            pages.forEach { menuPage ->
                invokePageNextAndBackButtons(menuPage)
                for (fixedButton in buttonsToRegister.filter { it.fixed }) {
                    menuPage.inventory!!.setItem(fixedButton.effectiveSlot, fixedButton.icon)
                    menuPage.buttons.add(fixedButton)
                }
            }
            buttonsToRegister.clear()
            val choosenInv = inventories[pageToOpen] ?: error("cannot open page $pageToOpen; pages size: ${pages.size}")
            player.openInventory(choosenInv)
            return choosenInv
        } else {
            val singlePage = MenuPage()
            val pageInventory =
                inventories.getOrDefault(
                    pageToOpen,
                    Bukkit.createInventory(
                        null,
                        9 * lineAmount,
                        title.replace("%page%", pages.size.plus(1).toString(), true)
                    )
                )
            pageInventory.clear()
            singlePage.inventory = pageInventory
            inventories[pageToOpen] = pageInventory
            pages.add(singlePage)

            for (button in buttonsToRegister) {
                if (button.icon != null) {
                    pageInventory.setItem(button.effectiveSlot, button.icon)
                    singlePage.buttons.add(button)
                }
            }
            buttonsToRegister.clear()
            player.openInventory(pageInventory)
            return pageInventory
        }
    }

    open fun open(player: Player): Inventory {
        return open(player, 1)
    }

    open fun button(buttonName: String? = null, setup: MenuButton.() -> Unit): MenuButton {
        val newButton = if (buttonName != null) MenuButton(buttonName) else MenuButton()
        newButton.setup()
        buttonsToRegister.add(newButton)
        return newButton
    }

// Listeners - Start
    /**
     * [PlayerQuitEvent] is now on [GeneralListener].
     */

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInvClose(e: InventoryCloseEvent) {
        if (e.player !is Player) return
        if (pages.firstOrNull { e.inventory == it.inventory!! } == null) return
        val player = e.player as Player
        MenuSystem.openedMenu.remove(player)
        MenuSystem.openedPage.remove(player)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onInvClick(e: InventoryClickEvent) {
        if (e.clickedInventory == null) return
        val clickedPage = pages.firstOrNull { e.clickedInventory == it.inventory!! } ?: return
        val button = clickedPage.buttons.firstOrNull { e.slot == it.effectiveSlot } ?: return
        button.click.invoke(e)
        e.isCancelled = true
    }
// Listeners - End

}