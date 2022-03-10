package com.mikael.mkutils.api.redis

import redis.clients.jedis.Connection
import redis.clients.jedis.Jedis

object RedisAPI {

    /**
     * RedisAPI v1.2 (Using Jedis v4.1.1)
     *
     * Remember that in the mkUtils config you can enable this API automatically by setting Redis 'isEnabled' to true.
     * This API is not yet supported to be activated and used in different plugins at the same time with different Clients.
     * You should always create a single Redis Client and use it in all your plugins.
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
     * Creates a new redis client (Jedis) using the data provided by RedisConnectionData.
     * After creation sets the 'client' variable to the new created client and 'usedRedisConnectionData' to the used RedisConnectionData.
     *
     * @param connectionData A RedisConnectionData to create the Redis Client.
     * @return A redis client (Jedis).
     * @throws IllegalStateException if the 'isEnabled' of the given RedisConnectionData is false.
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
     * Connects the client (Jedis) if it is not already connected.
     * If the client is already connected, it will just return the existing connection.
     *
     * @param force if is to force a new connection, ignoring if the client is already connected.
     * @return An existing Jedis connection.
     * @throws IllegalStateException if the RedisAPI client is null.
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
     * Closes the existing connection to the redis server, and then sets the variable 'clientConnection', 'client' and 'usedRedisConnectionData' to null.
     */
    fun finishConnection() {
        clientConnection?.close()
        clientConnection = null
        client = null
        usedRedisConnectionData = null
    }

    /**
     * Checks if the redis client (Jedis) is created and connected.
     *
     * @return True if the redis client is created and connected. Otherwise, false.
     */
    fun isInitialized(): Boolean {
        if (client == null || clientConnection == null) return false
        return true
    }

    /**
     * Inserts a data into redis server using the given Key and Value.
     *
     * @param key the key to push the value.
     * @param value the value to be pushed into redis server. The value will always be converted to string.
     * @return True if the insert was completed. Otherwise, false.
     * @throws IllegalStateException if the Redis client or the connection is null.
     */
    fun insert(key: String, value: Any): Boolean {
        if (!isInitialized()) error("Cannot insert any data to a null redis server")
        return try {
            client!!.set(key, value.toString())
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    /**
     * Returns a String from redis server using the given Key.
     *
     * @param key the key to search on redis server for a data.
     * @return A String from the redis server.
     * @throws IllegalStateException if the Redis client or the connection is null.
     * @throws NullPointerException if the data retorned is null.
     */
    fun getString(key: String): String {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return client!!.get(key)
    }

    /**
     * Returns a Int from redis server using the given Key.
     *
     * @param key the key to search on redis server for a data.
     * @return A Int from the redis server.
     * @throws IllegalStateException if the Redis client or the connection is null.
     * @throws NumberFormatException if the retorned data is null or is not a Int.
     */
    fun getInt(key: String): Int {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return client!!.get(key).toInt()
    }

    /**
     * Returns a Double from redis server using the given Key.
     *
     * @param key the key to search on redis server for a data.
     * @return A Double from the redis server.
     * @throws IllegalStateException if the Redis client or the connection is null.
     * @throws NumberFormatException if the retorned data is null or is not a Double.
     */
    fun getDouble(key: String): Double {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return client!!.get(key).toDouble()
    }

    /**
     * Returns a Long from redis server using the given Key.
     *
     * @param key the key to search on redis server for a data.
     * @return A Long from the redis server.
     * @throws IllegalStateException if the Redis client or the connection is null.
     * @throws NumberFormatException if the retorned data is null or is not a Long.
     */
    fun getLong(key: String): Long {
        if (!isInitialized()) error("Cannot get any data from a null redis server")
        return client!!.get(key).toLong()
    }

    /**
     * Sends a ping to Redis server.
     *
     * @return True if the ping is answered. Otherwise, false.
     * @throws IllegalStateException if the Redis client or the connection is null.
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
     * @throws IllegalStateException if the Redis client or the connection is null.
     */
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