package com.mikael.mkutils.spigot.api.storable

import com.mikael.mkutils.spigot.api.lib.MineItem
import net.eduard.api.lib.storage.Storable
import org.bukkit.Material

class MineItemStorable : Storable<MineItem> {

    override fun store(map: MutableMap<String, Any>, item: MineItem) {
        // finish it
    }

    override fun restore(map: MutableMap<String, Any>): MineItem {
        return MineItem(Material.BARRIER) // finish it
    }

}