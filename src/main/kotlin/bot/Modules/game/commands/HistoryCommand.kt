package bot.Modules.game.commands

import bot.Bot
import bot.Core.database.models.User
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.EmbedPaginator
import bot.utils.extensions.humanizeMs
import net.dv8tion.jda.api.EmbedBuilder

class HistoryCommand: Command() {
    override val name = "history"
    override val description = "Check your history of games played."

    private suspend fun historyEmbeds(userData: User): List<EmbedBuilder>? {
        if(userData.uuid != null && userData.id != null) {
            val history = Bot.database.archiveRepository.getPlayerHistory(userData.id!!)
            if(history.isNotEmpty()) {
                return history.chunked(5).map { historyChunk ->
                    val desc = historyChunk.map { archive ->
                        val playersDesc = archive.players.mapNotNull { player ->
                            val playerData = Bot.database.userRepository.getUser(player.playerId)
                            if(playerData.hypixel?.displayName != null) {
                                if(player.leader) ":star2: " else "" + playerData.hypixel?.displayName
                            } else null
                        }
                        "**Game Lasted ${archive.gameLength.humanizeMs()}**\n" + playersDesc.joinToString(", ")
                    }
                    EmbedTemplates.normal(desc.joinToString("\n\n"), "Game History")
                }
            }
        }
        return null
    }

    @Executor
    suspend fun execute(
        context: ICommandContext,
        @Argument(optional = true) page: Int?
    ) {
        val historyEmbeds = historyEmbeds(context.getUserData())
        if(historyEmbeds != null && historyEmbeds.isNotEmpty()) {
           EmbedPaginator(
               context,
               historyEmbeds.size,
               { historyEmbeds[it] },
               if (page == null) 0 else page - 1
           ).start()
        } else {
            context.reply(
                EmbedTemplates
                    .error("You don't seem to have a game history!")
                    .build()
            ).queue()
        }
    }
}