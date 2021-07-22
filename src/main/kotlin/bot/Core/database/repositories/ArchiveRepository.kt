package bot.Core.database.repositories

import bot.Core.database.Database
import bot.Core.database.models.Archive
import bot.Core.database.models.Player
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.*
class ArchiveRepository(
    database: Database,
    private val collection: CoroutineCollection<Archive>
): Repository(database) {
    suspend fun createArchive(
        gameLength: Long,
        players: List<Player>
    ) {
        collection.insertOne(
            Archive(
                gameLength,
                players
            )
        )
    }

    suspend fun getPlayerHistory(playerId: String): List<Archive> {
        return collection.find(Archive::players / Player::playerId eq playerId).toList()
    }
}