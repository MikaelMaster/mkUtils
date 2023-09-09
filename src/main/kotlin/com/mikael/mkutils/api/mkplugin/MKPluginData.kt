package com.mikael.mkutils.api.mkplugin

import com.mikael.mkutils.api.UtilsManager
import net.eduard.api.lib.database.SQLManager
import net.eduard.api.lib.database.api.DatabaseElement

@Suppress("WARNINGS")
interface MKPluginData : DatabaseElement {

    override val sqlManager: SQLManager
        get() = UtilsManager.sqlManager

}