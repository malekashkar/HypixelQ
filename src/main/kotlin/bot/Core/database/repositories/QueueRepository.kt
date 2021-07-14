package bot.Core.database.repositories

import bot.Core.database.Database
import bot.Core.database.models.Queue
import org.litote.kmongo.coroutine.CoroutineCollection

class QueueRepository(
    database: Database,
    private val collection: CoroutineCollection<Queue>
): Repository(database) {
    private suspend fun ensureSaved(queueData: Queue) {
        if(queueData._isNew) {
            queueData._isNew = false
            collection.insertOne(queueData)
        }
    }
}