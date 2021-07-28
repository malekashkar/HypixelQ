package bot.Modules.game.tasks

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Task
import bot.utils.Config
import java.time.DayOfWeek
import java.time.LocalDateTime

class TotalGamesLeaderboardTask: Task() {
    override val name = "totalgamesleaderboard"
    override val interval = 1000L

    private val emojis = arrayOf("🥇", "\uD83E\uDD48", "\uD83E\uDD49")

    override suspend fun execute() {
        val now = LocalDateTime.now()
        if(
            now.dayOfWeek == DayOfWeek.MONDAY &&
            now.hour == 0 &&
            now.minute == 0
        ) {
            val guild = Bot.getMainGuild()
            val config = Bot.database.configRepository.getConfig()

            if(guild != null) {
                val gameLengthPlayers = Bot.database.archiveRepository.gameLengthWeeklyLeaderboard()
                if(gameLengthPlayers.isNotEmpty()) {
                    val gameLengthChannel = guild.getTextChannelById(Config.Channels.gameTimeLb)
                    if(gameLengthChannel != null) {
                        val gameLengthDesc = gameLengthPlayers.mapIndexedNotNull { index, leaderboardPlayer ->
                            val playerInfo = Bot.database.userRepository.getUser(leaderboardPlayer.playerId)
                            val discordPlayer = Bot.jda.getUserById(playerInfo.id!!)
                            if (playerInfo.hypixelData.displayName != null && discordPlayer != null) {
                                "${emojis[index]} ${playerInfo.hypixelData.displayName} (${discordPlayer.asMention})"
                            } else {
                                null
                            }
                        }

                        if(config.gameLengthLbMessageId != null) {
                            gameLengthChannel.history.getMessageById(config.gameLengthLbMessageId!!)?.delete()?.queue()
                        }
                        gameLengthChannel.sendMessageEmbeds(
                            EmbedTemplates
                                .normal(
                                    gameLengthDesc.joinToString("\n"),
                                    "Weekly Leaderboard | Total Time In-Game"
                                ).build()
                        ).queue()
                    }
                }

                val gameCountPlayers = Bot.database.archiveRepository.gameCountWeeklyLeaderboard()
                if(gameCountPlayers.isNotEmpty()) {
                    val gameCountChannel = guild.getTextChannelById(Config.Channels.gameCountLb)
                    if(gameCountChannel != null) {
                        val gameCountDesc = gameCountPlayers.mapIndexedNotNull { index, leaderboardPlayer ->
                            val playerInfo = Bot.database.userRepository.getUser(leaderboardPlayer.playerId)
                            val discordPlayer = Bot.jda.getUserById(playerInfo.id!!)
                            if (playerInfo.hypixelData.displayName != null && discordPlayer != null) {
                                "${emojis[index]} ${playerInfo.hypixelData.displayName} (${discordPlayer.asMention})"
                            } else {
                                null
                            }
                        }

                        if(config.gameCountLbMessageId != null) {
                            gameCountChannel.history.getMessageById(config.gameCountLbMessageId!!)?.delete()?.queue()
                        }
                        gameCountChannel.sendMessageEmbeds(
                            EmbedTemplates
                                .normal(
                                    gameCountDesc.joinToString("\n"),
                                    "Weekly Leaderboard | Total Games Played"
                                ).build()
                        ).queue()
                    }
                }
            }
        }
    }
}
