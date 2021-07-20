package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Party(
    val leaderId: String,
    var players: MutableList<Player> = mutableListOf(),

    var _isNew: Boolean = true
)