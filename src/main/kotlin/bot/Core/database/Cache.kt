package bot.Core.database

import bot.utils.RedissonNameMapper
import org.redisson.Redisson
import org.redisson.api.*
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class Cache {
    private val logger = LoggerFactory.getLogger(Cache::class.java)

    private val redissonClient: RedissonClient

    val userMap: RMapCacheAsync<String, String>
    val settingMap: RMapCacheAsync<String, String>

    init {
        val nameMapper = RedissonNameMapper(System.getenv("REDIS_NAME_MAPPER"))
        val clusterServersData = System.getenv("REDIS_CLUSTERS")
        val sentinelAddressesData = System.getenv("REDIS_SENTINEL_ADDRESSES")
        val sentinelMasterName = System.getenv("REDIS_SENTINEL_MASTER_NAME")
        val redisUrl = System.getenv("REDIS_URL")
        val redisPassword = System.getenv("REDIS_PASSWORD")

        val config = Config()
        if (!clusterServersData.isNullOrEmpty()) {
            config
                .useClusterServers()
                .addNodeAddress(*clusterServersData.split(",").toTypedArray())
                .setPassword(redisPassword)
                .nameMapper = nameMapper
        } else if (!sentinelAddressesData.isNullOrEmpty() && !sentinelMasterName.isNullOrEmpty()) {
            config.useSentinelServers()
                .setMasterName(sentinelMasterName)
                .addSentinelAddress(*sentinelAddressesData.split(",").toTypedArray())
                .setPassword(redisPassword)
                .nameMapper = nameMapper
        } else if (!redisUrl.isNullOrEmpty()) {
            config.useSingleServer()
                .setAddress(redisUrl)
                .setPassword(redisPassword)
                .nameMapper = nameMapper
        } else {
            throw Exception("Redis configuration not found in environment variables. Please configure Redis first.")
        }
        try {
            redissonClient = Redisson.create(config)
        } catch (e: Exception) {
            logger.error("Failed to create redisson client", e)
            exitProcess(1)
        }

        userMap = redissonClient.getMapCache("user")
        settingMap = redissonClient.getMapCache("setting")
    }

    fun getUserLock(userId: String): RLock {
        return redissonClient.getFairLock("user_lock-${userId}")
    }

    fun shutdown() {
        redissonClient.shutdown()
    }
}
