package bot.Modules.registration.tasks

import bot.Bot
import bot.Core.database.models.HypixelData
import bot.Core.database.models.User
import bot.Core.structures.base.Task
import bot.utils.Config
import bot.utils.api.Hypixel
import com.mongodb.client.model.UpdateOneModel
import kotlinx.coroutines.flow.collect
import org.litote.kmongo.*

class UpdateUsersTask: Task() {
    override val interval = 3_600_000L
    override val name = "UpdateUsers"

    override suspend fun execute() {
        val guild = Bot.getMainGuild()
        val users = Bot.database.userCollection
            .find(User::lastUpdated lte System.currentTimeMillis() - Config.Intervals.userStatsUpdateInt)
            .toFlow()
        val updates: MutableList<UpdateOneModel<User>> = mutableListOf()

        users.collect {
            if(it.uuid != null) {
                val data = Hypixel.getPlayerData(it.uuid!!)
                if (data != null) {
                    it.hypixel = data
                    updates.add(updateOne(
                        User::id eq it.id,
                        set(User::hypixel setTo data)
                    ))
                }

                if(guild != null) {
                    bot.Modules.registration.User.updateUser(guild, it, false)
                }
            }
        }

        updates.chunked(1000).forEach {
            Bot.database.userCollection.bulkWrite(it)
        }
    }
}