package com.mikael.mkutils.spigot.api.craftapi

import com.mikael.mkutils.spigot.api.soundClick
import com.mikael.mkutils.spigot.api.soundPickup
import net.eduard.api.lib.game.ItemBuilder
import net.eduard.api.lib.game.SoundEffect
import net.eduard.api.lib.kotlin.mineAddLore
import net.eduard.api.lib.kotlin.player
import net.eduard.api.lib.menu.ClickEffect
import net.eduard.api.lib.menu.Menu
import org.bukkit.Material
import org.bukkit.Sound

class CustomRecipesMenu : Menu("Custom Crafts", 6) {
    companion object {
        lateinit var instance: CustomRecipesMenu
    }

    init {
        instance = this@CustomRecipesMenu

        isAutoAlignItems = true
        autoAlignSkipLines = listOf(1, 5, 6)
        autoAlignSkipColumns = listOf(9, 1)
        autoAlignPerLine = 7
        autoAlignPerPage = 3 * autoAlignPerLine

        update()
    }

    override fun update() {
        removeAllButtons()

        nextPage.item = ItemBuilder(Material.ARROW)
            .name("§aPage %page")
        nextPage.setPosition(9, 3)
        nextPageSound = SoundEffect(Sound.BLOCK_LEVER_CLICK, 2f, 1f)

        previousPage.item = ItemBuilder(Material.ARROW)
            .name("§aPage %page")
        previousPage.setPosition(1, 3)
        previousPageSound = SoundEffect(Sound.BLOCK_LEVER_CLICK, 2f, 1f)

        button("close") {
            setPosition(5, 6)

            fixed = true
            icon = ItemBuilder(Material.RED_DYE)
                .name("§cClose")
            click = ClickEffect {
                val player = it.player
                player.soundClick()
                player.closeInventory()
            }
        }

        if (CraftAPI.getCustomRecipes().isEmpty()) {
            button("empty") {
                setPosition(5, 3)

                icon = ItemBuilder(Material.COBWEB)
                    .name("§cEmpty")
                    .lore("§7None custom craft have been created yet.")
                click = ClickEffect {
                    it.player.soundPickup()
                }
            }
            return
        }

        for (recipe in CraftAPI.getCustomRecipes()) {
            button("craft-${recipe.keyName}") {
                iconPerPlayer = {
                    val item = recipe.result.clone()
                    item.mineAddLore(
                        "",
                        "§8Custom craft ID: ${recipe.keyName}",
                        "",
                        "§aClick to see the recipe of this item."
                    )
                    item
                }
                click = ClickEffect {
                    val player = it.player
                    player.soundClick()
                    SeeCustomRecipeMenu.getMenu(recipe).update()
                    SeeCustomRecipeMenu.getMenu(recipe).open(player)
                }
            }
        }
    }

}
