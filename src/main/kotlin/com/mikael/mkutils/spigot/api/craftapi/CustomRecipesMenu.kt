package com.mikael.mkutils.spigot.api.craftapi

import com.mikael.mkutils.spigot.api.addLore
import com.mikael.mkutils.spigot.api.lib.MineItem
import com.mikael.mkutils.spigot.api.lib.menu.MineMenu
import com.mikael.mkutils.spigot.api.soundClick
import com.mikael.mkutils.spigot.api.soundPickup
import org.bukkit.Material
import org.bukkit.entity.Player

class CustomRecipesMenu : MineMenu("Custom Crafts", 6) {
    companion object {
        lateinit var instance: CustomRecipesMenu
    }

    init {
        instance = this@CustomRecipesMenu

        isAutoAlignItems = true
        autoAlignSkipLines = listOf(1, 5, 6)
    }

    override fun update(player: Player) {
        removeAllButtons()

        backPageButtonPosX = 0
        backPageButtonPosY = 3

        nextPageButtonPosX = 8
        nextPageButtonPosY = 3

        button("close") {
            setPosition(5, 6)

            fixed = true
            icon = MineItem(Material.RED_DYE).name("§cClose")
            click = {
                player.soundClick()
                player.closeInventory()
            }
        }

        if (CraftAPI.getCustomRecipes().isEmpty()) {
            button("empty") {
                setPosition(5, 3)

                fixed = true
                icon = MineItem(Material.COBWEB)
                    .name("§cEmpty")
                    .lore("§7None custom craft have been created yet.")
                click = {
                    player.soundPickup()
                }
            }
            return
        }

        for (recipe in CraftAPI.getCustomRecipes()) {
            button("craft-${recipe.keyName}") {

                val item = recipe.result.clone()
                item.addLore(
                    "",
                    "§8Custom craft ID: ${recipe.keyName}",
                    "",
                    "§aClick to see the recipe of this item."
                )
                icon = item
                click = {
                    player.soundClick()
                    SeeCustomRecipeMenu.getMenu(recipe).open(player)
                }
            }
        }
    }

}
