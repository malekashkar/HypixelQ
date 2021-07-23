package bot.Core.database.models

import kotlinx.serialization.Serializable

@Serializable
data class PartyInvite(
    val inviteMessageId: String,
    val inviter: Player,
    val invited: Player,

    val cratedAt: Long = System.currentTimeMillis()
)