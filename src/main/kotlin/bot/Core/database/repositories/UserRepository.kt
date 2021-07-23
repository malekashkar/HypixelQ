package bot.Core.database.repositories

import bot.Core.database.Database
import bot.Core.database.models.HypixelData
import net.dv8tion.jda.api.entities.User as JDAUser
import bot.Core.database.models.User
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.set
import org.litote.kmongo.setTo

class UserRepository(
    database: Database,
    private val collection: CoroutineCollection<User>
) : Repository(database) {
    private suspend fun ensureSaved(userData: User) {
        if(userData._isNew) {
            userData._isNew = false
            collection.insertOne(userData)
        }
    }

    suspend fun getUser(
        discordId: String? = null,
        mojangUuid: String? = null
    ) : User {
        var user = if(mojangUuid != null) {
            collection.findOne(User::uuid eq mojangUuid)
        } else {
            collection.findOne(User::id eq discordId)
        }
        if(user == null) {
            user = User(discordId, mojangUuid, hypixelData = HypixelData(), _isNew = true)
        }
        return user
    }

    suspend fun updateId(userData: User, id: String) {
        userData.id = id
        collection.updateOneById(userData._id, set(User::id setTo id))
        ensureSaved(userData)
    }

    suspend fun updateUuid(userData: User, uuid: String) {
        userData.uuid = uuid
        collection.updateOneById(userData._id, set(User::uuid setTo uuid))
        ensureSaved(userData)
    }

    suspend fun updateStats(userData: User, data: HypixelData) {
        userData.hypixelData = data
        userData.lastUpdated = System.currentTimeMillis()
        collection.updateOneById(
            userData._id,
            set(
                User::hypixelData setTo data,
                User::lastUpdated setTo System.currentTimeMillis()
            )
        )
        ensureSaved(userData)
    }

    suspend fun addToIgnoredList(userData: User, ignoreUuid: String) {
        userData.ignoredList.add(ignoreUuid)
        collection.updateOneById(userData._id, set(User::ignoredList setTo userData.ignoredList))
        ensureSaved(userData)
    }

    suspend fun removeFromIgnoreList(userData: User, ignoreUuid: String) {
        userData.ignoredList.remove(ignoreUuid)
        collection.updateOneById(userData._id, set(User::ignoredList setTo userData.ignoredList))
        ensureSaved(userData)
    }
}