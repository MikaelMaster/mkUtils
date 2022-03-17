package com.mikael.mkutils.api

import com.mikael.mkutils.api.redis.RedisAPI
import net.eduard.api.lib.kotlin.resolve
import net.md_5.bungee.api.chat.TextComponent
import java.text.NumberFormat
import java.util.*

val utilsmanager = resolve<UtilsManager>()
val Redis = RedisAPI

fun String.toTextComponent(): TextComponent {
    return TextComponent(this)
}

fun Double.formatEN(): String {
    return NumberFormat.getNumberInstance(Locale.US).format(this)
}

fun Int.formatEN(): String {
    return this.toDouble().formatEN()
}