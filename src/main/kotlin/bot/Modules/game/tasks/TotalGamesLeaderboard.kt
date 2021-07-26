package bot.Modules.game.tasks

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Task
import bot.utils.Config
import java.time.DayOfWeek
import java.time.LocalDateTime

class TotalGamesLeaderboard: Task() {
    override val name = "totalgamesleaderboard"
    override val interval = 1000L

    object emojis {
        const val first = "🥇"
        const val second = "\uD83E\uDD48"
        const val third = "\uD83E\uDD49"
    }

    override suspend fun execute() {
        val now = LocalDateTime.now()
        if(
            now.dayOfWeek == DayOfWeek.MONDAY &&
            now.hour == 0 &&
            now.minute == 0
        ) {
            val guild = Bot.getMainGuild()
            val config = Bot.database.configRepository.getConfig()

            val gameLength = Bot.database.archiveRepository.gameLengthWeeklyLeaderboard()


            val gameCountPlayers = Bot.database.archiveRepository.gameCountWeeklyLeaderboard()
            val gameCountChannel = guild!!.getTextChannelById(Config.Channels.totalGamesLeaderboard)
            val gameCountMessage = gameCountChannel!!.history.getMessageById(config.gameCountLbMessageId!!)

            gameCountPlayers.mapIndexed { index, leaderboardPlayer -> {
                // val playerInfo = Bot.database.userRepository.getUser(leaderboardPlayer.playerId)
                "$index. ${leaderboardPlayer.playerId}"
            } }
            // Format the gameCountPlayers

            if(gameCountMessage != null) {
                gameCountMessage.editMessageEmbeds(
                    EmbedTemplates
                        .normal(
                            "testing this feature",
                            "Weekly Leaderboard | Total Games"
                        ).build()
                ).queue()
            } else {
                gameCountChannel.sendMessageEmbeds(
                    EmbedTemplates
                        .normal(
                            "testing this feature",
                            "Weekly Leaderboard | Total Games"
                        ).build()
                ).queue()
            }
        }
    }
}
