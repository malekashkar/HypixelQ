package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class PartyInvite(
    val inviteMessageId: String,
    val inviterId: String,
    val invitedId: String
)