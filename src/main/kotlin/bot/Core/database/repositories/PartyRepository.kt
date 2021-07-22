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

    suspend fun findPartyWithPlayer(playerId: String): Party? {
        return collection.findOne(Party::players / Player::playerId eq playerId)
    }

    suspend fun getParty(leaderPlayer: Player): Party {
        return collection.findOne(
            Party::players / Player::playerId eq leaderPlayer.playerId,
            Party::players / Player::leader eq true
        ) ?: Party(mutableListOf(leaderPlayer))
    }

    suspend fun deleteParty(leaderId: String) {
        collection.deleteOne(
            Party::players / Player::playerId eq leaderId,
            Party::players / Player::leader eq true
        )
    }

    suspend fun addPartyPlayer(party: Party, player: Player) {
        val leader = party.players.find { it.leader }
        if(leader != null) {
            party.players.add(player)
            collection.updateOne(
                Party::players / Player::playerId eq leader.playerId,
                set(Party::players setTo party.players)
            )
            ensureSaved(party)
        }
    }

    suspend fun removePartyPlayer(party: Party, playerId: String) {
        val player = party.players.find { it.playerId == playerId }
        val leader = party.players.find { it.leader }
        if(player != null && leader != null) {
            party.players.remove(player)
            collection.updateOne(
                Party::players / Player::playerId eq leader.playerId,
                set(Party::players setTo party.players)
            )
            ensureSaved(party)
        }
    }
}