package bot.Modules.game.tasks

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Task
import bot.utils.Config
import bot.utils.extensions.humanizeMs
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.entities.User
import java.time.DayOfWeek
import java.time.LocalDateTime

class LeaderboardTask: Task() {
    override val name = "totalgamesleaderboard"
    override val interval = 1000L

    private val emojis = arrayOf("🥇", "\uD83E\uDD48", "\uD83E\uDD49")

    data class LeaderboardDetails(
        val displayName: String,
        val discordPlayer: User,
        val totalGameTime: Long? = null,
        val totalGames: Int? = null
    )

    override suspend fun execute() {
        val now = LocalDateTime.now()
        if(
            now.dayOfWeek == DayOfWeek.MONDAY &&
            now.hour == 0 &&
            now.minute == 0
        ) {
            val guild = Bot.getMainGuild()
            if(guild != null) {
                val config = Bot.database.configRepository.getConfig()
                val gameLengthPlayers = Bot.database.archiveRepository.gameLengthWeeklyLeaderboard()
                if (gameLengthPlayers.isNotEmpty()) {
                    val gameLengthChannel = guild.getTextChannelById(Config.Channels.gameTimeLb)
                    if (gameLengthChannel != null) {
                        val gameLengthInfo = gameLengthPlayers.mapNotNull {
                            val userData = Bot.database.userRepository.getUser(it.playerId)
                            val discordPlayer = Bot.jda.getUserById(userData.id!!)
                            if (userData.hypixel?.displayName != null && discordPlayer != null) {
                                LeaderboardDetails(
                                    userData.hypixel!!.displayName!!,
                                    discordPlayer,
                                    it.gameTotalLength
                                )
                            } else null
                        }

                        if (gameLengthInfo.isNotEmpty()) {
                            val gameLengthDescription = gameLengthInfo.mapIndexed { index, leaderboardDetails ->
                                "${emojis[index]} ${leaderboardDetails.displayName} (${leaderboardDetails.discordPlayer.asMention}) | " +
                                        leaderboardDetails.totalGameTime?.humanizeMs()
                            }.joinToString("\n")
                            if (config.gameLengthLbMessageId != null) {
                                gameLengthChannel
                                    .getHistoryFromBeginning(10).await()
                                    .getMessageById(config.gameLengthLbMessageId!!)
                                    ?.delete()?.queue()
                            }
                            val gameLengthMsg = gameLengthChannel.sendMessageEmbeds(
                                EmbedTemplates
                                    .normal(
                                        gameLengthDescription,
                                        "Weekly Leaderboard | Total Time In-Game"
                                    ).build()
                            ).await()
                            Bot.database.configRepository.setGameLengthLbMessageId(gameLengthMsg.id)
                        }
                    }
                }

                val gameCountPlayers = Bot.database.archiveRepository.gameCountWeeklyLeaderboard()
                if (gameCountPlayers.isNotEmpty()) {
                    val gameCountChannel = guild.getTextChannelById(Config.Channels.gameCountLb)
                    if (gameCountChannel != null) {
                        val gameCountInfo = gameCountPlayers.mapNotNull {
                            val userData = Bot.database.userRepository.getUser(it.playerId)
                            val discordPlayer = Bot.jda.getUserById(userData.id!!)
                            if (userData.hypixel!!.displayName != null && discordPlayer != null) {
                                LeaderboardDetails(
                                    userData.hypixel!!.displayName!!,
                                    discordPlayer,
                                    totalGames = it.gameCount
                                )
                            } else {
                                null
                            }
                        }

                        if (gameCountInfo.isNotEmpty()) {
                            val gameCountDescription = gameCountInfo.mapIndexed { index, leaderboardDetails ->
                                "${emojis[index]} ${leaderboardDetails.displayName} (${leaderboardDetails.discordPlayer.asMention}) | " +
                                        "${leaderboardDetails.totalGames} Games"
                            }.joinToString("\n")
                            if (config.gameCountLbMessageId != null) {
                                gameCountChannel
                                    .getHistoryFromBeginning(10).await()
                                    .getMessageById(config.gameCountLbMessageId!!)
                                    ?.delete()?.queue()
                            }
                            val gameCountMsg = gameCountChannel.sendMessageEmbeds(
                                EmbedTemplates
                                    .normal(
                                        gameCountDescription,
                                        "Weekly Leaderboard | Total Games Played"
                                    ).build()
                            ).await()
                            Bot.database.configRepository.setGameCountLbMessageId(gameCountMsg.id)
                        }
                    }
                }
            }
        }
    }
}
