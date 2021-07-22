package bot.Modules.game.commands

import bot.Bot
import bot.Core.database.models.Archive
import bot.Core.database.models.Player
import bot.Core.database.models.User
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.EmbedPaginator
import bot.utils.extensions.humanizeMs
import bot.utils.extensions.humanizeMsDate
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class HistoryCommand: Command() {
    override val name = "history"
    override val description = "Check your history of games played."

    private suspend fun historyEmbeds(userData: User): List<EmbedBuilder>? {
        if(userData.uuid != null && userData.id != null) {
            val history = Bot.database.archiveRepository.getPlayerHistory(userData.id!!)
            if(history.isNotEmpty()) {
                val playersData = history
                    .flatMap { it.players }
                    .map { Bot.database.userRepository.getUser(it.playerId) }
                return history
                    .chunked(5)
                    .map { historyChunk ->
                        val description: MutableList<String> = mutableListOf()
                        historyChunk.forEach { archive ->
                            val playersDescription: MutableList<String> = mutableListOf()
                            archive.players.forEach { player ->
                                playersDescription.add(
                                    if(player.leader) {
                                        ":star2: " + (playersData.find { data -> data.id == player.playerId }?.hypixelData?.displayName ?: "N/A")
                                    } else {
                                        playersData.find { data -> data.id == player.playerId }?.hypixelData?.displayName ?: "N/A"
                                    }
                                )
                            }
                            description.add("**Game Lasted ${archive.gameLength.humanizeMs()}**\n" + playersDescription.joinToString(", "))
                        }
                        EmbedTemplates
                            .normal(description.joinToString("\n\n"), "Game History")
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