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
    suspend fun createGame(
        guild: Guild,
        players: List<Player>
    ) {
        val members = players.map { guild.retrieveMemberById(it.playerId).await() }

        var missingMember = false
        for(member in members) {
            if(member.voiceState == null) {
                missingMember = true
                Bot.database.queueRepository.deleteQueue(discordId = member.id)
            }
        }

        if(!missingMember && members.size == players.size) {
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
                    members.map { it.asMention }.joinToString { "" },
                    EmbedTemplates
                        .normal(
                            "Welcome this is a guide",
                            "Welcome to the game babes"
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