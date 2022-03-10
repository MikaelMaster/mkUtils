package com.mikael.mkutils.api.redis

class RedisConnectionData(

    /**
     * Data to create a redis client (Jedis) using the RedisAPI.
     *
     * @see RedisAPI
     */

    var isEnabled: Boolean = false,
    var syncBungeeDataUsingRedis: Boolean = false,
    var usePass: Boolean = false,
    var pass: String = "password",
    var port: Int = 6379,
    var host: String = "localhost"
)