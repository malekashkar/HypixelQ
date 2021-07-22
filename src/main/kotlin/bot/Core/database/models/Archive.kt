package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Archive(
    val gameLength: Long,
    val players: List<Player>
)