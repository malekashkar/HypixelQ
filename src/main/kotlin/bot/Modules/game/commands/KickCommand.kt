package bot.Modules.game.commands

import bot.Bot
import bot.Core.database.models.Player
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import net.dv8tion.jda.api.entities.User

class KickCommand: Command() {
    override val name = "kick"
    override val description = "Kick a user from your party"

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = false) user: User?
    ) {
        if(user != null) {
            val userData = context.getUserData()
            if(userData.uuid != null) {
                val player = Player(context.author.id, userData.uuid)
                val party = Bot.database.partyRepository.findPartyWithPlayer(player)
                val targetUserData = Bot.database.userRepository.getUser(discordId = user.id)
                if(targetUserData.uuid != null) {
                    if(party != null && party.leaderId == context.author.id) {
                        val targetUserPlayer = Player(user.id, targetUserData.uuid)
                        if(player == targetUserPlayer) {
                          context.reply(
                              EmbedTemplates
                                  .error("You may not kick yourself from your own party!")
                                  .build()
                          ).queue()
                        } else if(party.players.contains(targetUserPlayer)) {
                            Bot.database.partyRepository.removePartyPlayer(party, targetUserPlayer)
                            context.reply(
                                EmbedTemplates
                                    .normal(
                                        "You kicked ${user.asMention} from your party!",
                                        "User Kicked"
                                    ).build()
                            ).queue()
                        } else {
                            context.reply(
                                EmbedTemplates
                                    .error("The user you mentioned is not in your party!")
                                    .build()
                            ).queue()
                        }
                    } else {
                        context.reply(
                            EmbedTemplates
                                .error("You are not the leader of your current party!")
                                .build()
                        ).queue()
                    }
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("The user you would like to kick is not registered.")
                            .build()
                    ).queue()
                }
            } else {
                context.reply(
                    EmbedTemplates
                        .error("You may not run this command before registering.")
                        .build()
                ).queue()
            }
        } else {
            context.reply(
                EmbedTemplates
                    .error("Please tag the user you would like to kick from your party!")
                    .build()
            ).queue()
        }
    }
}