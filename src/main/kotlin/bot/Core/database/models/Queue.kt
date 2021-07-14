package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class HypixelData(
    var fkdr: Int? = null,
    var winstreak: Int? = null,
    var bedwarsLevel: Int? = null,
)

@Serializable
data class Queue (
    val uuid: String,

    val hypixelData: HypixelData,

    val filter: HypixelData? = null,
    val ignoredList: List<String>,

    var _isNew: Boolean = false
)