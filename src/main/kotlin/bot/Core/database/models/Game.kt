package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    var leader: Boolean = false,
    val playerId: String,
    val playerUuid: String? = null
)

@Serializable
enum class GameType(val size: Int) {
    DUOS(2),
    TRIOS(3),
    FOURS(4)
}

@Serializable
data class Game(
    val categoryId: String,
    val type: GameType,
    val players: List<Player>,

    var createdAt: Long = System.currentTimeMillis(),
    var _isNew: Boolean = false
)