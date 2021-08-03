package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Setting(
    val userId: String,

    var joinQueueMessage: Boolean = true,

    var _isNew: Boolean = false,
)