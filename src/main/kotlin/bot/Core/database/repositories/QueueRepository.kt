package bot.Core.database.repositories

import bot.Bot
import bot.Core.database.Database
import bot.Core.database.models.*
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection

class QueueRepository(
    database: Database,
    private val collection: CoroutineCollection<Queue>
): Repository(database) {
    suspend fun createQueue(
        player: Player,
        score: Int,
        ignoredList: List<String>? = arrayListOf(),
        gameType: GameType
    ) {
        val queueData = Queue(
            player,
            gameType,
            score,
            ignoredList
        )
        collection.insertOne(queueData)
    }

    suspend fun deleteQueue(
        discordId: String? = null,
        playerUuid: String? = null
    ) {
        if(discordId != null) {
            collection.deleteOne(Queue::player / Player::playerId eq discordId)
        } else if(playerUuid != null) {
            collection.deleteOne(Queue::player / Player::playerUuid eq playerUuid)
        }
    }

    suspend fun findQueue(
        discordId: String? = null,
        playerUuid: String? = null
    ): Queue? {
        return if(discordId != null) {
            collection.findOne(Queue::player / Player::playerId eq discordId)
        } else if(playerUuid != null) {
            collection.findOne(Queue::player / Player::playerUuid eq playerUuid)
        } else {
            null
        }
    }

    suspend fun searchForPlayers(playerData: User, gameType: GameType): MutableList<Player>? {
        val lessResult = collection.aggregate<Queue>(
            LessThanSearchQueue(playerData).pipeline.toList()
        ).toList()
        val greaterResult = collection.aggregate<Queue>(
            GreaterThanSearchQueue(playerData).pipeline.toList()
        ).toList()
        val totalResults = greaterResult.size + lessResult.size

        return if(totalResults >= gameType.size - 1) {
            val foundPlayers: MutableList<Player> = mutableListOf()
            for(i in 1 until gameType.size) {
                if(lessResult.isNotEmpty() && greaterResult.isNotEmpty()) {
                    val lessThanDifference = playerData.score - lessResult.first().score
                    val greaterThanDifference = greaterResult.first().score - playerData.score
                    if(lessThanDifference < greaterThanDifference) {
                        foundPlayers.add(lessResult.first().player)
                        lessResult.filter { it.player == lessResult.first().player }
                    } else {
                        foundPlayers.add(greaterResult.first().player)
                        greaterResult.filter { it.player == greaterResult.first().player }
                    }
                } else if(lessResult.isNotEmpty()) {
                    foundPlayers.add(lessResult.first().player)
                    lessResult.filter { it.player == lessResult.first().player }
                } else if(greaterResult.isNotEmpty()) {
                    foundPlayers.add(greaterResult.first().player)
                    greaterResult.filter { it.player == greaterResult.first().player }
                }
            }
            foundPlayers
        } else {
            null
        }
    }

    data class LessThanSearchQueue(val playerData: User) {
        val pipeline: Array<Bson>
            get() {
                val aggregation = arrayListOf<Bson>()

                if(playerData.ignoredList.isNotEmpty()) {
                    aggregation.add(match(Queue::player / Player::playerUuid nin playerData.ignoredList))
                }

                aggregation.add(match(Queue::score lte playerData.score))
                aggregation.add(sort(descending(Queue::score)))

                return aggregation.toTypedArray()
            }
    }

    data class GreaterThanSearchQueue(val playerData: User) {
        val pipeline: Array<Bson>
            get() {
                val aggregation = arrayListOf<Bson>()

                if(playerData.ignoredList.isNotEmpty()) {
                    aggregation.add(match(Queue::player / Player::playerUuid nin playerData.ignoredList))
                }

                aggregation.add(match(Queue::score gte playerData.score))
                aggregation.add(sort(ascending(Queue::score)))

                return aggregation.toTypedArray()
            }
    }
}