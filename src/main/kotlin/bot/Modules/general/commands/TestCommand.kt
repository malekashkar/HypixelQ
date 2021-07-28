package bot.Modules.general.commands

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Command
import bot.api.ICommandContext
import bot.utils.Config
import bot.utils.extensions.humanizeMs
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import java.util.*

class TestCommand: Command() {
    override val name = "test"
    override val description = "This command is meant for only testing!"

    override var requiredUserPermissions = arrayOf(Permission.MANAGE_SERVER)

    private val emojis = arrayOf("🥇", "\uD83E\uDD48", "\uD83E\uDD49")

    data class LeaderboardDetails(
        val displayName: String,
        val discordPlayer: User,
        val totalGameTime: Long? = null,
        val totalGames: Int? = null
        )

    @Executor
    suspend fun execute(context: ICommandContext) {
        val config = Bot.database.configRepository.getConfig()

        val gameLengthPlayers = Bot.database.archiveRepository.gameLengthWeeklyLeaderboard()
        if(gameLengthPlayers.isNotEmpty()) {
            val gameLengthChannel = context.guild!!.getTextChannelById(Config.Channels.gameTimeLb)
            if(gameLengthChannel != null) {
                val gameLengthInfo = gameLengthPlayers.mapNotNull {
                    val playerInfo = Bot.database.userRepository.getUser(it.playerId)
                    val discordPlayer = Bot.jda.getUserById(playerInfo.id!!)
                    if(playerInfo.hypixelData.displayName != null && discordPlayer != null) {
                        LeaderboardDetails(
                            playerInfo.hypixelData.displayName!!,
                            discordPlayer,
                            it.gameTotalLength
                        )
                    } else null
                }

                if(gameLengthInfo.isNotEmpty()) {
                    val gameLengthDescription = gameLengthInfo.mapIndexed { index, leaderboardDetails ->
                        "${emojis[index]} ${leaderboardDetails.displayName} (${leaderboardDetails.discordPlayer.asMention}) | " +
                                leaderboardDetails.totalGameTime?.humanizeMs()
                    }.joinToString("\n")
                    if(config.gameLengthLbMessageId != null) {
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
        if(gameCountPlayers.isNotEmpty()) {
            val gameCountChannel = context.guild!!.getTextChannelById(Config.Channels.gameCountLb)
            if(gameCountChannel != null) {
                val gameCountInfo = gameCountPlayers.mapNotNull {
                    val playerInfo = Bot.database.userRepository.getUser(it.playerId)
                    val discordPlayer = Bot.jda.getUserById(playerInfo.id!!)
                    if (playerInfo.hypixelData.displayName != null && discordPlayer != null) {
                        LeaderboardDetails(
                            playerInfo.hypixelData.displayName!!,
                            discordPlayer,
                            totalGames = it.gameCount
                        )
                    } else {
                        null
                    }
                }

                if(gameCountInfo.isNotEmpty()) {
                    val gameCountDescription = gameCountInfo.mapIndexed { index, leaderboardDetails ->
                        "${emojis[index]} ${leaderboardDetails.displayName} (${leaderboardDetails.discordPlayer.asMention}) | " +
                                "${leaderboardDetails.totalGames} Games"
                    }.joinToString("\n")
                    if(config.gameCountLbMessageId != null) {
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