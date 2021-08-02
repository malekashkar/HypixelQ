package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Queue (
    val player: Player,
    val gameType: GameType,
    val score: Int,
    val ignoredList: List<String>? = arrayListOf(),
)