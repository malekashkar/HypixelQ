package bot.Modules.game.events

import bot.Bot
import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Event
import bot.Modules.game.Game
import bot.utils.Config
import dev.minn.jda.ktx.Message
import dev.minn.jda.ktx.await
import kotlinx.coroutines.*
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceUpdateEvent
import java.util.concurrent.Executors

class EndGameEvent: Event() {
    override val name = "endgame"

    private val coroutineScope = CoroutineScope(Executors.newCachedThreadPool().asCoroutineDispatcher())

    @Handler
    suspend fun handle(event: GenericGuildVoiceUpdateEvent) {
        if(
            event.channelLeft?.parent != null &&
            event.channelLeft?.members?.isEmpty() == true
        ) {
            val gameData = Bot.database.gameRepository.findGame(event.channelLeft!!.parent!!.id)
            if(gameData != null) {
                coroutineScope.launch {
                    delay(120_000)

                    val voiceChannelUpdated = event.guild.getVoiceChannelById(event.channelLeft!!.id)
                    if(
                        voiceChannelUpdated?.parent != null &&
                        voiceChannelUpdated.members.isEmpty()
                    ) {
                        Game.endGame(event.guild, voiceChannelUpdated.parent!!.id)

                        val queueCommandsChannel = event.guild.getTextChannelById(Config.Channels.queueCommandsChannel)
                        if(queueCommandsChannel != null) {
                            val playersMentionString = gameData.players.map {
                                event.guild.retrieveMemberById(it.playerId).await().asMention
                            }.joinToString("")

                            queueCommandsChannel.sendMessage(
                                Message(
                                    playersMentionString,
                                    EmbedTemplates
                                        .normal(
                                            "Your game has been ended due to voice channel inactivity!",
                                            "Game Ended"
                                        ).build()
                                )
                            ).queue()
                        }
                    }
                }
            }
        }
    }
}