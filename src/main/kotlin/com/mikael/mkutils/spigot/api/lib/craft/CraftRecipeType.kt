package com.mikael.mkutils.spigot.api.lib.craft

enum class CraftRecipeType(var slots: Int) {

    /**
     * [PLAYER_CRAFT] - will always have 4 slots on the craft.
     *
     * [CRAFTING_TABLE] - will always have 9 slots on the craft.
     */

    PLAYER_CRAFT(4), CRAFTING_TABLE(9)

}