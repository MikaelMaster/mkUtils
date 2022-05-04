package com.mikael.mkutils.api

import com.mikael.mkutils.api.redis.RedisAPI
import net.eduard.api.lib.kotlin.resolve
import net.md_5.bungee.api.chat.TextComponent
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

val utilsmanager = resolve<UtilsManager>()

/**
 * @see RedisAPI
 */
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

fun Long.formatDuration(): String {
    return if (this <= 0L) {
        "-1"
    } else {
        val day = TimeUnit.MILLISECONDS.toDays(this)
        val hours = TimeUnit.MILLISECONDS.toHours(this) - day * 24L
        val minutes = TimeUnit.MILLISECONDS.toMinutes(this) - TimeUnit.MILLISECONDS.toHours(this) * 60L
        val seconds = TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MILLISECONDS.toMinutes(this) * 60L
        val stringBuilder = StringBuilder()
        if (day > 0L) {
            stringBuilder.append(day).append("d")
            if (minutes > 0L || seconds > 0L || hours > 0L) {
                stringBuilder.append(" ")
            }
        }
        if (hours > 0L) {
            stringBuilder.append(hours).append("h")
            if (minutes > 0L || seconds > 0L) {
                stringBuilder.append(" ")
            }
        }
        if (minutes > 0L) {
            stringBuilder.append(minutes).append("m")
            if (seconds > 0L) {
                stringBuilder.append(" ")
            }
        }
        if (seconds > 0L) {
            stringBuilder.append(seconds).append("s")
        }
        val formatedTime = stringBuilder.toString()
        if (formatedTime.isEmpty()) "-1" else formatedTime
    }
}