package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class User (
    val id: String,
    var uuid: String? = null,

    var hypixelData: HypixelData? = null,

    var lastUpdated: Int? = null,
    var _isNew: Boolean = false
)