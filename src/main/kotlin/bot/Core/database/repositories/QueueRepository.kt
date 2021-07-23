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
        hypixelData: HypixelData,
        filter: HypixelData?,
        ignoredList: List<String>? = arrayListOf(),
        gameType: GameType
    ) {
        val queueData = Queue(
            player,
            gameType,
            hypixelData,
            filter,
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
        if(discordId != null) {
            return collection.findOne(Queue::player / Player::playerId eq discordId)
        } else if(playerUuid != null) {
            return collection.findOne(Queue::player / Player::playerUuid eq playerUuid)
        } else {
            return null
        }
    }

    suspend fun searchForPlayers(playerData: User, filter: HypixelData, gameType: GameType): List<Player>? {
        val result = collection.aggregate<Queue>(
            SearchQueueOptions(playerData, filter).pipeline.toList()
        )
        return if(result.toList().size >= gameType.size - 1) {
            result.toList().slice(0 until gameType.size - 1).map {
                Player(it.player.leader, it.player.playerId, it.player.playerUuid)
            }
        } else {
            null
        }
    }

    data class SearchQueueOptions(
        val playerData: User,
        val searchFilter: HypixelData
    ) {
        val pipeline: Array<Bson>
            get() {
                val aggregation = arrayListOf<Bson>()

                if(playerData.ignoredList.isNotEmpty()) {
                    aggregation.add(match(Queue::player / Player::playerUuid nin playerData.ignoredList))
                }

                if (playerData.hypixelData.bedwarsLevel != null) {
                    if(searchFilter.bedwarsLevel == null) {
                        aggregation.add(match(Queue::hypixelData / HypixelData::bedwarsLevel lte playerData.hypixelData.bedwarsLevel))
                        aggregation.add(sort(descending(Queue::hypixelData / HypixelData::bedwarsLevel)))
                    } else {
                        aggregation.add(match(
                            Queue::hypixelData / HypixelData::bedwarsLevel lte playerData.hypixelData.bedwarsLevel,
                            Queue::hypixelData / HypixelData::bedwarsLevel gte searchFilter.bedwarsLevel
                        ))
                        aggregation.add(sort(descending(Queue::hypixelData / HypixelData::bedwarsLevel)))
                    }
                }

                return aggregation.toTypedArray()
            }
    }
}