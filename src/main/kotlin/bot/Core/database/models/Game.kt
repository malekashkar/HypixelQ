package bot.Core.database.models

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
data class Player(
    val playerId: String,
    val playerUuid: String,
    val score: Int,

    val party: Boolean = false,
    var leader: Boolean = false
)

@Serializable
enum class GameType(val size: Int) {
    DUOS(2),
    TRIOS(3),
    FOURS(4)
}

@Serializable
data class Game(
    val type: GameType,
    val players: MutableList<Player>,

    var categoryId: String? = null,
    var createdAt: Long = System.currentTimeMillis(),

    var _isNew: Boolean = false
)