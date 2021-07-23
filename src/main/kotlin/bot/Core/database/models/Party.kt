package bot.Core.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Party(
    var players: MutableList<Player> = mutableListOf(),

    var _isNew: Boolean = true,

    @Contextual val _id: Id<User> = newId(),
)