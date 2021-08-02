package bot.Core.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Stats(
    val fkdr: Double,
    val winstreak: Int,
    val level: Int,
)

@Serializable
data class HypixelData(
    var displayName: String? = null,
    var rank: String? = null,
    var discordTag: String? = null,
    var stats: Stats? = null
)

@Serializable
data class User (
    var id: String? = null,
    var uuid: String? = null,

    var hypixel: HypixelData? = null,
    var score: Int = 0,
    var ignoredList: MutableList<String> = mutableListOf(),

    var lastUpdated: Long = System.currentTimeMillis(),
    var _isNew: Boolean = false,

    @Contextual val _id: Id<User> = newId(),
)