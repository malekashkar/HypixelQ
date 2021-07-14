package bot.Core.database.repositories
import bot.Core.database.Database

abstract class Repository(open val database: Database) {
    open suspend fun createIndexes() {}
}