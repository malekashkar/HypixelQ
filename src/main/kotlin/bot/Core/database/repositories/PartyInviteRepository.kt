package bot.Core.database.repositories

import bot.Core.database.Database
import bot.Core.database.models.PartyInvite
import bot.Core.database.models.Player
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.*

class PartyInviteRepository(
    database: Database,
    private val collection: CoroutineCollection<PartyInvite>
): Repository(database) {
    suspend fun createInvite(inviteData: PartyInvite) {
        collection.insertOne(inviteData)
    }

    suspend fun findInvite(inviter: String? = null, invited: String? = null, inviteMessageId: String? = null): PartyInvite? {
        return if(inviteMessageId != null) {
            collection.findOne(PartyInvite::inviteMessageId eq inviteMessageId)
        } else {
            collection.findOne(
                PartyInvite::inviter / Player::playerId eq inviter,
                PartyInvite::invited / Player::playerId eq invited
            )
        }
    }

    suspend fun deleteInvite(inviter: String? = null, invited: String? = null, inviteMessageId: String? = null) {
        if(inviteMessageId != null) {
            collection.deleteOne(PartyInvite::inviteMessageId eq inviteMessageId)
        } else {
            collection.deleteOne(
                PartyInvite::inviter / Player::playerId eq inviter,
                PartyInvite::invited / Player::playerId eq invited
            )
        }
    }
}