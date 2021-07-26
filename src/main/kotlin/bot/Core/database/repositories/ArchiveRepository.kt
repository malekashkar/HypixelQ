package bot.Core.database.repositories

import bot.Core.database.Database
import bot.Core.database.models.*
import com.mongodb.client.model.Field
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.aggregate

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
                players,
                System.currentTimeMillis()
            )
        )
    }

    suspend fun getPlayerHistory(playerId: String): List<Archive> {
        return collection.find(Archive::players / Player::playerId eq playerId).toList()
    }

    @Serializable
    data class LeaderboardPlayer(
        @SerialName("_id") val playerId: String,
        val gameCount: Int?,
        val gameTotalLength: Long?
        )

    suspend fun gameCountWeeklyLeaderboard(): List<LeaderboardPlayer> {
        return collection.aggregate<LeaderboardPlayer>(
            Archive::players.unwind(),
            match(
                Archive::endedAt gte (System.currentTimeMillis() - 604_800_000L) // one week
            ),
            group(
                Archive::players / Player::playerId,
                LeaderboardPlayer::gameCount sum 1,
            ),
            sort(
                descending(
                    LeaderboardPlayer::gameCount
                )
            ),
            limit(10)
        ).toList()
    }

    suspend fun gameLengthWeeklyLeaderboard(): List<LeaderboardPlayer> {
        return collection.aggregate<LeaderboardPlayer>(
            Archive::players.unwind(),
            match(
                Archive::endedAt gte (System.currentTimeMillis() - 604_800_000L) // one week
            ),
            group(
                Archive::players / Player::playerId,
                LeaderboardPlayer::gameTotalLength sum Archive::gameLength,
            ),
            sort(
                descending(
                    LeaderboardPlayer::gameCount
                )
            ),
            limit(10)
        ).toList()
    }
}