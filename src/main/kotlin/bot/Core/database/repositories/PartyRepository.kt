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

    suspend fun findPartyWithPlayer(player: Player): Party? {
        return collection.findOne(Party::players contains player)
    }

    suspend fun getParty(leaderPlayer: Player): Party {
        return collection.findOne(Party::leaderId eq leaderPlayer.playerId)
            ?: Party(leaderPlayer.playerId, mutableListOf(leaderPlayer))
    }

    suspend fun deleteParty(leaderId: String) {
        collection.deleteOne(Party::leaderId eq leaderId)
    }

    suspend fun addPartyPlayer(party: Party, player: Player) {
        party.players.add(player)
        collection.updateOne(Party::leaderId eq party.leaderId, set(Party::players setTo party.players))
        ensureSaved(party)
    }

    suspend fun removePartyPlayer(party: Party, player: Player) {
        party.players.remove(player)
        collection.updateOne(Party::leaderId eq party.leaderId, set(Party::players setTo party.players))
        ensureSaved(party)
    }
}