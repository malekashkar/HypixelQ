package bot.Core.database.repositories

import bot.Core.database.Database
import bot.Core.database.models.Config
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection

class ConfigRepository(
    database: Database,
    private val collection: CoroutineCollection<Config>
): Repository(database) {
    suspend fun getConfig(): Config {
        var config = collection.findOne()
        if(config == null) {
            config = Config()
            collection.insertOne(config)
        }
        return config
    }

    suspend fun setTicketPanelId(ticketPanelId: String) {
        val config = getConfig()
        collection.updateOne(
            Config::_id eq config._id,
            set(Config::ticketPanelId setTo ticketPanelId)
        )
    }

    suspend fun setGameCountLbMessageId(messageId: String) {
        val config = getConfig()
        collection.updateOne(
            Config::_id eq config._id,
            set(Config::gameCountLbMessageId setTo messageId)
        )
    }

    suspend fun setGameLengthLbMessageId(messageId: String) {
        val config = getConfig()
        collection.updateOne(
            Config::_id eq config._id,
            set(Config::gameLengthLbMessageId setTo messageId)
        )
    }
}