package bot.Core.database.repositories

import bot.Core.database.Database
import bot.Core.database.models.Archive
import bot.Core.database.models.Player
import org.litote.kmongo.coroutine.CoroutineCollection

class ArchiveRepository(
    database: Database,
    private val collection: CoroutineCollection<Archive>
): Repository(database) {
    suspend fun createArchive(
        players: List<Player>
    ) {
        collection.insertOne(
            Archive(
                System.currentTimeMillis(),
                players
            )
        )
    }
}