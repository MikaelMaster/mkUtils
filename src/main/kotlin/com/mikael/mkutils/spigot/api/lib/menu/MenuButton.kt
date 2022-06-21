package com.mikael.mkutils.spigot.api.lib.menu

import com.mikael.mkutils.spigot.api.lib.MineItem
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

open class MenuButton(var name: String) {

    constructor() : this("null-name-button")

    var positionX = 1
    var positionY = 1
    var fixed = false

    var menuId = 0
    var autoEffectiveSlot: Int? = null
    val effectiveSlot: Int
        get() {
            if (autoEffectiveSlot != null) return autoEffectiveSlot!!
            val y = if (this.positionY > 1) this.positionY - 1 else this.positionY
            return y * 9 + positionX - 1
        }

    var icon: ItemStack? = MineItem(Material.BARRIER)
    var click: (InventoryClickEvent) -> Unit = {}

    open fun setPosition(x: Int, y: Int) {
        positionX = x
        positionY = y
    }
}