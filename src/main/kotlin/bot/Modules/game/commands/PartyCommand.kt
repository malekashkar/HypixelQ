package bot.Modules.game.commands

import bot.Bot
import bot.Core.database.models.Player
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import dev.minn.jda.ktx.await

class PartyCommand: Command() {
    override val name = "party"
    override val description = "Get information on your current party"

    @Executor
    suspend fun execute(context: ICommandContext) {
        val playerData = Bot.database.userRepository.getUser(discordId = context.author.id)
        if(playerData.uuid != null) {
            val player = Player(playerData.id!!, playerData.uuid)
            val party = Bot.database.partyRepository.findPartyWithPlayer(player)
            if(party != null) {
                val players = party.players.map { context.guild!!.retrieveMemberById(it.playerId).await() }
                val playersMention = players.filter { it.id != party.leaderId }.joinToString { it.asMention }
                val leader = players.find { it.id == party.leaderId }?.asMention
                context.reply(
                    EmbedTemplates
                        .normal(
                            "Below is the list of players in your party.",
                            "Party Players"
                        )
                        .addField("Leader", leader, true)
                        .addField("Players", playersMention, true)
                        .build()
                ).queue()
            } else {
                context.reply(
                    EmbedTemplates
                        .error("You are not in a party currently.")
                        .build()
                ).queue()
            }
        } else {
            context.reply(
                EmbedTemplates
                    .error("Please sign up before running this command!")
                    .build()
            ).queue()
        }
    }
}