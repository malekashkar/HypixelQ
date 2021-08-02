package bot.Core.database.repositories

import bot.Bot
import bot.Core.database.Database
import bot.Core.database.models.HypixelData
import bot.Core.database.models.Stats
import bot.Core.database.models.User
import bot.utils.Json
import bot.utils.api.Hypixel
import bot.utils.extensions.awaitSuspending
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.redisson.api.RMapCacheAsync

class UserRepository(
    database: Database,
    private val collection: CoroutineCollection<User>,
    private val cacheMap: RMapCacheAsync<String, String>,
) : Repository(database) {
    private suspend fun getCachedUser(uuid: String): User? {
        val json = cacheMap.getAsync(uuid).awaitSuspending() ?: return null
        return Json.decodeFromString(json)
    }

    private suspend fun setCacheUser(userData: User) {
        if (userData._isNew) {
            userData._isNew = false
            collection.insertOne(userData)
        }
        cacheMap.putAsync(userData.id, Json.encodeToString(userData.copy())).awaitSuspending()
    }

    suspend fun getUser(discordId: String? = null, uuid: String? = null) : User {
        var user: User? = null
        if(uuid != null) {
            user = getCachedUser(uuid)
        }
        if(user == null && (discordId != null || uuid != null)) {
            user = collection.findOne(or(User::uuid eq uuid, User::id eq discordId))
        }
        if(user == null) {
            user = User(discordId, uuid, _isNew = true)
        } else setCacheUser(user)
        return user
    }

    suspend fun updateId(userData: User, id: String) {
        userData.id = id
        collection.updateOne(User::_id eq userData._id, set(User::id setTo id))
        setCacheUser(userData)
    }

    suspend fun updateUuid(userData: User, uuid: String) {
        userData.uuid = uuid
        collection.updateOneById(userData._id, set(User::uuid setTo uuid))
        setCacheUser(userData)
    }

    suspend fun updateHypixelData(userData: User, hypixelData: HypixelData) {
        if(hypixelData.stats != null) {
            userData.score = bot.Modules.registration.User.calculateScore(hypixelData.stats!!)
        }
        userData.hypixel = hypixelData
        collection.updateOneById(
            userData._id,
            set(
                User::hypixel setTo hypixelData,
                User::score setTo userData.score,
                User::lastUpdated setTo System.currentTimeMillis()
            )
        )
        setCacheUser(userData)
    }

    suspend fun addToIgnoredList(userData: User, ignoreUuid: String) {
        userData.ignoredList.add(ignoreUuid)
        collection.updateOneById(userData._id, set(User::ignoredList setTo userData.ignoredList))
        setCacheUser(userData)
    }

    suspend fun removeFromIgnoreList(userData: User, ignoreUuid: String) {
        userData.ignoredList.remove(ignoreUuid)
        collection.updateOneById(userData._id, set(User::ignoredList setTo userData.ignoredList))
        setCacheUser(userData)
    }
}