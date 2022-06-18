package com.mikael.mkutils.spigot.api.lib

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.potion.PotionEffect
import java.util.*

class MineItem(item: ItemStack) : ItemStack(item) {

    constructor(material: Material) : this(ItemStack(material))
    constructor(material: Material, amount: Int) : this(ItemStack(material, amount))

    /**
     * MineItem util class v1.0
     *
     * This class represents an [ItemStack].
     *
     * To create invoke it you can use:
     * - MineItem(item: [ItemStack])
     * - MineItem(material: [Material])
     * - MineItem(material: [Material], amount: [Int])
     *
     * @author Mikael
     * @see ItemStack
     */

    fun name(name: String? = null): MineItem {
        this.itemMeta!!.setDisplayName(name)
        return this
    }

    fun getName(): String {
        return if (this.hasItemMeta()) if (this.itemMeta!!.hasDisplayName()) this.itemMeta!!.displayName else "" else ""
    }

    fun lore(vararg lore: String): MineItem {
        this.itemMeta!!.lore = lore.toList()
        return this
    }

    fun addLore(vararg lines: String): MineItem {
        val meta = this.itemMeta!!
        if (meta.lore == null) meta.lore = listOf()
        val newLore = mutableListOf<String>()
        for (line in meta.lore!!) {
            newLore.add(line)
        }
        for (newLine in lines) {
            newLore.add(newLine)
        }
        itemMeta = meta
        return this
    }

    fun clearLore(): MineItem {
        this.itemMeta!!.lore = emptyList()
        return this
    }

    fun getLore(): List<String> {
        return if (this.hasItemMeta()) if (this.itemMeta!!.hasLore()) this.itemMeta!!.lore!! else emptyList() else emptyList()
    }

    fun type(material: Material): MineItem {
        this.type = material
        return this
    }

    fun amount(amount: Int): MineItem {
        this.amount = amount
        return this
    }

    fun addFlags(vararg flags: ItemFlag): MineItem {
        val meta = this.itemMeta
        meta!!.addItemFlags(*flags)
        this.itemMeta = meta
        return this
    }

    fun addAllFlags(): MineItem {
        val meta = this.itemMeta
        meta!!.addItemFlags(*ItemFlag.values())
        this.itemMeta = meta
        return this
    }

    fun removeFlag(flag: ItemFlag): MineItem {
        val meta = this.itemMeta
        meta!!.removeItemFlags(flag)
        this.itemMeta = meta
        return this
    }

    fun removeFlags(): MineItem {
        val meta = this.itemMeta
        meta!!.removeItemFlags(*ItemFlag.values())
        this.itemMeta = meta
        return this
    }

    fun addEnchant(enchant: Enchantment, level: Int): MineItem {
        this.addUnsafeEnchantment(enchant, level)
        return this
    }

    fun removeEnchant(enchant: Enchantment): MineItem {
        this.removeEnchantment(enchant)
        return this
    }

    fun clearEnchants(): MineItem {
        for (enchant in this.enchantments.keys) {
            this.removeEnchantment(enchant)
        }
        return this
    }

    override fun clone(): MineItem {
        return super.clone() as MineItem
    }

    // Change Custom Item Properties Functions

    fun color(color: Color): MineItem { // Change Item Color
        if (!this.type.name.contains("LEATHER")) {
            this.type = Material.LEATHER_CHESTPLATE
        }
        val meta = this.itemMeta as LeatherArmorMeta
        meta.setColor(color)
        this.itemMeta = meta
        return this
    }

    fun potion(effect: PotionEffect): MineItem { // Change Item PotionEffect
        if (this.type != Material.POTION) {
            this.type = Material.POTION
        }
        val meta = this.itemMeta as PotionMeta
        meta.setMainEffect(effect.type)
        meta.addCustomEffect(effect, true)
        this.itemMeta = meta
        return this
    }

    fun spawnerType(type: EntityType): MineItem {
        this.type = Material.SPAWNER
        val meta = this.itemMeta as BlockStateMeta
        val state = meta.blockState
        val spawner = state as CreatureSpawner
        spawner.spawnedType = type
        this.itemMeta = meta
        return this
    }

    private var skinURL: String? = null // Custom Skull Skin

    fun getSkinURL(): String? {
        return skinURL
    }

    fun skull(skullName: String): MineItem { // Custom Skull Name
        this.type = Material.PLAYER_HEAD
        val meta = this.itemMeta as SkullMeta
        meta.owner = skullName
        this.itemMeta = meta
        return this
    }

    private fun texture(textureBase64: String): MineItem { // Custom Skull Texture
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

    fun skin(skinUrl: String): MineItem { // Custom Skull Skin
        this.skinURL = skinUrl
        return this.texture(
            Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", skinUrl).toByteArray())
                .toString()
        )
    }

    fun skinId(skinId: String): MineItem { // Custom Skull Skin ID
        return this.skin("http://textures.minecraft.net/texture/${skinId}")
    }
}