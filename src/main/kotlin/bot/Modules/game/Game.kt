package bot.Modules.game

import bot.Bot
import bot.Core.database.models.GameType
import bot.Core.database.models.Player
import bot.Core.structures.EmbedTemplates
import bot.utils.Config
import dev.minn.jda.ktx.Message
import dev.minn.jda.ktx.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild as JdaGuild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Guild

object Game {
    private fun getGameType(playerSize: Int): GameType {
        return when(playerSize) {
            2 -> GameType.DUOS
            3 -> GameType.TRIOS
            4 -> GameType.FOURS
            else -> GameType.DUOS
        }
    }

    suspend fun createGame(
        guild: Guild,
        players: List<Player>
    ) {
        val members = players.map { guild.retrieveMemberById(it.playerId).await() }

        val missingPlayers: MutableList<Player> = mutableListOf()
        for(member in members) {
            if(member.voiceState != null && !member.voiceState!!.inVoiceChannel()) {
                val player = players.find { it.playerId == member.id }
                if(player != null) {
                    println(member.id)
                    missingPlayers.add(player)
                    Bot.database.queueRepository.deleteQueue(discordId = player.playerId)
                }
            }
        }

        if(missingPlayers.isEmpty() && members.size == players.size) {
            val gamesCount = Bot.database.gameCollection.countDocuments() + 1
            val gameCategory = guild
                .createCategory("[$gamesCount] Game Category")
                .await()
            val gameTextPre = gameCategory
                .createTextChannel("game-chat")
                .addRolePermissionOverride(
                    guild.publicRole.idLong,
                    mutableListOf(),
                    mutableListOf(Permission.VIEW_CHANNEL)
                )
            val gameVoicePre = gameCategory
                .createVoiceChannel("[$gamesCount] Game Voice")
                .addRolePermissionOverride(
                    guild.publicRole.idLong,
                    mutableListOf(Permission.VIEW_CHANNEL),
                    mutableListOf(Permission.VOICE_CONNECT)
                )

            for(member in members) {
                gameTextPre.addMemberPermissionOverride(
                    member.idLong,
                    mutableListOf(
                        Permission.VIEW_CHANNEL,
                        Permission.MESSAGE_WRITE,
                        Permission.MESSAGE_READ,
                        Permission.MESSAGE_HISTORY,
                    ),
                    mutableListOf()
                )
                gameVoicePre.addMemberPermissionOverride(
                    member.idLong,
                    mutableListOf(
                        Permission.VIEW_CHANNEL,
                        Permission.VOICE_CONNECT,
                        Permission.VOICE_SPEAK,
                    ),
                    mutableListOf()
                )
            }

            val gameText = gameTextPre.await()
            val gameVoice = gameVoicePre.await()

            for(member in members) {
                guild.moveVoiceMember(member, gameVoice).queue()
            }

            gameText.sendMessage(
                Message(
                    members.joinToString { it.asMention },
                    EmbedTemplates
                        .normal(
                            "In this *text channel*, you will be able to run commands that are game specific and communicate with teammates that are not able to voice chat.\n" +
                                    "In the *voice channel*, all members must stay active in order for the game to continue. Once all members leave the call, the game will be dis-activated.\n\n" +
                                    "If all members do not leave the voice channel, the game will remain open. Please refrain from doing this if you are not currently playing with others.\n\n" +
                                    "**Happy playing!**",
                            "Welcome to your BedwarsQ Game"
                        )
                        .build()
                )
            ).queue()

            for(player in players) {
                Bot.database.queueRepository.deleteQueue(playerUuid = player.playerUuid)
            }

            Bot.database.gameRepository.createGame(
                gameCategory.id,
                GameType.DUOS,
                players
            )
        } else {
            for(player in missingPlayers) {
                val userData = Bot.database.userRepository.getUser(player.playerId)
                Bot.database.queueRepository.createQueue(
                    player,
                    userData.hypixelData,
                    userData.ignoredList,
                    getGameType(players.size)
                )
            }

            val queueCommandsChannel = guild.getTextChannelById(Config.Channels.queueCommandsChannel)
            queueCommandsChannel?.sendMessageEmbeds(
                EmbedTemplates
                    .error("One of the members of your queued game has disappeared!\n" +
                            "You have been placed in the queue again, please be patient!")
                    .build()
            )?.queue()
        }
    }

    suspend fun endGame(guild: JdaGuild, categoryId: String) {
        Bot.database.gameRepository.deleteGame(categoryId)

        val gameCategory = guild.getCategoryById(categoryId)
        if(gameCategory != null) {
            for(channel in gameCategory.channels) {
                channel.delete().queue()
            }
            gameCategory.delete().queue()
        }
    }
}