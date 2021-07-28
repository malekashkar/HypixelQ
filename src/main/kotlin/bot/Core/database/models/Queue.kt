package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Queue (
    val player: Player,
    val gameType: GameType,
    val hypixelData: HypixelData,
    val ignoredList: List<String>? = arrayListOf(),
)