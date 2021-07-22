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
            val party = Bot.database.partyRepository.findPartyWithPlayer(playerData.id!!)
            if(party != null) {
                val players = party.players.map { context.guild!!.retrieveMemberById(it.playerId).await() }
                val leader = party.players.find { it.leader }
                val playersMention = players.filter { it.id != leader?.playerId }.joinToString { it.asMention }
                context.reply(
                    EmbedTemplates
                        .normal(
                            "Below is the list of players in your party.",
                            "Party Players"
                        )
                        .addField(
                            "Leader",
                            players.find { it.id == leader?.playerId }?.asMention,
                            true
                        )
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