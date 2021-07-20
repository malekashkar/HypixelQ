package bot.Core.database.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.litote.kmongo.Id
import org.litote.kmongo.newId

@Serializable
data class Config(
    var ticketPanelId: String? = null,

    @Contextual val _id: Id<Config> = newId()
)