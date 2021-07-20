package bot.Modules.game.events

import bot.Core.structures.EmbedTemplates
import bot.Core.structures.base.Event
import bot.utils.Config
import dev.minn.jda.ktx.Message
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent

class QueueEvent: Event() {
    override val name = "queue"

    @Handler
    fun onVoiceChannelJoin(event: GuildVoiceJoinEvent) {
        if(event.member.user.isBot) return

        if(event.channelJoined.id == Config.Channels.queueRoomChannel) {
            val queueCommandsChannel = event.guild.getTextChannelById(Config.Channels.queueCommandsChannel)
            queueCommandsChannel?.sendMessage(
                Message(
                    event.member.asMention,
                    EmbedTemplates
                        .normal(
                            "Run the command `${Config.prefix}queue` to get started!",
                            "Queue Notification"
                        )
                        .build()
                )
            )?.queue()
        }
    }
}