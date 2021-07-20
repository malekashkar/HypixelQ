package bot.Modules.game.commands

import bot.Bot
import bot.Core.database.models.Player
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext

class LeaveCommand: Command() {
    override val name = "leave"
    override val description = "Leave a party you're in, or disband if you're the leader"

    @Executor
    suspend fun execute(context: ICommandContext) {
        val playerData = Bot.database.userRepository.getUser(discordId = context.author.id)
        if(playerData.uuid != null) {
            val player = Player(playerData.id!!, playerData.uuid)
            val party = Bot.database.partyRepository.findPartyWithPlayer(player)
            if(party != null) {
                if(party.leaderId == context.author.id) {
                    Bot.database.partyRepository.deleteParty(party.leaderId)
                    context.reply(
                        EmbedTemplates
                            .normal(
                                "Your party has been disbanded since you were leader!",
                                "Party Disbanded"
                            )
                            .build()
                    ).queue()
                } else {
                    Bot.database.partyRepository.removePartyPlayer(party, player)
                    context.reply(
                        EmbedTemplates
                            .normal(
                                "You left the party you were in!",
                                "Party Left"
                            )
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates
                        .error("You are not in a party to leave currently.")
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