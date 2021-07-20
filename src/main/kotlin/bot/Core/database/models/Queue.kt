package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Queue (
    val id: String,
    val uuid: String,
    val gameType: GameType,

    val hypixelData: HypixelData,

    val filter: HypixelData? = null,
    val ignoredList: List<String>? = arrayListOf(),

    var _isNew: Boolean = false
)