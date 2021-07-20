package bot.Core.database.repositories

import bot.Bot
import bot.Core.database.Database
import bot.Core.database.models.Game
import bot.Core.database.models.GameType
import bot.Core.database.models.Player
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.div
import org.litote.kmongo.elemMatch
import org.litote.kmongo.eq

class GameRepository(
    database: Database,
    private val collection: CoroutineCollection<Game>
): Repository(database) {
    suspend fun createGame(
        categoryId: String,
        type: GameType,
        players: List<Player>
    ) {
        collection.insertOne(
            Game(
                categoryId,
                type,
                players,
            )
        )
    }

    suspend fun findGame(
        categoryId: String? = null,
        playerId: String? = null
    ): Game? {
        return when {
            categoryId != null -> collection.findOne(Game::categoryId eq categoryId)
            playerId != null -> collection.findOne(Game::players / Player::playerId eq playerId)
            else -> null
        }
    }

    suspend fun deleteGame(categoryId: String) {
        val gameData = collection.findOne(Game::categoryId eq categoryId)
        if(gameData != null) {
            Bot.database.archiveRepository.createArchive(gameData.players)
            collection.deleteOne(Game::categoryId eq categoryId)
        }
    }
}