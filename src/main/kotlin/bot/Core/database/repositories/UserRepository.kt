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

    suspend fun getUser(jdaUser: JDAUser) : User {
        var user = collection.findOneById(jdaUser.id)
        if(user == null) {
            user = User(jdaUser.id, _isNew = true)
        }
        return user
    }

    suspend fun updateUuid(userData: User, uuid: String) {
        userData.uuid = uuid
        collection.updateOne(User::id eq userData.id, set(User::uuid setTo uuid))
        ensureSaved(userData)
    }

    suspend fun updateStats(userData: User, fkdr: Int?, winstreak: Int?, bedwarsLevel: Int?) {
        val hypixelData = HypixelData(fkdr, winstreak, bedwarsLevel)
        userData.hypixelData = hypixelData
        collection.updateOne(User::id eq userData.id, set(User::hypixelData setTo hypixelData))
        ensureSaved(userData)
    }
}