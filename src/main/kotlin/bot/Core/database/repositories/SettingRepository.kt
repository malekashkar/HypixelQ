package bot.Core.database.repositories

import bot.Core.database.Database
import bot.Core.database.models.Setting
import bot.Core.database.models.User
import bot.utils.Json
import bot.utils.extensions.awaitSuspending
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.or
import org.redisson.api.RMapCacheAsync

class SettingRepository(
    database: Database,
    private val collection: CoroutineCollection<Setting>,
    private val cacheMap: RMapCacheAsync<String, String>,
): Repository(database) {
    private suspend fun getCachedSetting(userId: String): Setting? {
        val json = cacheMap.getAsync(userId).awaitSuspending() ?: return null
        return Json.decodeFromString(json)
    }

    private suspend fun setCacheSetting(settings: Setting) {
        if (settings._isNew) {
            settings._isNew = false
            collection.insertOne(settings)
        }
        cacheMap.putAsync(settings.userId, Json.encodeToString(settings.copy())).awaitSuspending()
    }

    suspend fun getSettings(userId: String): Setting {
        var settings: Setting? = getCachedSetting(userId)
        if(settings == null) {
            settings = collection.findOne(Setting::userId eq userId)
        }
        if(settings == null) {
            settings = Setting(userId)
        } else setCacheSetting(settings)
        return settings
    }
}