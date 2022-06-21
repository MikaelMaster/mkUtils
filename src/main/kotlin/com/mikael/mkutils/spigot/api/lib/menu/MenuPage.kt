package com.mikael.mkutils.spigot.api.lib.menu

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

open class MenuPage {

    var pageId = 1
    val buttons = mutableSetOf<MenuButton>()
    var inventory: Inventory? = null

    var backPageButton: MenuButton? = null
    var backPage: MenuPage? = null
    var hasBackPage = false

    var nextPageButton: MenuButton? = null
    var nextPage: MenuPage? = null
    var hasNextPage = false

}