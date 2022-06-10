package com.mikael.mkutils.spigot.api.craftapi

import com.mikael.mkutils.spigot.UtilsMain
import net.eduard.api.lib.game.ItemBuilder
import net.eduard.api.lib.plugin.IPluginInstance
import org.bukkit.NamespacedKey
import org.bukkit.inventory.RecipeChoice.ExactChoice
import org.bukkit.inventory.ShapedRecipe

object CraftAPI {

    private val recipes = mutableListOf<CraftRecipe>()

    fun registerCustomRecipe(plugin: IPluginInstance, recipe: CraftRecipe): Boolean {
        val craft = ShapedRecipe(
            NamespacedKey.fromString("${plugin.systemName.lowercase()}:${recipe.keyName.lowercase()}")!!,
            ItemBuilder(recipe.result)
        )
        if (recipe.type == CraftRecipeType.PLAYER_CRAFT) {
            if (recipe.recipeItems.size != 4) error("Cannot register a custom recipe using PLAYER_CRAFT type with recipe items size different of 4")
            craft.shape("12", "34")
        } else {
            if (recipe.recipeItems.size != 9) error("Cannot register a custom recipe using CRAFTING_TABLE type with recipe items size different of 9")
            craft.shape("123", "456", "789")
        }
        recipe.slot1Item?.let { craft.setIngredient('1', ExactChoice(it)) }
        recipe.slot2Item?.let { craft.setIngredient('2', ExactChoice(it)) }
        recipe.slot3Item?.let { craft.setIngredient('3', ExactChoice(it)) }
        recipe.slot4Item?.let { craft.setIngredient('4', ExactChoice(it)) }
        if (recipe.type == CraftRecipeType.CRAFTING_TABLE) {
            recipe.slot5Item?.let { craft.setIngredient('5', ExactChoice(it)) }
            recipe.slot6Item?.let { craft.setIngredient('6', ExactChoice(it)) }
            recipe.slot7Item?.let { craft.setIngredient('7', ExactChoice(it)) }
            recipe.slot8Item?.let { craft.setIngredient('8', ExactChoice(it)) }
            recipe.slot9Item?.let { craft.setIngredient('9', ExactChoice(it)) }
        }
        UtilsMain.instance.server.addRecipe(craft)
        recipes.add(recipe)
        return true
    }

    fun getCustomRecipes(): MutableList<CraftRecipe> {
        return recipes
    }

    fun onEnable() {
        recipes.clear()

        // See Custom Crafts
        if (UtilsMain.instance.config.getBoolean("CustomCrafts.customCraftsMenuAndCommand")) {
            CustomCraftsCommand().registerCommand(UtilsMain.instance)
            CustomRecipesMenu().registerMenu(UtilsMain.instance)
        }
    }

    fun onDisable() {
        recipes.clear()
    }

}