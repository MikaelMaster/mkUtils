package com.mikael.mkutils.api.redis

import net.eduard.api.lib.plugin.IPluginInstance
import net.eduard.api.server.hasAPI
import redis.clients.jedis.Connection
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

object RedisAPI {

    /**
     * RedisAPI v1.3 (Using Jedis v4.2.3)
     *
     * Remember that in the mkUtils config you can enable this API automatically by setting Redis 'isEnabled' to true.
     * This API is not yet supported to be activated and used in different plugins at the same time with different Clients.
     * You should always create a single Redis Client ([createClient]), connect it ([connectClient]), and use it in all your plugins.
     *
     * @author Mikael
     * @see RedisConnectionData
     */

    var useToSyncBungeePlayers: Boolean = false
    lateinit var managerData: RedisConnectionData
    var client: Jedis? = null
    var clientConnection: Connection? = null
    var usedRedisConnectionData: RedisConnectionData? = null

    /**
     * Creates a new Redis client (Jedis) using the data provided by [RedisConnectionData].
     * After creation sets the [client] variable to the new created client and [usedRedisConnectionData] to the provided [RedisConnectionData].
     *
     * @param connectionData A [RedisConnectionData] to create the Redis Client.
     * @return A Redis Client ([Jedis]).
     * @throws IllegalStateException if the '[RedisConnectionData.isEnabled]' of the given [RedisConnectionData] is false.
     * @see connectClient
     */
    fun createClient(connectionData: RedisConnectionData): Jedis {
        if (!connectionData.isEnabled) error("RedisConnectionData isEnabled must not be false")
        val jedis = Jedis("http://${connectionData.host}:${connectionData.port}/")
        usedRedisConnectionData = connectionData
        client = jedis
        return client!!
    }

    /**
     * Connects the Redis [client] (Jedis) if it is not already connected.
     * If the client is already connected, it will just return the existing connection.
     *
     * @param force if is to force a new connection, ignoring if the client is already connected.
     * @return An existing Jedis [Connection].
     * @throws IllegalStateException if the [RedisAPI.client] is null.
     * @see createClient
     */
    fun connectClient(force: Boolean = false): Connection {
        if (client == null) error("Cannot get redis client (Jedis)")
        if (usedRedisConnectionData == null) error("Cannot get RedisConnectionData to connect the client")
        if (!force) {
            if (clientConnection != null) {
                return clientConnection!!
            }
        }
        if (usedRedisConnectionData!!.usePass) {
            client!!.auth(usedRedisConnectionData!!.pass)
        }
        client!!.connect()
        clientConnection = client!!.connection
        return clientConnection!!
    }

    /**
     * Closes the existing connection to the Redis server, and then sets the variable [clientConnection], [client] and [usedRedisConnectionData] to null.
     */
    fun finishConnection() {
        clientConnection?.close()
        clientConnection = null
        client = null
        usedRedisConnectionData = null
    }

    /**
     * Checks if the Redis [client] (Jedis) is created and connected.
     *
     * @return True if the redis client is created and connected. Otherwise, false.
     */
    fun isInitialized(): Boolean {
        if (client == null || clientConnection == null) return false
        return true
    }

    /**
     * Verify if a data existis on the Redis server.
     *
     * @param key the key to verify if the value existis.
     * @return True if the data existis. Otherwise, false.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     */
    fun existis(key: String): Boolean {
        if (!isInitialized()) error("Cannot insert any data to a null redis server")
        return client!!.exists(key)
    }

    /**
     * Inserts a data into redis server using the given [key] and [value].
     *
     * @param plugin the plugin instance owner of the data.
     * @param key the key to push the value.
     * @param value the value to be pushed into redis server. The value will always be converted to string.
     * @return True if the insert was completed. Otherwise, false.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     */
    fun insert(plugin: IPluginInstance, key: String, value: Any): Boolean {
        if (!isInitialized()) error("Cannot insert any data to a null redis server")
        return try {
            client!!.set("${plugin.systemName}:${key}", value.toString())
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    /**
     * Inserts a String List into the redis server using the given [key] and [stringList].
     *
     * @param plugin the plugin instance owner of the data.
     * @param key the key to push the value.
     * @param stringList the String List to be pushed into redis server.
     * @param useExistingData if is to use the existing redis list data. If you want to send this list with just the given `stringList`, mark this as false.
     * @return True if the insert was completed. Otherwise, false.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     */
    fun insertStringList(
        plugin: IPluginInstance,
        key: String,
        stringList: MutableList<String>,
        useExistingData: Boolean = true
    ): Boolean {
        if (!isInitialized()) error("Cannot insert any data to a null redis server")
        return try {
            if (useExistingData) {
                if (existis("${plugin.systemName}:${key}")) {
                    for (string in getStringList(plugin, key)) {
                        stringList.add(string)
                    }
                }
            }
            val stringBuilder = StringBuilder()
            for (string in stringList) {
                stringBuilder.append("${string};")
            }
            client!!.set("${plugin.systemName}:${key}", stringBuilder.toString())
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    /**
     * Inserts a String List into the redis server using the given [key] and [stringList].
     *
     * @param pluginName the plugin name owner of the data.
     * @param key the key to push the value.
     * @param stringList the String List to be pushed into redis server.
     * @param useExistingData if is to use the existing redis list data. If you want to send this list with just the given `stringList`, mark this as false.
     * @return True if the insert was completed. Otherwise, false.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     */
    fun insertStringList(
        pluginName: String,
        key: String,
        stringList: MutableList<String>,
        useExistingData: Boolean = true
    ): Boolean {
        if (!isInitialized()) error("Cannot insert any data to a null redis server")
        return try {
            if (useExistingData) {
                if (existis("${pluginName}:${key}")) {
                    for (string in getStringList(pluginName, key)) {
                        stringList.add(string)
                    }
                }
            }
            val stringBuilder = StringBuilder()
            for (string in stringList) {
                stringBuilder.append("${string};")
            }
            client!!.set("${pluginName}:${key}", stringBuilder.toString())
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    /**
     * Returns a String from redis server using the given [key].
     *
     * @param plugin the plugin instance owner of the data.
     * @param key the key to search on redis server for a data.
     * @return A String from the redis server.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     * @throws NullPointerException if the data retorned is null.
     */
    fun getString(plugin: IPluginInstance, key: String): String {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return client!!.get("${plugin.systemName}:${key}")
    }

    /**
     * Returns a String List from redis server using the given [key].
     *
     * @param plugin the plugin instance owner of the data.
     * @param key the key to search on redis server for a data.
     * @return A String List from the redis server.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     * @throws NullPointerException if the data retorned is null.
     */
    fun getStringList(plugin: IPluginInstance, key: String): List<String> {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        if (!existis("${plugin.systemName}:${key}")) return emptyList()
        return client!!.get("${plugin.systemName}:${key}").split(";")
    }

    /**
     * Returns a String List from redis server using the given [key].
     *
     * @param pluginName the plugin name owner of the data.
     * @param key the key to search on redis server for a data.
     * @return A String List from the redis server.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     * @throws NullPointerException if the data retorned is null.
     */
    fun getStringList(pluginName: String, key: String): List<String> {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        if (!existis("${pluginName}:${key}")) return emptyList()
        return client!!.get("${pluginName}:${key}").split(";")
    }

    /**
     * Returns a Int from redis server using the given [key].
     *
     * @param plugin the plugin instance owner of the data.
     * @param key the key to search on redis server for a data.
     * @return A Int from the redis server.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     * @throws NumberFormatException if the retorned data is null or is not a Int.
     */
    fun getInt(plugin: IPluginInstance, key: String): Int {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return client!!.get("${plugin.systemName}:${key}").toInt()
    }

    /**
     * Returns a Double from redis server using the given [key].
     *
     * @param plugin the plugin instance owner of the data.
     * @param key the key to search on redis server for a data.
     * @return A Double from the redis server.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     * @throws NumberFormatException if the retorned data is null or is not a Double.
     */
    fun getDouble(plugin: IPluginInstance, key: String): Double {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return client!!.get("${plugin.systemName}:${key}").toDouble()
    }

    /**
     * Returns a Long from redis server using the given [key].
     *
     * @param plugin the plugin instance owner of the data.
     * @param key the key to search on redis server for a data.
     * @return A Long from the redis server.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     * @throws NumberFormatException if the retorned data is null or is not a Long.
     */
    fun getLong(plugin: IPluginInstance, key: String): Long {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return client!!.get("${plugin.systemName}:${key}").toLong()
    }

    /**
     * Updates a counter value on Redis server.
     *
     * @param plugin the plugin instance owner of the data.
     * @param key the key to search on redis server for a data.
     * @return True if the counter update was completed. Otherwise, false.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     */
    fun updateCounter(plugin: IPluginInstance, key: String, newCount: Int): Boolean {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return try {
            if (!existis("${plugin.systemName}:${key}")) {
                client!!.set("${plugin.systemName}:${key}", newCount.toString())
            } else {
                val currentCount = getInt(plugin, "${plugin.systemName}:${key}")
                client!!.set("${plugin.systemName}:${key}", currentCount.plus(newCount).toString())
            }
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    /**
     * Updates a counter value on Redis server.
     * This fun can be useful if you don't have acess to the plugin instance owner of the data.
     *
     * @param pluginName the name of the plugin owner of the data. (Not in LowerCase)
     * @param key the key to search on redis server for a data.
     * @param countToSum the value (Int) to sum on Redis server.
     * @return True if the counter update was completed. Otherwise, false.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     */
    fun updateCounter(pluginName: String, key: String, countToSum: Int): Boolean {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return try {
            if (!existis("${pluginName}:${key}")) {
                client!!.set("${pluginName}:${key}", countToSum.toString())
            } else {
                val currentCount = client!!.get("${pluginName}:${key}").toInt()
                client!!.set("${pluginName}:${key}", currentCount.plus(countToSum).toString())
            }
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    /**
     * Sends a ping to Redis server.
     *
     * @return True if the ping is answered. Otherwise, false.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     */
    fun testPing(): Boolean {
        if (!isInitialized()) error("Cannot send ping to a null redis server")
        return try {
            client!!.ping()
            true
        } catch (ex: Exception) {
            false
        }
    }

    /**
     * Send an 'event' to Redis server.
     *
     * @param channel the redis channel name to send the event.
     * @param message the message that will be sent with the event.
     * @return True if the event sent was completed. Otherwise, false.
     * @throws IllegalStateException if the Redis [client] or the [clientConnection] is null.
     */
    // (Un)deprecated since mkUtils v1.1-final.
    // @Deprecated("Deprecated since mkUtils v1.1; Use your own method instead.", ReplaceWith("client!!.publish(channel, message)"))
    fun sendEvent(channel: String, message: String): Boolean {
        if (!isInitialized()) error("Cannot send an event message to a null redis server")
        return try {
            client!!.publish(channel, message)
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

}