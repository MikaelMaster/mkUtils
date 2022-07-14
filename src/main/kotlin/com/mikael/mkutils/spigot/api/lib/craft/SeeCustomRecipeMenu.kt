package com.mikael.mkutils.spigot.api.lib.craft

import com.mikael.mkutils.spigot.UtilsMain
import com.mikael.mkutils.spigot.api.lib.MineItem
import com.mikael.mkutils.spigot.api.lib.menu.MineMenu
import com.mikael.mkutils.spigot.api.soundClick
import com.mikael.mkutils.spigot.api.soundPickup
import org.bukkit.Material
import org.bukkit.entity.Player

class SeeCustomRecipeMenu(var recipe: CraftRecipe) : MineMenu("Custom Craft Recipe", 6) {
    companion object {
        lateinit var instance: SeeCustomRecipeMenu

        private val menus = mutableMapOf<CraftRecipe, SeeCustomRecipeMenu>()

        fun getMenu(recipe: CraftRecipe): SeeCustomRecipeMenu {
            if (menus.containsKey(recipe)) return menus[recipe]!!
            val newMenu = SeeCustomRecipeMenu(recipe)
            menus[recipe] = newMenu
            newMenu.registerMenu(UtilsMain.instance)
            return newMenu
        }
    }

    init {
        instance = this@SeeCustomRecipeMenu
    }

    override fun update(player: Player) {
        removeAllButtons(player)

        button("back") {
            setPosition(5, 6)

            icon = MineItem(Material.ARROW)
                .name("§cBack")
                .lore("§7To Custom Crafts.")
            click = click@{
                player.soundClick()
                CustomRecipesMenu.instance.open(player)
            }
        }


        // Craft Result
        button("recipe-result") {
            setPosition(7, 3)

            icon = recipe.result.clone()
            click = click@{
                player.soundPickup()
            }
        }

        // Recipes start

        button("recipe-item-1") {
            setPosition(3, 2)

            icon = recipe.slot1Item ?: MineItem(Material.AIR).name(" ")
            click = click@{
                player.soundPickup()
            }
        }
        button("recipe-item-2") {
            setPosition(4, 2)

            fixed = true
            icon = recipe.slot2Item ?: MineItem(Material.AIR).name(" ")
            click = click@{
                player.soundPickup()
            }
        }
        button("recipe-item-3") {
            setPosition(5, 2)

            icon = recipe.slot3Item ?: MineItem(Material.AIR).name(" ")
            click = click@{
                player.soundPickup()
            }
        }
        button("recipe-item-4") {
            setPosition(3, 3)

            icon = recipe.slot4Item ?: MineItem(Material.AIR).name(" ")
            click = click@{
                player.soundPickup()
            }
        }

        button("recipe-item-5") {
            setPosition(4, 3)

            icon = recipe.slot5Item ?: MineItem(Material.AIR).name(" ")
            click = click@{
                player.soundPickup()
            }
        }
        button("recipe-item-6") {
            setPosition(5, 3)

            fixed = true
            icon = recipe.slot6Item ?: MineItem(Material.AIR).name(" ")
            click = click@{
                player.soundPickup()
            }
        }
        button("recipe-item-7") {
            setPosition(3, 4)

            icon = recipe.slot7Item ?: MineItem(Material.AIR).name(" ")
            click = click@{
                player.soundPickup()
            }
        }
        button("recipe-item-8") {
            setPosition(4, 4)

            icon = recipe.slot8Item ?: MineItem(Material.AIR).name(" ")
            click = click@{
                player.soundPickup()
            }
        }
        button("recipe-item-9") {
            setPosition(5, 4)

            icon = recipe.slot9Item ?: MineItem(Material.AIR).name(" ")
            click = click@{
                player.soundPickup()
            }
        }

        // Recipes end

        x@ for (x in 1..9) {
            y@ for (y in 1..6) {
                if (buttonsToRegister.firstOrNull { it.positionX == x && it.positionY == y } != null) continue@y
                button("button-x${x}-y${y}") {
                    setPosition(x, y)

                    icon = MineItem(Material.BLACK_STAINED_GLASS_PANE)
                        .name(" ")
                }
            }
        }
    }

}
