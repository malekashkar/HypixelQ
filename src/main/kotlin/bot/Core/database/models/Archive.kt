package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class Archive(
    val gameEnded: Long,
    val players: List<Player>
)