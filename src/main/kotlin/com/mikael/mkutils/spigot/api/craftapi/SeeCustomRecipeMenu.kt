package com.mikael.mkutils.spigot.api.craftapi

import com.mikael.mkutils.spigot.UtilsMain
import com.mikael.mkutils.spigot.api.soundClick
import com.mikael.mkutils.spigot.api.soundPickup
import net.eduard.api.lib.game.ItemBuilder
import net.eduard.api.lib.kotlin.mineName
import net.eduard.api.lib.kotlin.player
import net.eduard.api.lib.menu.ClickEffect
import net.eduard.api.lib.menu.Menu
import org.bukkit.Material

class SeeCustomRecipeMenu(var recipe: CraftRecipe) : Menu("Custom Craft Recipe", 6) {
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
        update()
    }

    override fun update() {
        removeAllButtons()

        button("back") {
            setPosition(5, 6)

            icon = ItemBuilder(Material.ARROW)
                .name("§cBack")
                .lore("§7To Custom Crafts.")
            click = ClickEffect {
                val player = it.player
                player.soundClick()
                CustomRecipesMenu.instance.update()
                CustomRecipesMenu.instance.open(player)
            }
        }


        // Craft Result
        button("recipe-result") {
            setPosition(7, 3)

            icon = recipe.result.clone()
            click = ClickEffect {
                it.player.soundPickup()
            }
        }

        // Recipes start

        button("recipe-item-1") {
            setPosition(3, 2)

            icon = recipe.slot1Item ?: ItemBuilder(Material.AIR).name(" ")
            click = ClickEffect {
                it.player.soundPickup()
            }
        }
        button("recipe-item-2") {
            setPosition(4, 2)

            fixed = true
            icon = recipe.slot2Item ?: ItemBuilder(Material.AIR).name(" ")
            click = ClickEffect {
                it.player.soundPickup()
            }
        }
        button("recipe-item-3") {
            setPosition(5, 2)

            icon = recipe.slot3Item ?: ItemBuilder(Material.AIR).name(" ")
            click = ClickEffect {
                it.player.soundPickup()
            }
        }
        button("recipe-item-4") {
            setPosition(3, 3)

            icon = recipe.slot4Item ?: ItemBuilder(Material.AIR).name(" ")
            click = ClickEffect {
                it.player.soundPickup()
            }
        }

        button("recipe-item-5") {
            setPosition(4, 3)

            icon = recipe.slot5Item ?: ItemBuilder(Material.AIR).name(" ")
            click = ClickEffect {
                it.player.soundPickup()
            }
        }
        button("recipe-item-6") {
            setPosition(5, 3)

            fixed = true
            icon = recipe.slot6Item ?: ItemBuilder(Material.AIR).name(" ")
            click = ClickEffect {
                it.player.soundPickup()
            }
        }
        button("recipe-item-7") {
            setPosition(3, 4)

            icon = recipe.slot7Item ?: ItemBuilder(Material.AIR).name(" ")
            click = ClickEffect {
                it.player.soundPickup()
            }
        }
        button("recipe-item-8") {
            setPosition(4, 4)

            icon = recipe.slot8Item ?: ItemBuilder(Material.AIR).name(" ")
            click = ClickEffect {
                it.player.soundPickup()
            }
        }
        button("recipe-item-9") {
            setPosition(5, 4)

            icon = recipe.slot9Item ?: ItemBuilder(Material.AIR).name(" ")
            click = ClickEffect {
                it.player.soundPickup()
            }
        }

        // Recipes end


        x@ for (x in 1..9) {
            y@ for (y in 1..6) {
                if (buttons.firstOrNull { it.positionX == x && it.positionY == y } != null) continue@y
                button {
                    setPosition(x, y)

                    icon = ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                        .name(" ")
                }
            }
        }
    }

}
