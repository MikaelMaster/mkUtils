package com.mikael.mkutils.spigot.api.lib

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

class MineItem(material: Material = Material.STONE) : ItemStack(material) {

    /**
     * MineItem util class v1.0b
     *
     * This class is NOT finished to use.
     * Plase use ItemStack instead.
     *
     * @author Mikael
     * @see ItemStack
     */

    var skinURL: String? = null

    fun name(name: String? = null): MineItem {
        this.itemMeta!!.setDisplayName(name)
        return this
    }

    fun getName(): String {
        return if (hasItemMeta()) if (itemMeta!!.hasDisplayName()) itemMeta!!.displayName else "" else ""
    }

    fun lore(vararg lore: String): MineItem {
        this.itemMeta!!.lore = lore.toList()
        return this
    }

    fun getLore(): List<String> {
        return if (hasItemMeta()) if (itemMeta!!.hasLore()) itemMeta!!.lore!! else emptyList() else emptyList()
    }

    fun skull(skullName: String): MineItem {
        this.type = Material.PLAYER_HEAD
        val meta = this.itemMeta as SkullMeta?
        meta!!.owner = skullName
        this.itemMeta = meta
        return this
    }

    fun texture(textureBase64: String): MineItem {
        this.type = Material.PLAYER_HEAD
        val newItemMeta = this.itemMeta as SkullMeta
        val profile = GameProfile(UUID.randomUUID(), null as String?)
        profile.properties.put("textures", Property("textures", textureBase64))
        try {
            val profileField = newItemMeta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField[newItemMeta] = profile
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        this.itemMeta = newItemMeta
        return this
    }

    fun skin(skinUrl: String): MineItem {
        this.skinURL = skinUrl
        return this.texture(
            Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl).toByteArray())
                .toString()
        )
    }

    fun amount(amount: Int): MineItem {
        this.amount = amount
        return this
    }

    fun type(material: Material): MineItem {
        this.type = material
        return this
    }

}