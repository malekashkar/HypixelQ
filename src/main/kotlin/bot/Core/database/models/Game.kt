package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Player(
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
    var _isNew: Boolean = false
)