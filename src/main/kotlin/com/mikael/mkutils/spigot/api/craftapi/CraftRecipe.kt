package com.mikael.mkutils.spigot.api.craftapi

import org.bukkit.inventory.ItemStack

class CraftRecipe(
    var keyName: String,
    var type: CraftRecipeType,
    var result: ItemStack,
    var slot1Item: ItemStack? = null,
    var slot2Item: ItemStack? = null,
    var slot3Item: ItemStack? = null,
    var slot4Item: ItemStack? = null,
    /**
     * If the [type] is [CraftRecipeType.PLAYER_CRAFT], just ignore the 5, 6, 7, 8 and 9 slot item.
     */
    var slot5Item: ItemStack? = null,
    var slot6Item: ItemStack? = null,
    var slot7Item: ItemStack? = null,
    var slot8Item: ItemStack? = null,
    var slot9Item: ItemStack? = null
) {
    /**
     * Return all the [ItemStack]s needed to craft the item using the vars order ([slot1Item], [slot2Item], ...).
     *
     * If the [type] is [CraftRecipeType.PLAYER_CRAFT] the size of the retorned list
     * will always be 4, if the [CraftRecipeType.CRAFTING_TABLE] is used, the size will be 9.
     */
    val recipeItems: List<ItemStack?>
        get() {
            val items = mutableListOf<ItemStack?>()
            items.add(slot1Item)
            items.add(slot2Item)
            items.add(slot3Item)
            items.add(slot4Item)
            if (type == CraftRecipeType.CRAFTING_TABLE) {
                items.add(slot5Item)
                items.add(slot6Item)
                items.add(slot7Item)
                items.add(slot8Item)
                items.add(slot9Item)
            }
            return items
        }
}