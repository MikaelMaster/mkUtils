package com.mikael.mkutils.api

import com.mikael.mkutils.api.mkplugin.MKPluginData
import com.mikael.mkutils.api.redis.RedisAPI
import net.eduard.api.lib.kotlin.resolve
import net.md_5.bungee.api.chat.TextComponent
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * [UtilsManager] class shortcut.
 */
val utilsmanager = resolve<UtilsManager>()

/**
 * Key to sync MySql async and sync updates.
 * DO NOT USE IT BY YOURSELF IF YOU DO NOT KNOW WHAT YOU ARE DOING.
 * Instead, use [syncMysql] and give the block code that will be executed using this sync key.
 *
 * @see syncMysql
 */
val syncMysqUpdatesKey = Any()

/**
 * Use it to sync updates in mysql that interact with a local list/map to save a [MKPluginData].
 * You can call this function in a main or async thread, everything will be sync as the same.
 *
 * @param thing the block code to execute using the [syncMysqUpdatesKey].
 * @return True if the block code has been executed with no error. Otherwise, false.
 */
inline fun syncMysql(crossinline thing: (() -> Unit)): Boolean {
    synchronized(syncMysqUpdatesKey) {
        return try {
            thing.invoke()
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }
}

/**
 * [RedisAPI] class shortcut.
 *
 * @see RedisAPI
 */
val Redis = RedisAPI

/**
 * Transform a [String] into a [TextComponent].
 *
 * @return [TextComponent] with the given [String].
 */
fun String?.toTextComponent(): TextComponent {
    return if (this != null) {
        TextComponent(this)
    } else {
        TextComponent("")
    }
}

/**
 * Will return a [String] with "seconds" if the given [Int] is different from 1. Otherwise, it will return "second".
 *
 * @return [String] with "seconds" or "second".
 */
fun Int.formatSeccondWorld(): String {
    return if (this != 1) return "seconds" else "second"
}

fun Boolean.formatEnabledDisabled(colored: Boolean = true): String {
    val text = if (colored) {
        if (this) "§aEnabled" else "§cDisabled"
    } else {
        if (this) "Enabled" else "Disabled"
    }
    return text
}

fun Boolean.formatYesNo(colored: Boolean = true): String {
    val text = if (colored) {
        if (this) "§aYes" else "§cNo"
    } else {
        if (this) "Yes" else "No"
    }
    return text
}

fun Int.isMultOf(multBy: Int): Boolean {
    return this % multBy == 0
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
        formatedTime.ifEmpty { "-1" }
    }
}