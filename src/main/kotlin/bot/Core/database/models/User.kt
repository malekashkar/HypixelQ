package bot.Core.database.models

import bot.utils.api.Hypixel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class HypixelData(
    var fkdr: Double? = null,
    var winstreak: Int? = null,
    var bedwarsLevel: Int? = null,
    var displayName: String? = null,
    var rank: String? = null
)

@Serializable
data class User (
    var id: String? = null,
    var uuid: String? = null,

    var hypixelData: HypixelData,
    var ignoredList: MutableList<String> = mutableListOf(),

    var lastUpdated: Long = System.currentTimeMillis(),
    var _isNew: Boolean = false,

    @Contextual val _id: Id<User> = newId(),
)