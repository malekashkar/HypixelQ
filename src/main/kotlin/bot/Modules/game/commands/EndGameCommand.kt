package bot.Modules.game.commands

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.Modules.game.Game
import bot.api.ICommandContext
import bot.utils.extensions.humanizeMsDate
import net.dv8tion.jda.api.Permission

class EndGameCommand: Command() {
    override val name = "endgame"
    override val description = "Manually end a game and delete its channels."

    override var requiredUserPermissions = arrayOf(Permission.MANAGE_SERVER)

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = false) categoryId: String?
    ) {
        if(categoryId != null) {
            val gameData = Bot.database.gameRepository.findGame(categoryId)
            if(gameData != null) {
                Game.endGame(context.guild!!, categoryId)
                context.reply(
                    EmbedTemplates
                        .normal(
                            "The game with ID `$categoryId` has been manually ended!",
                            "Game Ended"
                        ).build()
                ).queue()
            } else {
                val gameCategory = context.guild!!.getCategoryById(categoryId)
                if(gameCategory != null) {
                    for(channel in gameCategory.channels) {
                        channel.delete().queue()
                    }
                    gameCategory.delete().queue()

                    context.reply(
                        EmbedTemplates
                            .normal(
                                "The game with ID `$categoryId` has been manually ended!",
                                "Game Ended"
                            ).build()
                    ).queue()
                } else {
                    context.reply(
                        EmbedTemplates
                            .error("No game category found with the ID `$categoryId`.")
                            .build()
                    ).queue()
                }
            }
        } else {
            context.reply(
                EmbedTemplates
                    .error("Please provide the category ID of the running game!")
                    .build()
            ).queue()
        }
    }
}