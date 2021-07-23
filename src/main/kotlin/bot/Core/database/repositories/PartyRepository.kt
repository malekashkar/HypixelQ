package bot.Core.database.repositories

import bot.Core.database.Database
import bot.Core.database.models.Party
import bot.Core.database.models.Player
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection

class PartyRepository(
    database: Database,
    private val collection: CoroutineCollection<Party>
): Repository(database) {
    private suspend fun ensureSaved(partyData: Party) {
        if(partyData._isNew) {
            partyData._isNew = false
            collection.insertOne(partyData)
        }
    }

    suspend fun createParty(players: MutableList<Player>) {
        ensureSaved(Party(players))
    }

    suspend fun findPartyWithPlayer(playerId: String): Party? {
        return collection.findOne(Party::players / Player::playerId eq playerId)
    }

    suspend fun deleteParty(party: Party) {
        collection.deleteOneById(party._id)
    }

    suspend fun addPartyPlayer(party: Party, player: Player) {
        party.players.add(player)
        collection.updateOneById(party._id, set(Party::players setTo party.players))
        ensureSaved(party)
    }

    suspend fun removePartyPlayer(party: Party, playerId: String) {
        val player = party.players.find { it.playerId == playerId }
        if(player != null) {
            party.players.remove(player)
            collection.updateOneById(party._id, set(Party::players setTo party.players))
            ensureSaved(party)
        }
    }

    suspend fun transferLeadership(party: Party, leaderId: String, playerId: String) {
        party.players.find { it.playerId == leaderId }?.leader = false
        party.players.find { it.playerId == playerId }?.leader = true
        collection.updateOneById(party._id, set(Party::players setTo party.players))
        ensureSaved(party)
    }
}